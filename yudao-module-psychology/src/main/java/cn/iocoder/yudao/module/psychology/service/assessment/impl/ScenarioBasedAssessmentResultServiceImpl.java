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
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.ModuleResultConfigMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.ModuleResultMapper;
import cn.iocoder.yudao.module.psychology.rule.strategy.impl.DefaultModuleResultStrategy;
import cn.iocoder.yudao.module.psychology.rule.strategy.impl.ModuleDimensionInterlockStrategy;
import cn.iocoder.yudao.module.psychology.rule.strategy.impl.MultiDimensionLinkageEvaluator;
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

    @Resource
    private ModuleResultConfigMapper moduleResultConfigMapper;

    @Resource
    private ModuleResultMapper moduleResultMapper;

    @Resource
    private DefaultModuleResultStrategy defaultModuleResultStrategy;

    @Resource
    private ModuleDimensionInterlockStrategy moduleDimensionInterlockStrategy;

    private final MultiDimensionLinkageEvaluator multiDimensionLinkageEvaluator = new MultiDimensionLinkageEvaluator();

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

    @Override
    @Transactional
    public void generateModuleResultsForCompletedSlots(String taskNo, Long scenarioId, Long studentProfileId, Long userId, Long currentQuestionnaireId) {
        logger.info("[模块][即时] 入口: taskNo={}, scenarioId={}, userId={}, currentQid={}", taskNo, scenarioId, userId, currentQuestionnaireId);
        if (scenarioId == null) {
            logger.warn("[模块][即时] 场景ID为空, 退出");
            return;
        }
        
        // 查询场景插槽
        List<AssessmentScenarioSlotDO> allSlots = assessmentScenarioSlotMapper.selectListByScenarioId(scenarioId);
        logger.info("[模块][即时] 读取插槽: scenarioId={}, slotCount={}", scenarioId, allSlots != null ? allSlots.size() : 0);
        if (allSlots == null || allSlots.isEmpty()) {
            logger.warn("[模块][即时] 无插槽配置, 退出");
            return;
        }

        // 筛选目标插槽：若提供了 currentQuestionnaireId，则只处理包含该问卷的插槽；否则处理全部插槽
        List<AssessmentScenarioSlotDO> targetSlots = new ArrayList<>();
        if (currentQuestionnaireId != null) {
            for (AssessmentScenarioSlotDO slot : allSlots) {
                List<Long> qids = parseQuestionnaireIds(slot.getQuestionnaireIds());
                if (qids != null && qids.contains(currentQuestionnaireId)) {
                    targetSlots.add(slot);
                }
            }
            logger.info("[模块][即时] 筛选包含问卷ID={}的插槽: 命中{}个", currentQuestionnaireId, targetSlots.size());
            if (targetSlots.isEmpty()) {
                logger.warn("[模块][即时] 未找到包含该问卷的插槽, 退出");
                return;
            }
        } else {
            targetSlots = allSlots;
            logger.info("[模块][即时] 未指定问卷ID, 处理全部{}个插槽", targetSlots.size());
        }

        // 收集目标插槽相关的维度结果（用于上下文）
        List<DimensionResultDO> allParticipatingDimensions = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (AssessmentScenarioSlotDO slot : targetSlots) {
            List<Long> qids = parseQuestionnaireIds(slot.getQuestionnaireIds());
            if (qids == null || qids.isEmpty()) continue;
            List<QuestionnaireResultDO> taskResults = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, userId);
            for (QuestionnaireResultDO qr : taskResults) {
                if (!qids.contains(qr.getQuestionnaireId())) continue;
                List<DimensionResultDO> dimList = dimensionResultMapper.selectListByQuestionnaireResultId(qr.getId());
                for (DimensionResultDO dr : dimList) {
                    if (dr.getDimensionId() != null && seen.add(dr.getDimensionId())) {
                        allParticipatingDimensions.add(dr);
                    }
                }
            }
        }
        logger.info("[模块][即时] 收集目标插槽维度: totalDims={}", allParticipatingDimensions.size());

        // 遍历目标插槽，若该插槽内所有问卷已完成，则按模块配置生成模块结果
        for (AssessmentScenarioSlotDO slot : targetSlots) {
            List<Long> qids = parseQuestionnaireIds(slot.getQuestionnaireIds());
            logger.info("[模块][即时] 插槽检查: slotId={}, qids={}", slot.getId(), qids);
            if (qids == null || qids.isEmpty()) {
                logger.warn("[模块][即时] 插槽无问卷, 跳过: slotId={}", slot.getId());
                continue;
            }
            // 判断该插槽问卷是否全部完成
            boolean allDone = true;
            List<QuestionnaireResultDO> taskResults = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, userId);
            logger.info("[模块][即时] 插槽检查: slotId={}, taskResultsLoaded={}", slot.getId(), taskResults != null ? taskResults.size() : 0);
            for (Long qid : qids) {
                boolean found = false;
                for (QuestionnaireResultDO qr : taskResults) {
                    if (qid.equals(qr.getQuestionnaireId()) && qr.getGenerationStatus() != null && qr.getGenerationStatus() == 2) {
                        found = true; break;
                    }
                }
                logger.info("[模块][即时] 插槽问卷完成检测: slotId={}, qid={}, finished={} ", slot.getId(), qid, found);
                if (!found) { allDone = false; break; }
            }
            if (!allDone) {
                logger.info("[模块][即时] 插槽未全部完成, 跳过: slotId={}", slot.getId());
                continue;
            }

            // 筛选属于该插槽的维度
            List<DimensionResultDO> slotDimensions = new ArrayList<>();
            for (DimensionResultDO dr : allParticipatingDimensions) {
                QuestionnaireResultDO qr = questionnaireResultMapper.selectById(dr.getQuestionnaireResultId());
                if (qr != null && qids.contains(qr.getQuestionnaireId())) slotDimensions.add(dr);
            }
            logger.info("[模块][即时] 插槽维度筛选: slotId={}, dimCount={}", slot.getId(), slotDimensions.size());
            if (slotDimensions.isEmpty()) {
                logger.warn("[模块][即时] 插槽无维度, 跳过: slotId={}", slot.getId());
                continue;
            }

            // 读取模块配置
            List<ModuleResultConfigDO> moduleConfigs = moduleResultConfigMapper.selectByScenarioSlotId(slot.getId());
            logger.info("[模块][即时] 读取模块配置: slotId={}, cfgCount={}", slot.getId(), moduleConfigs != null ? moduleConfigs.size() : 0);
            if (moduleConfigs == null || moduleConfigs.isEmpty()) {
                logger.warn("[模块][即时] 插槽无模块配置, 跳过: slotId={}", slot.getId());
                continue;
            }

            // 构建上下文
            EvaluateContext baseCtx = buildUnifiedContext(slotDimensions, userId, null);
            logger.info("[模块][即时] 上下文构建完成: slotId={}, dims={}, userId={}", slot.getId(), slotDimensions.size(), userId);

            // 排序，稳定优先级
            try { moduleConfigs.sort(java.util.Comparator.comparing(ModuleResultConfigDO::getId)); } catch (Exception ignore) {}

            // 三段执行（综合->等级->评语）
            Integer finalRiskLevel = null;
            String finalTeacherComment = null;
            String finalStudentComment = null;
            String finalDescription = null;
            java.math.BigDecimal finalModuleScore = null;
            java.util.Map<String, Object> resultPayload = new java.util.LinkedHashMap<>();
            boolean matchedAny = false;

            final java.util.List<DimensionResultDO> slotDimsFinal2 = slotDimensions;
            final Long userIdFinal2 = userId;
            final EvaluateContext baseCtxFinal2 = baseCtx;

            java.util.function.Function<ModuleResultConfigDO, EvaluateResult> evalFn2 = (cfg) -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(cfg.getCalculateFormula());
                    String strategy = json.has("strategy") ? json.get("strategy").asText() : null;
                    if ("multi_linkage".equals(strategy)) {
                        EvaluateContext ruleCtx = buildUnifiedContext(slotDimsFinal2, userIdFinal2, json.get("multiDimensionV2"));
                        return multiDimensionLinkageEvaluator.evaluate(cfg.getCalculateFormula(), ruleCtx);
                    } else {
                        return defaultModuleResultStrategy.calculateModuleResult(cfg, baseCtxFinal2);
                    }
                } catch (Exception e) {
                    logger.error("[模块][即时] 规则解析失败: configId={}", cfg.getId(), e);
                    return null;
                }
            };

            // 综合（ruleType==2 或 null）
            for (ModuleResultConfigDO cfg : moduleConfigs) {
                Integer rt = cfg.getRuleType();
                if (rt != null && rt != 2) continue;
                logger.info("[模块-综合][即时] 开始评估: cfgId={}, level={}", cfg.getId(), cfg.getLevel());
                EvaluateResult ev = evalFn2.apply(cfg);
                logger.info("[模块-综合][即时] 完成: cfgId={}, matched={}, payload={}", cfg.getId(), ev != null ? ev.isMatched() : null, ev != null ? ev.getPayload() : null);
                if (ev != null && ev.isMatched()) {
                    matchedAny = true;
                    if (ev.getPayload().get("riskLevel") instanceof Number) finalRiskLevel = ((Number) ev.getPayload().get("riskLevel")).intValue();
                    Object ms = ev.getPayload().get("moduleScore");
                    if (ms instanceof Number) finalModuleScore = new java.math.BigDecimal(((Number) ms).toString());
                    resultPayload.put("level", cfg.getLevel());
                    Object sug = ev.getPayload().get("suggestion");
                    if (sug instanceof String && !((String) sug).isEmpty()) finalTeacherComment = (String) sug; else if (cfg.getSuggestions() != null && !cfg.getSuggestions().isEmpty()) finalTeacherComment = cfg.getSuggestions();
                    String sc = cn.iocoder.yudao.module.psychology.util.StudentCommentUtils.selectRandomComment(cfg.getComments());
                    if (sc != null && !sc.isEmpty()) { finalStudentComment = sc; resultPayload.put("studentComment", sc); }
                    if (ev.getPayload().get("description") instanceof String) finalDescription = (String) ev.getPayload().get("description");
                    if (finalDescription == null || finalDescription.isEmpty()) finalDescription = cfg.getDescription();
                    resultPayload.putAll(ev.getPayload());
                    break;
                }
            }

            // 等级（ruleType==0）
            for (ModuleResultConfigDO cfg : moduleConfigs) {
                if (cfg.getRuleType() == null || cfg.getRuleType() != 0) continue;
                logger.info("[模块-等级][即时] 开始评估: cfgId={}, level={}", cfg.getId(), cfg.getLevel());
                EvaluateResult ev = evalFn2.apply(cfg);
                logger.info("[模块-等级][即时] 完成: cfgId={}, matched={}, payload={}", cfg.getId(), ev != null ? ev.isMatched() : null, ev != null ? ev.getPayload() : null);
                if (ev != null && ev.isMatched()) {
                    matchedAny = true;
                    if (finalRiskLevel == null && ev.getPayload().get("riskLevel") instanceof Number) finalRiskLevel = ((Number) ev.getPayload().get("riskLevel")).intValue();
                    if (!resultPayload.containsKey("level")) resultPayload.put("level", cfg.getLevel());
                    if (ev.getPayload().get("moduleScore") instanceof Number) finalModuleScore = new java.math.BigDecimal(((Number) ev.getPayload().get("moduleScore")).toString());
                    if (finalRiskLevel != null) resultPayload.put("riskLevel", finalRiskLevel);
                    break;
                }
            }

            // 评语（ruleType==1）
            for (ModuleResultConfigDO cfg : moduleConfigs) {
                if (cfg.getRuleType() == null || cfg.getRuleType() != 1) continue;
                logger.info("[模块-评语][即时] 开始评估: cfgId={}, hasSuggestions={}, hasComments={}", cfg.getId(), cfg.getSuggestions() != null && !cfg.getSuggestions().isEmpty(), cfg.getComments() != null ? cfg.getComments().length() : null);
                EvaluateResult ev = evalFn2.apply(cfg);
                logger.info("[模块-评语][即时] 完成: cfgId={}, matched={}, payload={}", cfg.getId(), ev != null ? ev.isMatched() : null, ev != null ? ev.getPayload() : null);
                if (ev != null && ev.isMatched()) {
                    matchedAny = true;
                    String sc = cn.iocoder.yudao.module.psychology.util.StudentCommentUtils.selectRandomComment(cfg.getComments());
                    if (sc != null && !sc.isEmpty()) { finalStudentComment = sc; resultPayload.put("studentComment", sc); }
                    Object sug = ev.getPayload().get("suggestion");
                    if (sug instanceof String && !((String) sug).isEmpty()) { finalTeacherComment = (String) sug; resultPayload.put("suggestion", sug); }
                    else if ((finalTeacherComment == null || finalTeacherComment.isEmpty()) && cfg.getSuggestions() != null && !cfg.getSuggestions().isEmpty()) finalTeacherComment = cfg.getSuggestions();
                    break;
                }
            }

            if (matchedAny) {
                ModuleResultDO m = new ModuleResultDO();
                m.setAssessmentTaskNo(taskNo);
                m.setUserId(userId);
                m.setScenarioSlotId(slot.getId());
                m.setSlotKey(slot.getSlotKey());
                if (finalModuleScore != null) m.setModuleScore(finalModuleScore);
                m.setRiskLevel(finalRiskLevel);
                m.setTeacherComment(finalTeacherComment);
                m.setStudentComment(finalStudentComment);
                m.setModuleDescription(finalDescription);
                try {
                    ObjectMapper om = new ObjectMapper();
                    m.setResultData(resultPayload.isEmpty() ? "{}" : om.writeValueAsString(resultPayload));
                } catch (Exception ignore) {}

                ModuleResultDO exist = moduleResultMapper.selectByTaskNoAndUserIdAndSlotId(taskNo, userId, slot.getId());
                if (exist == null) {
                    moduleResultMapper.insert(m);
                    logger.info("[模块][即时] 已保存(新增): taskNo={}, userId={}, slotId={}, riskLevel={}, level={}, teacherComment={}, studentComment={}, moduleScore={}, keys={}",
                            taskNo, userId, slot.getId(), finalRiskLevel, resultPayload.get("level"), finalTeacherComment, finalStudentComment, finalModuleScore, resultPayload.keySet());
                } else {
                    m.setId(exist.getId());
                    moduleResultMapper.updateById(m);
                    logger.info("[模块][即时] 已保存(更新): taskNo={}, userId={}, slotId={}, riskLevel={}, level={}, teacherComment={}, studentComment={}, moduleScore={}, keys={}",
                            taskNo, userId, slot.getId(), finalRiskLevel, resultPayload.get("level"), finalTeacherComment, finalStudentComment, finalModuleScore, resultPayload.keySet());
                }
            } else {
                logger.info("[模块][即时] 无命中: taskNo={}, userId={}, slotId={}, cfgCount={}", taskNo, userId, slot.getId(), moduleConfigs.size());
            }
        }
    }

    // 不再需要创建assessmentResult壳记录，模块结果基于(taskNo,userId,slot)去重

    /**
     * 收集参与测评计算的维度结果
     */
    private List<DimensionResultDO> collectParticipatingDimensions(List<AssessmentScenarioSlotDO> slots, Long userId, String taskNo) {
        List<DimensionResultDO> participatingDimensions = new ArrayList<>();
        Set<Long> seenDimensionIds = new HashSet<>();

        for (AssessmentScenarioSlotDO slot : slots) {
            // 解析插槽关联的问卷ID列表
            logger.info("[模块] 开始计算: taskNo={}, userId={}, slotId={}, slotKey={}", taskNo, userId, slot.getId(), slot.getSlotKey());
            List<Long> questionnaireIds = parseQuestionnaireIds(slot.getQuestionnaireIds());
            logger.info("[模块] 插槽问卷集合: raw='{}', parsed={}", slot.getQuestionnaireIds(), questionnaireIds);
            
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

        // 添加维度级别与风险等级统计（按维度编码）
        Map<String, String> dimensionLevels = dimensions.stream()
            .filter(d -> d.getDimensionCode() != null && d.getLevel() != null)
            .collect(Collectors.toMap(
                DimensionResultDO::getDimensionCode,
                DimensionResultDO::getLevel,
                (existing, replacement) -> existing
            ));
        Map<String, Integer> dimensionRiskLevels = dimensions.stream()
            .filter(d -> d.getDimensionCode() != null && d.getRiskLevel() != null)
            .collect(Collectors.toMap(
                DimensionResultDO::getDimensionCode,
                DimensionResultDO::getRiskLevel,
                (existing, replacement) -> existing
            ));

        for (Map.Entry<String, String> entry : dimensionLevels.entrySet()) {
            context.withVar("dimension_" + entry.getKey() + "_level", entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : dimensionRiskLevels.entrySet()) {
            context.withVar("dimension_" + entry.getKey() + "_riskLevel", entry.getValue());
        }

        logger.info("构建测评评估上下文完成: abnormalCount={}, riskLevelCounts={}", abnormalCount, riskLevelCounts);
        return context;
    }

    /**
     * 统一上下文构建：在基础统计的基础上，可选补齐 multi_linkage 的 main/other 变量与维度映射
     */
    private EvaluateContext buildUnifiedContext(List<DimensionResultDO> dimensions, Long userId, com.fasterxml.jackson.databind.JsonNode multiV2) {
        EvaluateContext ctx = buildAssessmentEvaluateContext(dimensions, userId);
        // 构建 code->DimensionResultDO 映射
        java.util.Map<String, DimensionResultDO> dimMap = new java.util.HashMap<>();
        for (DimensionResultDO d : dimensions) {
            if (d.getDimensionCode() != null && !dimMap.containsKey(d.getDimensionCode())) {
                dimMap.put(d.getDimensionCode(), d);
            }
        }
        ctx.getExt().put("slotDimensionMap", dimMap);

        if (multiV2 != null && multiV2.isObject()) {
            String mainDim = multiV2.has("mainDimension") ? multiV2.get("mainDimension").asText() : null;
            if (mainDim != null) {
                DimensionResultDO mainDR = dimMap.get(mainDim);
                if (mainDR != null && mainDR.getRiskLevel() != null) ctx.withVar("main.riskLevel", mainDR.getRiskLevel());
            }
            com.fasterxml.jackson.databind.JsonNode others = multiV2.get("otherDimensions");
            if (others != null && others.isArray()) {
                int o1 = 0, o2 = 0, o3 = 0, o4 = 0;
                for (com.fasterxml.jackson.databind.JsonNode o : others) {
                    String code = o.asText();
                    DimensionResultDO dr = dimMap.get(code);
                    if (dr == null || dr.getRiskLevel() == null) continue;
                    switch (dr.getRiskLevel()) {
                        case 1 -> o1++;
                        case 2 -> o2++;
                        case 3 -> o3++;
                        case 4 -> o4++;
                        default -> {}
                    }
                }
                ctx.withVar("other.riskLevel1Count", o1);
                ctx.withVar("other.riskLevel2Count", o2);
                ctx.withVar("other.riskLevel3Count", o3);
                ctx.withVar("other.riskLevel4Count", o4);
            }
        }
        return ctx;
    }

    /**
     * 针对每个场景插槽计算模块结果
     */
    private void calculateAndSaveModuleResults(List<AssessmentScenarioSlotDO> slots,
                                               List<DimensionResultDO> allParticipatingDimensions,
                                               AssessmentResultDO assessmentResult) {
        if (assessmentResult == null) {
            return;
        }
        String taskNo = assessmentResult.getTaskNo();
        Long ctxUserId = assessmentResult.getUserId();

        // 按插槽筛选维度并计算
        for (AssessmentScenarioSlotDO slot : slots) {
            // 读取该插槽的模块结果配置，若无配置则跳过，避免无谓计算
            List<ModuleResultConfigDO> moduleConfigs = moduleResultConfigMapper.selectByScenarioSlotId(slot.getId());
            if (moduleConfigs == null || moduleConfigs.isEmpty()) {
                continue;
            }

            List<Long> questionnaireIds = parseQuestionnaireIds(slot.getQuestionnaireIds());
            List<DimensionResultDO> slotDimensions = new ArrayList<>();

            // 选择属于该插槽的维度：来源问卷在该插槽questionnaireIds中
            if (questionnaireIds != null && !questionnaireIds.isEmpty()) {
                for (DimensionResultDO dr : allParticipatingDimensions) {
                    QuestionnaireResultDO qr = questionnaireResultMapper.selectById(dr.getQuestionnaireResultId());
                    if (qr != null && questionnaireIds.contains(qr.getQuestionnaireId())) {
                        slotDimensions.add(dr);
                    }
                }
            } else {
                // 未配置则使用全部参与维度
                slotDimensions = allParticipatingDimensions;
            }

            if (slotDimensions == null || slotDimensions.isEmpty()) {
                logger.info("[模块] 插槽无可用维度，跳过: slotId={}", slot.getId());
                continue;
            }

            // 先构建基础上下文（无 main/other），后续每条规则再按需要补齐
            EvaluateContext baseCtx = buildUnifiedContext(slotDimensions, ctxUserId, null);
            logger.info("[模块] 上下文构建完成: slotId={}, dims={}, userId={}", slot.getId(), slotDimensions.size(), ctxUserId);

            // 稳定顺序：按ID升序，确保优先级一致
            try {
                moduleConfigs.sort(java.util.Comparator.comparing(ModuleResultConfigDO::getId));
            } catch (Exception ignore) {}

            // 分段执行：综合(2) -> 等级(0) -> 评语(1)，保存到同一条模块结果
            Integer finalRiskLevel = null;
            String finalTeacherComment = null;
            String finalStudentComment = null;
            String finalDescription = null;
            java.util.Map<String, Object> resultPayload = new java.util.LinkedHashMap<>();
            boolean matchedAny = false;
            java.math.BigDecimal finalModuleScore = null;

            // 为lambda准备实际final的快照变量
            final Long userIdFinal = ctxUserId;
            final java.util.List<DimensionResultDO> slotDimsFinal = slotDimensions;
            final EvaluateContext baseCtxFinal = baseCtx;

            java.util.function.Function<ModuleResultConfigDO, EvaluateResult> evalFn = (cfg) -> {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    JsonNode json = mapper.readTree(cfg.getCalculateFormula());
                    String strategy = json.has("strategy") ? json.get("strategy").asText() : null;
                    if ("multi_linkage".equals(strategy)) {
                        JsonNode v2 = json.get("multiDimensionV2");
                        EvaluateContext ruleCtx = buildUnifiedContext(slotDimsFinal, userIdFinal, v2);
                        return multiDimensionLinkageEvaluator.evaluate(cfg.getCalculateFormula(), ruleCtx);
                    } else {
                        return defaultModuleResultStrategy.calculateModuleResult(cfg, baseCtxFinal);
                    }
                } catch (Exception e) {
                    logger.error("模块规则解析失败: configId={}", cfg.getId(), e);
                    return null;
                }
            };

            // 1) 综合规则（ruleType==2 或 null 视为综合），命中一条即采用其等级与评语（riskLevel + level + suggestion + comments）
            for (ModuleResultConfigDO cfg : moduleConfigs) {
                Integer rt = cfg.getRuleType();
                if (rt != null && rt != 2) continue;
                logger.info("[模块-综合] 开始评估: cfgId={}, ruleType={}, level={}, hasComments={}",
                        cfg.getId(), cfg.getRuleType(), cfg.getLevel(),
                        cfg.getComments() != null ? cfg.getComments().length() : null);
                EvaluateResult ev = evalFn.apply(cfg);
                logger.info("[模块-综合] 评估完成: cfgId={}, matched={}, payload={}",
                        cfg.getId(), ev != null ? ev.isMatched() : null, ev != null ? ev.getPayload() : null);
                if (ev != null && ev.isMatched()) {
                    matchedAny = true;
                    if (ev.getPayload().get("riskLevel") instanceof Number) finalRiskLevel = ((Number) ev.getPayload().get("riskLevel")).intValue();
                    // moduleScore
                    Object ms = ev.getPayload().get("moduleScore");
                    if (ms instanceof Number) {
                        finalModuleScore = new java.math.BigDecimal(((Number) ms).toString());
                    } else if (ms instanceof String) {
                        try { finalModuleScore = new java.math.BigDecimal((String) ms); } catch (Exception ignore) {}
                    }
                    // level
                    resultPayload.put("level", cfg.getLevel());
                    // 建议（teacherComment）：payload.suggestion 优先，否则配置 suggestions
                    Object sug = ev.getPayload().get("suggestion");
                    if (sug instanceof String && !((String) sug).isEmpty()) {
                        finalTeacherComment = (String) sug;
                    } else if (cfg.getSuggestions() != null && !cfg.getSuggestions().isEmpty()) {
                        finalTeacherComment = cfg.getSuggestions();
                    }
                    // 评语（studentComment）：从配置 comments 随机一条
                    String sc = cn.iocoder.yudao.module.psychology.util.StudentCommentUtils.selectRandomComment(cfg.getComments());
                    if (sc != null && !sc.isEmpty()) finalStudentComment = sc;
                    if (finalStudentComment != null && !finalStudentComment.isEmpty()) {
                        resultPayload.put("studentComment", finalStudentComment);
                    }
                    // 描述
                    if (ev.getPayload().get("description") instanceof String) finalDescription = (String) ev.getPayload().get("description");
                    if (finalDescription == null || finalDescription.isEmpty()) finalDescription = cfg.getDescription();
                    resultPayload.putAll(ev.getPayload());
                    break;
                }
            }

            // 2) 等级规则（ruleType==0），只保存 riskLevel 与 level（不影响描述与评语）
            for (ModuleResultConfigDO cfg : moduleConfigs) {
                if (cfg.getRuleType() == null || cfg.getRuleType() != 0) continue;
                logger.info("[模块-等级] 开始评估: cfgId={}, level={}", cfg.getId(), cfg.getLevel());
                EvaluateResult ev = evalFn.apply(cfg);
                logger.info("[模块-等级] 评估完成: cfgId={}, matched={}, payload={}",
                        cfg.getId(), ev != null ? ev.isMatched() : null, ev != null ? ev.getPayload() : null);
                if (ev != null && ev.isMatched()) {
                    matchedAny = true;
                    if (finalRiskLevel == null && ev.getPayload().get("riskLevel") instanceof Number) {
                        finalRiskLevel = ((Number) ev.getPayload().get("riskLevel")).intValue();
                    }
                    if (!resultPayload.containsKey("level")) {
                        resultPayload.put("level", cfg.getLevel());
                    }
                    if (finalRiskLevel != null) {
                        resultPayload.put("riskLevel", finalRiskLevel);
                    }
                    // moduleScore（若等级规则也提供）
                    if (ev.getPayload().get("moduleScore") instanceof Number) {
                        finalModuleScore = new java.math.BigDecimal(((Number) ev.getPayload().get("moduleScore")).toString());
                    }
                    break; // 等级命中一条即止
                }
            }

            // 3) 评语规则（ruleType==1），只保存 suggestion 与 comments（命中一条即止）
            for (ModuleResultConfigDO cfg : moduleConfigs) {
                if (cfg.getRuleType() == null || cfg.getRuleType() != 1) continue;
                logger.info("[模块-评语] 开始评估: cfgId={} hasSuggestions={} hasComments={}",
                        cfg.getId(),
                        cfg.getSuggestions() != null && !cfg.getSuggestions().isEmpty(),
                        cfg.getComments() != null ? cfg.getComments().length() : null);
                EvaluateResult ev = evalFn.apply(cfg);
                logger.info("[模块-评语] 评估完成: cfgId={}, matched={}, payload={}",
                        cfg.getId(), ev != null ? ev.isMatched() : null, ev != null ? ev.getPayload() : null);
                if (ev != null && ev.isMatched()) {
                    matchedAny = true;
                    String sc = cn.iocoder.yudao.module.psychology.util.StudentCommentUtils.selectRandomComment(cfg.getComments());
                    if (sc != null && !sc.isEmpty()) finalStudentComment = sc;
                    Object sug = ev.getPayload().get("suggestion");
                    if (sug instanceof String && !((String) sug).isEmpty()) {
                        finalTeacherComment = (String) sug;
                    } else if ((finalTeacherComment == null || finalTeacherComment.isEmpty()) && cfg.getSuggestions() != null && !cfg.getSuggestions().isEmpty()) {
                        finalTeacherComment = cfg.getSuggestions();
                    }
                    // 可将 suggestion 记录到 resultData 便于审计
                    if (sug instanceof String && !((String) sug).isEmpty()) {
                        resultPayload.put("suggestion", sug);
                    }
                    if (finalStudentComment != null && !finalStudentComment.isEmpty()) {
                        resultPayload.put("studentComment", finalStudentComment);
                    }
                    break; // 评语命中一条即止
                }
            }

            // 命中过任意一段即保存
            if (matchedAny) {
                ModuleResultDO m = new ModuleResultDO();
                m.setAssessmentTaskNo(taskNo);
                m.setUserId(ctxUserId);
                m.setScenarioSlotId(slot.getId());
                m.setSlotKey(slot.getSlotKey());
                if (finalModuleScore != null) m.setModuleScore(finalModuleScore);
                m.setRiskLevel(finalRiskLevel);
                m.setTeacherComment(finalTeacherComment);
                m.setStudentComment(finalStudentComment);
                m.setModuleDescription(finalDescription);
                try {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    if (resultPayload.isEmpty()) {
                        m.setResultData("{}");
                    } else {
                        m.setResultData(om.writeValueAsString(resultPayload));
                    }
                } catch (Exception ignore) {}

                ModuleResultDO exist = moduleResultMapper.selectByTaskNoAndUserIdAndSlotId(taskNo, ctxUserId, slot.getId());
                if (exist == null) {
                    moduleResultMapper.insert(m);
                    logger.info("模块结果已保存(新增): taskNo={}, userId={}, slotId={}, riskLevel={}, level={}, teacherComment={}, studentComment={}, moduleScore={}, resultDataKeys={}",
                            taskNo, ctxUserId, slot.getId(), finalRiskLevel, resultPayload.get("level"), finalTeacherComment, finalStudentComment, finalModuleScore,
                            resultPayload.keySet());
                } else {
                    m.setId(exist.getId());
                    moduleResultMapper.updateById(m);
                    logger.info("模块结果已保存(更新): taskNo={}, userId={}, slotId={}, riskLevel={}, level={}, teacherComment={}, studentComment={}, moduleScore={}, resultDataKeys={}",
                            taskNo, ctxUserId, slot.getId(), finalRiskLevel, resultPayload.get("level"), finalTeacherComment, finalStudentComment, finalModuleScore,
                            resultPayload.keySet());
                }
            } else {
                logger.info("模块结果无命中: taskNo={}, userId={}, slotId={}, moduleConfigs={}", taskNo, ctxUserId, slot.getId(), moduleConfigs != null ? moduleConfigs.size() : 0);
            }
        }
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
            } else if (jsonRule.has("strategy") && "multi_linkage".equals(jsonRule.get("strategy").asText())) {
                // 新版多维度联动策略（测评层）
                // 准备 main/other 变量：从 multiDimensionV2 提取 mainDimension 与 otherDimensions
                try {
                    JsonNode v2 = jsonRule.get("multiDimensionV2");
                    if (v2 != null) {
                        String mainDim = v2.get("mainDimension").asText();
                        // 建立 code->DimensionResultDO 映射
                        java.util.Map<String, DimensionResultDO> dimMap = new java.util.HashMap<>();
                        for (DimensionResultDO d : participatingDimensions) {
                            if (d.getDimensionCode() != null && !dimMap.containsKey(d.getDimensionCode())) {
                                dimMap.put(d.getDimensionCode(), d);
                            }
                        }
                        DimensionResultDO mainDR = dimMap.get(mainDim);
                        if (mainDR != null && mainDR.getRiskLevel() != null) {
                            context.withVar("main.riskLevel", mainDR.getRiskLevel());
                        }

                        int o1 = 0, o2 = 0, o3 = 0, o4 = 0;
                        JsonNode others = v2.get("otherDimensions");
                        if (others != null && others.isArray()) {
                            for (JsonNode o : others) {
                                String code = o.asText();
                                DimensionResultDO dr = dimMap.get(code);
                                if (dr == null || dr.getRiskLevel() == null) continue;
                                switch (dr.getRiskLevel()) {
                                    case 1 -> o1++;
                                    case 2 -> o2++;
                                    case 3 -> o3++;
                                    case 4 -> o4++;
                                    default -> { }
                                }
                            }
                        }
                        context.withVar("other.riskLevel1Count", o1);
                        context.withVar("other.riskLevel2Count", o2);
                        context.withVar("other.riskLevel3Count", o3);
                        context.withVar("other.riskLevel4Count", o4);
                    }
                } catch (Exception ignore) { }
                result = multiDimensionLinkageEvaluator.evaluate(config.getCalculateFormula(), context);
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
                    // JSON 的 riskLevel 同时写入 riskLevel 和 combinedRiskLevel
                    try {
                        Integer r = (Integer) payload.get("riskLevel");
                        assessmentResult.setRiskLevel(r);
                        assessmentResult.setCombinedRiskLevel(r);
                    } catch (Exception ignore) {}
                }

                if (payload.containsKey("description")) {
                    assessmentResult.setSuggestion((String) payload.get("description"));
                } else {
                    assessmentResult.setSuggestion(config.getDescription());
                }

                assessmentResult.setSuggestions(config.getSuggestions());
                assessmentResult.setComment(config.getComment());

                // 将参与测评计算的维度结果保存到 questionnaireResults 字段
                try {
                    List<Map<String, Object>> dimensionList = new ArrayList<>();
                    for (DimensionResultDO dim : participatingDimensions) {
                        Map<String, Object> dimData = new LinkedHashMap<>();
                        dimData.put("id", dim.getId());
                        dimData.put("dimensionId", dim.getDimensionId());
                        dimData.put("dimensionCode", dim.getDimensionCode());
                        dimData.put("score", dim.getScore());
                        dimData.put("isAbnormal", dim.getIsAbnormal());
                        dimData.put("riskLevel", dim.getRiskLevel());
                        dimData.put("level", dim.getLevel());
                        dimData.put("teacherComment", dim.getTeacherComment());
                        dimData.put("studentComment", dim.getStudentComment());
                        dimData.put("description", dim.getDescription());

                        // 获取维度名称
                        try {
                            QuestionnaireDimensionRespVO dimVO = questionnaireDimensionService.getDimension(dim.getDimensionId());
                            if (dimVO != null) {
                                dimData.put("dimensionName", dimVO.getDimensionName());
                            }
                        } catch (Exception ignore) {}

                        dimensionList.add(dimData);
                    }

                    if (!dimensionList.isEmpty()) {
                        ObjectMapper jsonMapper = new ObjectMapper();
                        assessmentResult.setQuestionnaireResults(jsonMapper.writeValueAsString(dimensionList));
                        logger.info("已保存{}个维度结果到测评结果 questionnaireResults 字段", dimensionList.size());
                    }
                } catch (Exception e) {
                    logger.error("序列化维度结果失败", e);
                }

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
