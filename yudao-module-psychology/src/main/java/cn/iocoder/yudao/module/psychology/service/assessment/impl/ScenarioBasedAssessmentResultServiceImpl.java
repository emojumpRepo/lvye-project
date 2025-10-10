package cn.iocoder.yudao.module.psychology.service.assessment.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.*;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.*;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.*;
import cn.iocoder.yudao.module.psychology.rule.executor.ExpressionExecutor;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateContext;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateResult;
import cn.iocoder.yudao.module.psychology.rule.strategy.impl.AbnormalFactorAggregationStrategy;
import cn.iocoder.yudao.module.psychology.rule.strategy.DimensionInterlockStrategy;
import cn.iocoder.yudao.module.psychology.service.assessment.ScenarioBasedAssessmentResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireDimensionService;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireDimensionRespVO;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于场景的测评结果计算服务实现
 *
 * @author MinGoo
 */
@Service
public class ScenarioBasedAssessmentResultServiceImpl implements ScenarioBasedAssessmentResultService {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioBasedAssessmentResultServiceImpl.class);

    @Resource
    private AssessmentResultMapper assessmentResultMapper;
    
    @Resource
    private AssessmentResultConfigMapper assessmentResultConfigMapper;
    
    @Resource
    private AssessmentScenarioSlotMapper assessmentScenarioSlotMapper;
    
    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;
    
    @Resource
    private DimensionResultMapper dimensionResultMapper;
    
    @Resource
    private QuestionnaireDimensionService questionnaireDimensionService;
    
    @Resource
    private ExpressionExecutor expressionExecutor;
    
    @Resource
    private AbnormalFactorAggregationStrategy abnormalFactorAggregationStrategy;
    
    @Resource
    private DimensionInterlockStrategy dimensionInterlockStrategy;

    @Override
    @Transactional
    public AssessmentResultDO calculateAssessmentResult(Long assessmentId, Long scenarioId, Long studentProfileId, Long userId, String taskNo) {
        logger.info("开始计算基于场景的测评结果: assessmentId={}, scenarioId={}, studentProfileId={}, userId={}, taskNo={}", 
            assessmentId, scenarioId, studentProfileId, userId, taskNo);

        // 1. 检查是否有场景ID绑定
        if (scenarioId == null) {
            logger.warn("测评未绑定场景ID，跳过场景化计算");
            return null;
        }

        // 2. 查找场景对应的测评结果配置
        List<AssessmentResultConfigDO> configs = assessmentResultConfigMapper.selectListByScenarioId(scenarioId);
        if (configs.isEmpty()) {
            logger.warn("场景ID={}未找到测评结果配置", scenarioId);
            return null;
        }

        // 3. 查找场景下的插槽及其关联的问卷结果
        List<AssessmentScenarioSlotDO> slots = assessmentScenarioSlotMapper.selectListByScenarioId(scenarioId);
        if (slots.isEmpty()) {
            logger.warn("场景ID={}未找到插槽配置", scenarioId);
            return null;
        }

        // 4. 收集所有参与测评计算的维度结果（限定在本次任务）
        List<DimensionResultDO> participatingDimensions = collectParticipatingDimensions(slots, userId, taskNo);
        if (participatingDimensions.isEmpty()) {
            logger.warn("未找到参与测评计算的维度结果");
            return null;
        }

        // 4.1 将本次任务中“测评相关问卷”的所有维度结果快照回写到对应的 questionnaire_result.resultData（以问卷为单位）
        try {
            snapshotDimensionsIntoQuestionnaireResults(slots, userId, taskNo);
        } catch (Exception e) {
            logger.error("回写问卷维度结果到questionnaire_result失败: taskNo={}, userId={}", taskNo, userId, e);
        }

        // 5. 构建评估上下文
        EvaluateContext context = buildAssessmentEvaluateContext(participatingDimensions, userId);
        
        // 将参与测评计算的维度结果存储到上下文扩展数据中，供策略使用
        context.getExt().put("participatingDimensions", participatingDimensions);
        context.getExt().put("dimensionResults", participatingDimensions); // 兼容已有策略实现

        // 6. 按优先级执行测评结果计算规则
        AssessmentResultDO result = executeAssessmentRules(configs, context, assessmentId, studentProfileId, userId);

        // 6.1 兜底补齐必填业务键，避免DB非空约束报错
        if (result != null) {
            if (result.getParticipantId() == null) {
                // participantId 绑定学生档案ID
                result.setParticipantId(studentProfileId);
            }
            if (result.getDimensionCode() == null || result.getDimensionCode().trim().isEmpty()) {
                result.setDimensionCode("total");
            }
            if (result.getTaskNo() == null) {
                result.setTaskNo(taskNo);
            }
        }

        // 7. 保存测评结果（先查询是否已存在，存在则更新，否则插入）
        if (result != null) {
            // 查询是否已存在相同 taskNo + participantId + dimensionCode 的记录
            List<AssessmentResultDO> existList = assessmentResultMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<AssessmentResultDO>()
                    .eq(AssessmentResultDO::getTaskNo, result.getTaskNo())
                    .eq(AssessmentResultDO::getParticipantId, result.getParticipantId())
                    .eq(AssessmentResultDO::getDimensionCode, result.getDimensionCode())
            );

            if (existList == null || existList.isEmpty()) {
                // 不存在，插入新记录
                assessmentResultMapper.insert(result);
                logger.info("测评结果计算完成并保存(新增): assessmentResultId={}", result.getId());
            } else {
                // 已存在，更新第一条记录
                AssessmentResultDO exist = existList.get(0);
                result.setId(exist.getId());
                assessmentResultMapper.updateById(result);
                logger.info("测评结果计算完成并保存(更新): assessmentResultId={}, taskNo={}, participantId={}",
                    result.getId(), result.getTaskNo(), result.getParticipantId());

                // 如果存在多条重复记录，删除其他记录
                if (existList.size() > 1) {
                    logger.warn("发现{}条重复的测评结果记录, taskNo={}, participantId={}, 将删除多余记录",
                        existList.size(), result.getTaskNo(), result.getParticipantId());
                    for (int i = 1; i < existList.size(); i++) {
                        assessmentResultMapper.deleteById(existList.get(i).getId());
                        logger.info("已删除重复的测评结果记录, id={}", existList.get(i).getId());
                    }
                }
            }
        }

        return result;
    }

    // 兼容旧调用（无 taskNo）
    @Transactional
    public AssessmentResultDO calculateAssessmentResult(Long assessmentId, Long scenarioId, Long studentProfileId, Long userId) {
        return calculateAssessmentResult(assessmentId, scenarioId, studentProfileId, userId, null);
    }

    @Override
    @Transactional
    public AssessmentResultDO recalculateAssessmentResult(Long assessmentResultId) {
        // 查询现有测评结果
        AssessmentResultDO existingResult = assessmentResultMapper.selectById(assessmentResultId);
        if (existingResult == null) {
            throw new IllegalArgumentException("测评结果不存在: " + assessmentResultId);
        }

        // 重新计算
        AssessmentResultDO newResult = calculateAssessmentResult(
            existingResult.getAssessmentId(),
            existingResult.getScenarioId(), 
            existingResult.getStudentProfileId(),
            existingResult.getUserId()
        );

        if (newResult != null) {
            // 更新现有记录
            newResult.setId(assessmentResultId);
            assessmentResultMapper.updateById(newResult);
            logger.info("测评结果重新计算完成: assessmentResultId={}", assessmentResultId);
            return newResult;
        }

        return existingResult;
    }

    /**
     * 收集参与测评计算的维度结果
     */
    private List<DimensionResultDO> collectParticipatingDimensions(List<AssessmentScenarioSlotDO> slots, Long userId, String taskNo) {
        List<DimensionResultDO> participatingDimensions = new ArrayList<>();
        Set<Long> seenDimensionIds = new HashSet<>();

        for (AssessmentScenarioSlotDO slot : slots) {
            // 解析插槽关联的问卷ID列表
            List<Long> questionnaireIds = parseQuestionnaireIds(slot.getQuestionnaireIds());
            
            for (Long questionnaireId : questionnaireIds) {
                // 先按任务号+用户查本次任务的问卷结果，再按问卷ID过滤；无任务号则按用户+问卷查全部历史
                List<QuestionnaireResultDO> questionnaireResults;
                if (taskNo != null && !taskNo.isEmpty()) {
                    List<QuestionnaireResultDO> taskResults = questionnaireResultMapper
                        .selectListByTaskNoAndUserId(taskNo, userId);
                    questionnaireResults = taskResults.stream()
                        .filter(r -> questionnaireId.equals(r.getQuestionnaireId()))
                        .collect(Collectors.toList());
                } else {
                    questionnaireResults = questionnaireResultMapper
                        .selectListByUserIdAndQuestionnaireId(userId, questionnaireId);
                }

                for (QuestionnaireResultDO qResult : questionnaireResults) {
                    // 查找该问卷结果的维度结果（严格依赖 questionnaire_result_id）
                    List<DimensionResultDO> dimensionResults = dimensionResultMapper
                        .selectListByQuestionnaireResultId(qResult.getId());

                    // 筛选参与测评计算的维度，并按 dimensionId 去重
                    for (DimensionResultDO dimensionResult : dimensionResults) {
                        QuestionnaireDimensionRespVO dimension = questionnaireDimensionService
                            .getDimension(dimensionResult.getDimensionId());

                        if (dimension != null && Boolean.TRUE.equals(dimension.getParticipateAssessmentCalc())) {
                            Long dimId = dimension.getId();
                            if (dimId != null && seenDimensionIds.add(dimId)) {
                                participatingDimensions.add(dimensionResult);
                                logger.debug("维度参与测评计算: dimensionId={}, dimensionName={}",
                                    dimId, dimension.getDimensionName());
                            }
                        }
                    }
                }
            }
        }

        logger.info("收集到{}个参与测评计算的维度结果", participatingDimensions.size());
        return participatingDimensions;
    }

    /**
     * 将本次任务下、场景插槽关联的问卷的“全部维度结果”写入各自的 questionnaire_result.resultData 字段
     * 结构：resultData = [ { dimensionId, dimensionCode, score, isAbnormal, riskLevel, level, teacherComment, studentComment, description, questionnaireId }, ... ]
     */
    private void snapshotDimensionsIntoQuestionnaireResults(List<AssessmentScenarioSlotDO> slots, Long userId, String taskNo) {
        if (taskNo == null || taskNo.isEmpty()) {
            // 没有任务号就不做快照
            return;
        }

        // 找出本任务下该用户的所有问卷结果
        List<QuestionnaireResultDO> taskResults = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, userId);
        if (taskResults == null || taskResults.isEmpty()) {
            return;
        }

        // 构建插槽内允许的问卷ID集合（若未配置则默认不限制）
        Set<Long> allowedQuestionnaireIds = new HashSet<>();
        for (AssessmentScenarioSlotDO slot : slots) {
            List<Long> ids = parseQuestionnaireIds(slot.getQuestionnaireIds());
            if (ids != null) {
                allowedQuestionnaireIds.addAll(ids);
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        for (QuestionnaireResultDO qr : taskResults) {
            // 若插槽配置了问卷ID集合，则只处理在集合内的问卷
            if (!allowedQuestionnaireIds.isEmpty() && !allowedQuestionnaireIds.contains(qr.getQuestionnaireId())) {
                continue;
            }

            List<DimensionResultDO> dimensionResults = dimensionResultMapper.selectListByQuestionnaireResultId(qr.getId());
            if (dimensionResults == null) {
                continue;
            }

            // 映射为精简JSON对象列表
            List<Map<String, Object>> payload = new ArrayList<>();
            for (DimensionResultDO dr : dimensionResults) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("dimensionId", dr.getDimensionId());
                item.put("dimensionCode", dr.getDimensionCode());
                item.put("score", dr.getScore());
                item.put("isAbnormal", dr.getIsAbnormal());
                item.put("riskLevel", dr.getRiskLevel());
                item.put("level", dr.getLevel());
                item.put("teacherComment", dr.getTeacherComment());
                item.put("studentComment", dr.getStudentComment());
                item.put("description", dr.getDescription());
                item.put("questionnaireId", qr.getQuestionnaireId());
                // 维度名称与维度描述来自问卷维度表
                String dimName = null;
                String dimDesc = null;
                try {
                    QuestionnaireDimensionRespVO dimVO = questionnaireDimensionService.getDimension(dr.getDimensionId());
                    if (dimVO != null) {
                        dimName = dimVO.getDimensionName();
                        dimDesc = dimVO.getDescription();
                    }
                } catch (Exception ignore) {}
                if (dimName == null || dimName.isEmpty()) {
                    dimName = dr.getDimensionCode() != null ? dr.getDimensionCode() : ("DIM-" + dr.getDimensionId());
                }
                item.put("dimensionName", dimName);
                // 若结果中的 description 为空，则补充维度表描述
                if (item.get("description") == null || String.valueOf(item.get("description")).isEmpty()) {
                    item.put("description", dimDesc);
                }
                payload.add(item);
            }

            try {
                qr.setResultData(mapper.writeValueAsString(payload));
                questionnaireResultMapper.updateById(qr);
                logger.debug("已回写问卷维度结果: questionnaireResultId={}, 问卷ID={}, 维度数={}",
                        qr.getId(), qr.getQuestionnaireId(), payload.size());
            } catch (Exception e) {
                logger.error("序列化/更新问卷维度结果失败: questionnaireResultId={}", qr.getId(), e);
            }
        }
    }

    /**
     * 解析问卷ID列表（JSON格式）
     */
    private List<Long> parseQuestionnaireIds(String questionnaireIdsJson) {
        if (questionnaireIdsJson == null || questionnaireIdsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return JSON.parseArray(questionnaireIdsJson, Long.class);
        } catch (Exception e) {
            // 兼容逗号分隔格式："1,2,3"
            try {
                List<Long> ids = new ArrayList<>();
                for (String part : questionnaireIdsJson.split(",")) {
                    String s = part.trim();
                    if (!s.isEmpty()) {
                        ids.add(Long.parseLong(s));
                    }
                }
                if (!ids.isEmpty()) {
                    logger.warn("问卷ID列表非JSON格式，已按逗号分隔解析: {} -> {}", questionnaireIdsJson, ids);
                    return ids;
                }
            } catch (Exception ignore) {
                // ignore
            }
            logger.error("解析问卷ID列表失败: {}", questionnaireIdsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 构建测评评估上下文
     */
    private EvaluateContext buildAssessmentEvaluateContext(List<DimensionResultDO> dimensions, Long userId) {
        EvaluateContext context = new EvaluateContext();

        // 添加用户变量
        context.withVar("userId", userId);

        // 统计异常维度数量
        int abnormalCount = (int) dimensions.stream()
            .filter(d -> d.getIsAbnormal() != null && d.getIsAbnormal() == 1)
            .count();
        context.withVar("abnormalCount", abnormalCount);

        // 按风险等级统计维度数量
        Map<Integer, Long> riskLevelCounts = dimensions.stream()
            .filter(d -> d.getRiskLevel() != null)
            .collect(Collectors.groupingBy(DimensionResultDO::getRiskLevel, Collectors.counting()));

        context.withVar("riskLevel1Count", riskLevelCounts.getOrDefault(1, 0L).intValue()); // 无/低风险
        context.withVar("riskLevel2Count", riskLevelCounts.getOrDefault(2, 0L).intValue()); // 轻度风险
        context.withVar("riskLevel3Count", riskLevelCounts.getOrDefault(3, 0L).intValue()); // 中度风险
        context.withVar("riskLevel4Count", riskLevelCounts.getOrDefault(4, 0L).intValue()); // 重度风险

        // 添加维度级别统计（按维度编码）
        Map<String, String> dimensionLevels = dimensions.stream()
            .filter(d -> d.getDimensionCode() != null && d.getLevel() != null)
            .collect(Collectors.toMap(
                DimensionResultDO::getDimensionCode,
                DimensionResultDO::getLevel,
                (existing, replacement) -> existing // 如果有重复，保留第一个
            ));

        for (Map.Entry<String, String> entry : dimensionLevels.entrySet()) {
            context.withVar("dimension_" + entry.getKey() + "_level", entry.getValue());
        }

        logger.info("构建测评评估上下文完成: abnormalCount={}, riskLevelCounts={}", abnormalCount, riskLevelCounts);
        return context;
    }

    /**
     * 执行测评结果计算规则
     */
    private AssessmentResultDO executeAssessmentRules(List<AssessmentResultConfigDO> configs, 
                                                    EvaluateContext context,
                                                    Long assessmentId, 
                                                    Long studentProfileId, 
                                                    Long userId) {
        // 按ID排序确保优先级
        configs.sort(Comparator.comparing(AssessmentResultConfigDO::getId));

        // 获取参与测评计算的维度结果
        List<DimensionResultDO> participatingDimensions = getParticipatingDimensionsFromContext(context);

        for (AssessmentResultConfigDO config : configs) {
            try {
                if (isJsonRule(config.getCalculateFormula())) {
                    // 检查规则类型并使用相应策略
                    AssessmentResultDO result = executeStrategyBasedRule(config, participatingDimensions, context, assessmentId, studentProfileId, userId);
                    if (result != null) {
                        logger.info("测评规则命中: configId={}, level={}", config.getId(), result.getLevel());
                        return result;
                    }
                } else {
                    // 兼容旧规则（如果需要）
                    logger.warn("测评配置使用非JSON规则，暂不支持: configId={}", config.getId());
                }
            } catch (Exception e) {
                logger.error("测评规则执行失败: configId={}", config.getId(), e);
            }
        }

        logger.warn("没有测评规则命中，返回默认结果");
        return createDefaultAssessmentResult(assessmentId, studentProfileId, userId);
    }

    /**
     * 判断是否为JSON规则
     */
    private boolean isJsonRule(String calculateFormula) {
        if (calculateFormula == null || calculateFormula.trim().isEmpty()) {
            return false;
        }
        return calculateFormula.trim().startsWith("{");
    }

    /**
     * 执行JSON测评规则
     */
    private AssessmentResultDO executeJsonAssessmentRule(AssessmentResultConfigDO config, 
                                                       EvaluateContext context,
                                                       Long assessmentId, 
                                                       Long studentProfileId, 
                                                       Long userId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonRule = mapper.readTree(config.getCalculateFormula());

            logger.info("执行测评JSON规则: configId={}, formula={}", config.getId(), config.getCalculateFormula());
            logger.info("上下文变量: {}", context.getVariables());

            // 使用表达式引擎评估规则
            EvaluateResult result = expressionExecutor.evaluate(jsonRule, context);

            logger.info("测评规则评估结果: matched={}, payload={}", result.isMatched(), result.getPayload());

            if (result.isMatched()) {
                // 构建测评结果
                AssessmentResultDO assessmentResult = new AssessmentResultDO();
                assessmentResult.setAssessmentId(assessmentId);
                assessmentResult.setStudentProfileId(studentProfileId);
                assessmentResult.setUserId(userId);
                assessmentResult.setLevel(config.getLevel());
                assessmentResult.setDescription(config.getDescription());
                assessmentResult.setSuggestions(config.getSuggestions());
                assessmentResult.setComment(config.getComment());

                // 从payload中提取风险等级或其他数据
                if (result.getPayload() != null && result.getPayload().containsKey("riskLevel")) {
                    Object riskLevelObj = result.getPayload().get("riskLevel");
                    if (riskLevelObj instanceof Number) {
                        assessmentResult.setRiskLevel(((Number) riskLevelObj).intValue());
                    }
                }

                // 必填业务主键字段：participantId 与 dimensionCode（total）
                assessmentResult.setParticipantId(userId);
                assessmentResult.setDimensionCode("total");

                // 处理扩展数据
                if (result.getPayload() != null && !result.getPayload().isEmpty()) {
                    ObjectMapper payloadMapper = new ObjectMapper();
                    assessmentResult.setResultData(payloadMapper.writeValueAsString(result.getPayload()));
                }

                return assessmentResult;
            }
        } catch (Exception e) {
            logger.error("测评JSON规则执行失败: configId={}", config.getId(), e);
        }
        return null;
    }

    /**
     * 基于策略执行测评规则
     */
    private AssessmentResultDO executeStrategyBasedRule(AssessmentResultConfigDO config, 
                                                      List<DimensionResultDO> participatingDimensions,
                                                      EvaluateContext context,
                                                      Long assessmentId, 
                                                      Long studentProfileId, 
                                                      Long userId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonRule = mapper.readTree(config.getCalculateFormula());

            logger.info("执行策略测评规则: configId={}, formula={}", config.getId(), config.getCalculateFormula());

            EvaluateResult result = null;

            // 检查规则类型并使用相应策略
            if (jsonRule.has("abnormalFactorAggregation")) {
                // 异常因子叠加策略（签名要求传入上下文）
                result = abnormalFactorAggregationStrategy.calculateAssessmentResult(config, context);
            } else if (jsonRule.has("dimensionInterlock")) {
                // 多维度联动策略
                result = dimensionInterlockStrategy.calculateAssessmentResult(config, participatingDimensions);
            } else {
                // 使用通用表达式引擎
                result = expressionExecutor.evaluate(jsonRule, context);
            }

            logger.info("策略测评规则评估结果: matched={}, payload={}", 
                result != null ? result.isMatched() : false, 
                result != null ? result.getPayload() : null);

            if (result != null && result.isMatched()) {
                // 构建测评结果
                AssessmentResultDO assessmentResult = new AssessmentResultDO();
                assessmentResult.setAssessmentId(assessmentId);
                assessmentResult.setStudentProfileId(studentProfileId);
                assessmentResult.setUserId(userId);

                // 从payload中提取结果信息
                Map<String, Object> payload = result.getPayload();
                if (payload.containsKey("level")) {
                    assessmentResult.setLevel((String) payload.get("level"));
                } else {
                    assessmentResult.setLevel(config.getLevel());
                }

                if (payload.containsKey("riskLevel")) {
                    assessmentResult.setRiskLevel((Integer) payload.get("riskLevel"));
                }

                if (payload.containsKey("description")) {
                    assessmentResult.setDescription((String) payload.get("description"));
                } else {
                    assessmentResult.setDescription(config.getDescription());
                }

                assessmentResult.setSuggestions(config.getSuggestions());
                assessmentResult.setComment(config.getComment());

                // 处理扩展数据
                if (!payload.isEmpty()) {
                    ObjectMapper payloadMapper = new ObjectMapper();
                    assessmentResult.setResultData(payloadMapper.writeValueAsString(payload));
                }

                return assessmentResult;
            }
        } catch (Exception e) {
            logger.error("策略测评规则执行失败: configId={}", config.getId(), e);
        }
        return null;
    }

    /**
     * 从上下文中获取参与测评计算的维度结果
     */
    @SuppressWarnings("unchecked")
    private List<DimensionResultDO> getParticipatingDimensionsFromContext(EvaluateContext context) {
        // 这里需要从上下文中获取维度结果列表
        // 由于当前上下文主要存储统计数据，我们需要重新查询维度结果
        // 或者在构建上下文时将维度结果列表也传入
        
        // 临时实现：从上下文的扩展数据中获取
        Object dimensionsObj = context.getExt().get("participatingDimensions");
        if (dimensionsObj instanceof List) {
            return (List<DimensionResultDO>) dimensionsObj;
        }
        
        return Collections.emptyList();
    }

    /**
     * 创建默认测评结果
     */
    private AssessmentResultDO createDefaultAssessmentResult(Long assessmentId, Long studentProfileId, Long userId) {
        AssessmentResultDO result = new AssessmentResultDO();
        result.setAssessmentId(assessmentId);
        result.setStudentProfileId(studentProfileId);
        result.setUserId(userId);
        result.setLevel("正常");
        result.setRiskLevel(1); // 无/低风险
        result.setDescription("未发现显著问题");
        result.setSuggestions("继续保持良好状态");
        result.setComment("测评结果正常，建议继续保持健康的生活方式。");
        return result;
    }

}
