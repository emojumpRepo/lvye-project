package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.result.AssessmentResultDetailRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.result.RiskLevelInterventionVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentScenarioDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentResultMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentScenarioMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.QuestionnaireResultVO;
import cn.iocoder.yudao.module.psychology.rule.executor.ExpressionExecutor;
import cn.iocoder.yudao.module.psychology.util.RiskLevelUtils;

import java.util.ArrayList;
import java.util.List;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorFactory;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.AssessmentResultVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireDimensionService;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.DimensionResultMapper;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.QuestionnaireResultVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 组合测评结果保存服务实现
 */
@Slf4j
@Service
public class AssessmentResultServiceImpl implements AssessmentResultService {

    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;
    @Resource
    private AssessmentUserTaskMapper assessmentUserTaskMapper;
    @Resource
    private AssessmentResultMapper assessmentResultMapper;
    @Resource
    private AssessmentTaskMapper assessmentTaskMapper;
    @Resource
    private AssessmentScenarioMapper assessmentScenarioMapper;
    @Resource
    private QuestionnaireMapper questionnaireMapper;
    @Resource
    private StudentProfileMapper studentProfileMapper;
    @Resource
    private ResultGeneratorFactory resultGeneratorFactory;
    @Resource
    private ExpressionExecutor expressionExecutor; // 预留用于模块/测评层的表驱动规则执行
    @Resource
    private QuestionnaireDimensionService questionnaireDimensionService;
    @Resource
    private DimensionResultMapper dimensionResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateAndSaveCombinedResult(String taskNo, Long studentProfileId) {
        // 1) 通过studentProfileId获取userId
        StudentProfileDO studentProfile = studentProfileMapper.selectById(studentProfileId);
        if (studentProfile == null) {
            log.warn("未找到学生档案, studentProfileId={}", studentProfileId);
            return null;
        }
        Long userId = studentProfile.getUserId();

        // 2) 拉取该任务下该学生的全部问卷结果
        // 注意：lvye_questionnaire_result表中存储的是userId，不是studentProfileId
        List<QuestionnaireResultDO> resultDOList = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, userId);
        log.info("查询问卷结果, taskNo={}, studentProfileId={}, userId={}, 查询到{}条记录",
                 taskNo, studentProfileId, userId, resultDOList != null ? resultDOList.size() : 0);

        if (resultDOList != null && !resultDOList.isEmpty()) {
            log.info("问卷结果详情:");
            for (int i = 0; i < resultDOList.size(); i++) {
                QuestionnaireResultDO result = resultDOList.get(i);
                log.info("  [{}] ID={}, 问卷ID={}, 用户ID={}, 任务编号={}, 风险等级={}, 生成状态={}",
                         i + 1, result.getId(), result.getQuestionnaireId(), result.getUserId(),
                         result.getAssessmentTaskNo(), result.getRiskLevel(), result.getGenerationStatus());
            }
        }

        if (CollUtil.isEmpty(resultDOList)) {
            log.info("无问卷结果可用于生成测评结果, taskNo={}, studentProfileId={}", taskNo, studentProfileId);
            return null;
        }

        // 组装问卷结果 VO 列表
        List<QuestionnaireResultVO> questionnaireResults = new ArrayList<>();
        for (QuestionnaireResultDO r : resultDOList) {
            Map<String, BigDecimal> dimScores = null;
            if (r.getDimensionScores() != null) {
                dimScores = JsonUtils.parseObjectQuietly(r.getDimensionScores(), new TypeReference<Map<String, BigDecimal>>() {});
            }
            
            // 解析维度异常状态（从resultData中提取）
            Map<String, Boolean> dimAbnormalStatus = null;
            if (r.getResultData() != null) {
                try {
                    // 输出resultData用于调试
                    String preview = r.getResultData().length() > 300 ? 
                        r.getResultData().substring(0, 300) + "..." : r.getResultData();
                    log.info("问卷ID={} resultData预览: {}", r.getQuestionnaireId(), preview);
                    
                    // resultData 可能是数组格式
                    if (r.getResultData().trim().startsWith("[")) {
                        // 解析为数组
                        List<Map<String, Object>> rawDataList = JsonUtils.parseObjectQuietly(
                            r.getResultData(), new TypeReference<List<Map<String, Object>>>() {}
                        );
                        
                        if (rawDataList != null && !rawDataList.isEmpty()) {
                            dimAbnormalStatus = new HashMap<>();
                            int totalAbnormal = 0;
                            
                            for (Map<String, Object> dimension : rawDataList) {
                                String dimensionName = (String) dimension.get("dimensionName");
                                Object isAbnormalObj = dimension.get("isAbnormal");
                                
                                if (dimensionName != null && isAbnormalObj != null) {
                                    Boolean isAbnormal = null;
                                    if (isAbnormalObj instanceof Boolean) {
                                        isAbnormal = (Boolean) isAbnormalObj;
                                    } else if (isAbnormalObj instanceof Integer) {
                                        isAbnormal = ((Integer) isAbnormalObj) == 1;
                                    } else if (isAbnormalObj instanceof String) {
                                        isAbnormal = "1".equals(isAbnormalObj);
                                    }
                                    
                                    if (isAbnormal != null) {
                                        if (dimensionName == null || dimensionName.isEmpty()) {
                                            // 兜底命名，防止缺失名称导致统计为0
                                            dimensionName = "dim_" + (dimAbnormalStatus.size() + 1);
                                        }
                                        dimAbnormalStatus.put(dimensionName, isAbnormal);
                                        if (isAbnormal) {
                                            totalAbnormal++;
                                        }
                                        log.info("维度 [{}] 异常状态: isAbnormal={}", dimensionName, isAbnormal);
                                    }
                                }
                            }
                            
                            // 添加整体异常判断（如果有任何维度异常，则整体异常）
                            dimAbnormalStatus.put("overall", totalAbnormal > 0);
                            log.info("问卷ID={} 解析到{}个维度，{}个维度异常，整体异常状态={}", 
                                r.getQuestionnaireId(), rawDataList.size(), totalAbnormal, totalAbnormal > 0);
                        }
                    } else {
                        // 尝试解析为对象格式（兼容其他可能的格式）
                        Map<String, Object> resultDataMap = JsonUtils.parseObjectQuietly(
                            r.getResultData(), 
                            new TypeReference<Map<String, Object>>() {}
                        );
                        
                        if (resultDataMap != null) {
                            // 直接获取根级别的 isAbnormal 字段
                            Object isAbnormalObj = resultDataMap.get("isAbnormal");
                            if (isAbnormalObj != null) {
                                Boolean questionnaireAbnormal = null;
                                if (isAbnormalObj instanceof Boolean) {
                                    questionnaireAbnormal = (Boolean) isAbnormalObj;
                                } else if (isAbnormalObj instanceof Integer) {
                                    questionnaireAbnormal = ((Integer) isAbnormalObj) == 1;
                                } else if (isAbnormalObj instanceof String) {
                                    questionnaireAbnormal = "1".equals(isAbnormalObj);
                                }
                                
                                if (questionnaireAbnormal != null) {
                                    dimAbnormalStatus = new HashMap<>();
                                    dimAbnormalStatus.put("overall", questionnaireAbnormal);
                                    log.info("问卷ID={} 整体异常状态(从对象格式解析): isAbnormal={}", 
                                        r.getQuestionnaireId(), questionnaireAbnormal);
                                }
                            }
                        }
                    }
                    
                    if (dimAbnormalStatus == null || dimAbnormalStatus.isEmpty()) {
                        log.warn("未能解析异常状态，问卷ID={}，resultData可能格式不同", r.getQuestionnaireId());
                    }
                } catch (Exception e) {
                    log.error("解析异常状态失败, questionnaireResultId={}, questionnaireId={}", 
                        r.getId(), r.getQuestionnaireId(), e);
                }
            } else {
                log.warn("问卷ID={} 的 resultData 为空", r.getQuestionnaireId());
            }
            
            QuestionnaireResultVO vo = QuestionnaireResultVO.builder()
                    .questionnaireId(r.getQuestionnaireId())
                    .rawScore(r.getScore() == null ? BigDecimal.ZERO : r.getScore())
                    .standardScore(r.getScore()) // 现有 DO 仅有 score 字段，这里先用作标准分占位
                    .riskLevel(r.getRiskLevel())
                    .levelDescription(r.getEvaluate())
                    .dimensionScores(dimScores)
                    .dimensionAbnormalStatus(dimAbnormalStatus)  // 添加维度异常状态
                    .reportContent(r.getResultData())
                    .suggestions(r.getSuggestions())
                    .build();
            questionnaireResults.add(vo);
        }

        // 获取参与者记录，以便拿到任务信息
        AssessmentUserTaskDO userTask = assessmentUserTaskMapper.selectByTaskNoAndUserId(taskNo, userId);
        // participantId 使用学生档案ID
        Long participantId = studentProfileId;

        // 查询场景编码
        String scenarioCode = null;
        Long scenarioId = null;
        AssessmentTaskDO assessmentTask = assessmentTaskMapper.selectByTaskNo(taskNo);
        if (assessmentTask != null && assessmentTask.getScenarioId() != null) {
            scenarioId = assessmentTask.getScenarioId();
            AssessmentScenarioDO scenario = assessmentScenarioMapper.selectById(assessmentTask.getScenarioId());
            if (scenario != null) {
                scenarioCode = scenario.getCode();
                log.info("测评任务关联场景: taskNo={}, scenarioId={}, scenarioCode={}", 
                        taskNo, scenarioId, scenarioCode);
            }
        }

        // 构建问卷ID到编码的映射
        Map<Long, String> questionnaireCodeMap = new HashMap<>();
        List<Long> questionnaireIds = resultDOList.stream()
                .map(QuestionnaireResultDO::getQuestionnaireId)
                .distinct()
                .collect(Collectors.toList());
        
        if (!questionnaireIds.isEmpty()) {
            List<QuestionnaireDO> questionnaires = questionnaireMapper.selectListByIds(questionnaireIds);
            for (QuestionnaireDO questionnaire : questionnaires) {
                if (questionnaire.getSurveyCode() != null) {
                    questionnaireCodeMap.put(questionnaire.getId(), questionnaire.getSurveyCode());
                    log.info("问卷编码映射: questionnaireId={}, surveyCode={}", 
                            questionnaire.getId(), questionnaire.getSurveyCode());
                }
            }
        }

        // 2) 调用生成器生成组合测评结果
        ResultGenerationContext context = ResultGenerationContext.builder()
                .generationType(ResultGeneratorTypeEnum.COMBINED_ASSESSMENT)
                .assessmentId(userTask != null ? userTask.getId() : null) // 暂以参与者任务ID代替assessmentId概念
                .userId(userId) // 这里使用系统用户ID
                .studentProfileId(participantId) // 传递学生档案ID用于场景化服务
                .questionnaireResults(questionnaireResults)
                .scenarioCode(scenarioCode)  // 传递场景编码
                .scenarioId(scenarioId)      // 传递场景ID，触发场景化计算路径
                .questionnaireCodeMap(questionnaireCodeMap)  // 传递问卷编码映射
                .taskNo(taskNo)              // 传递本次测评任务编号
                .build();
        AssessmentResultVO resultVO = resultGeneratorFactory.generateResult(ResultGeneratorTypeEnum.COMBINED_ASSESSMENT, context);

        // 3) 将 VO 映射到 DO（综合报告由生成器统一拼接 evaluate_config 结果）
        AssessmentResultDO save = AssessmentResultDO.builder()
                .participantId(participantId)
                .taskNo(taskNo)  // 添加taskNo字段
                .dimensionCode("total")
                .score(resultVO.getCombinedScore() == null ? null : resultVO.getCombinedScore().intValue())
                .combinedRiskLevel(resultVO.getCombinedRiskLevel())
                .suggestion(resultVO.getComprehensiveReport())
                .questionnaireResults(JsonUtils.toJsonString(resultVO.getQuestionnaireResults()))
                .riskFactors(JsonUtils.toJsonString(resultVO.getRiskFactors()))
                .interventionSuggestions(JsonUtils.toJsonString(resultVO.getInterventionSuggestions()))
                .generationConfigVersion(context.getConfigVersion())
                .build();

        // 4) 幂等保存：根据 (taskNo, participantId, dimensionCode) 存在则更新，否则插入
        // 为防止并发插入，使用synchronized同步块确保同一taskNo+participantId+dimensionCode组合只能有一个线程操作
        String lockKey = taskNo + "_" + participantId + "_" + save.getDimensionCode();
        synchronized (lockKey.intern()) {
            List<AssessmentResultDO> existList = assessmentResultMapper.selectList(
                new LambdaQueryWrapperX<AssessmentResultDO>()
                    .eq(AssessmentResultDO::getTaskNo, taskNo)
                    .eq(AssessmentResultDO::getParticipantId, participantId)
                    .eq(AssessmentResultDO::getDimensionCode, save.getDimensionCode())
            );

            log.info("找到{}条重复的测评结果记录", existList.size());

            if (CollUtil.isEmpty(existList)) {
                // 不存在记录，插入新记录
                assessmentResultMapper.insert(save);
                log.info("已保存组合测评结果(新增), taskNo={}, participantId={}, id={}", taskNo, participantId, save.getId());
            } else {
                // 存在记录，更新第一条，删除其他重复记录
                AssessmentResultDO exist = existList.get(0);
                save.setId(exist.getId());
                assessmentResultMapper.updateById(save);
                log.info("已保存组合测评结果(更新), taskNo={}, participantId={}, id={}", taskNo, participantId, save.getId());

                // 如果存在多条重复记录，删除其他记录
                if (existList.size() > 1) {
                    log.warn("发现{}条重复的测评结果记录, taskNo={}, participantId={}, dimensionCode={}, 将删除多余记录",
                        existList.size(), taskNo, participantId, save.getDimensionCode());
                    for (int i = 1; i < existList.size(); i++) {
                        assessmentResultMapper.deleteById(existList.get(i).getId());
                        log.info("已删除重复的测评结果记录, id={}", existList.get(i).getId());
                    }
                }
            }
        }
        
        // 5) 更新user_task表的risk_level字段
        updateUserTaskRiskLevel(taskNo, userId, resultVO.getCombinedRiskLevel());
        
        return save.getId();
    }

    @Override
    public AssessmentResultDetailRespVO getAssessmentResult(Long id) {
        // 1. 根据ID查询测评结果基本信息
        AssessmentResultDO assessmentResult = assessmentResultMapper.selectById(id);

        if (assessmentResult == null) {
            log.warn("未找到测评结果, id={}", id);
            return null;
        }

        // 2. 构建响应VO
        AssessmentResultDetailRespVO respVO = new AssessmentResultDetailRespVO();
        respVO.setId(assessmentResult.getId());
        respVO.setParticipantId(assessmentResult.getParticipantId());
        respVO.setTaskNo(assessmentResult.getTaskNo());
        respVO.setDimensionCode(assessmentResult.getDimensionCode());
        respVO.setScore(assessmentResult.getScore());
        respVO.setSuggestion(assessmentResult.getSuggestion());
        respVO.setCombinedRiskLevel(assessmentResult.getCombinedRiskLevel());
        respVO.setRiskLevelDescription(getRiskLevelDescription(assessmentResult.getCombinedRiskLevel()));
        respVO.setRiskFactors(assessmentResult.getRiskFactors());
        respVO.setInterventionSuggestions(assessmentResult.getInterventionSuggestions());
        respVO.setGenerationConfigVersion(assessmentResult.getGenerationConfigVersion());

        // 生成当前风险等级的结构化干预建议
        respVO.setRiskLevelIntervention(generateCurrentRiskLevelIntervention(assessmentResult.getCombinedRiskLevel()));
        respVO.setCreateTime(assessmentResult.getCreateTime());
        respVO.setUpdateTime(assessmentResult.getUpdateTime());

        // 查询并设置场景名称
        if (assessmentResult.getTaskNo() != null) {
            AssessmentTaskDO assessmentTask = assessmentTaskMapper.selectByTaskNo(assessmentResult.getTaskNo());
            if (assessmentTask != null && assessmentTask.getScenarioId() != null) {
                AssessmentScenarioDO scenario = assessmentScenarioMapper.selectById(assessmentTask.getScenarioId());
                if (scenario != null) {
                    respVO.setScenarioName(scenario.getName());
                    log.info("获取场景名称成功, taskNo={}, scenarioId={}, scenarioName={}",
                        assessmentResult.getTaskNo(), assessmentTask.getScenarioId(), scenario.getName());
                }
            }
        }

        // 3. 解析问卷结果JSON并添加问卷名称和答题结果
        List<AssessmentResultDetailRespVO.QuestionnaireResultDetailVO> questionnaireResults = new ArrayList<>();

        // 首先获取userId用于查询原始问卷结果
        StudentProfileDO studentProfile = studentProfileMapper.selectById(assessmentResult.getParticipantId());
        Long userId = studentProfile != null ? studentProfile.getUserId() : null;

        if (assessmentResult.getQuestionnaireResults() != null && userId != null) {
            try {
                List<QuestionnaireResultVO> resultVOList = JsonUtils.parseArray(
                    assessmentResult.getQuestionnaireResults(), QuestionnaireResultVO.class);

                for (QuestionnaireResultVO resultVO : resultVOList) {
                    AssessmentResultDetailRespVO.QuestionnaireResultDetailVO detailVO =
                        new AssessmentResultDetailRespVO.QuestionnaireResultDetailVO();

                    detailVO.setQuestionnaireId(resultVO.getQuestionnaireId());
                    detailVO.setRawScore(resultVO.getRawScore());
                    detailVO.setStandardScore(resultVO.getStandardScore());
                    detailVO.setRiskLevel(resultVO.getRiskLevel());
                    detailVO.setLevelDescription(resultVO.getLevelDescription());
                    detailVO.setSuggestions(resultVO.getSuggestions());
                    detailVO.setReportContent(resultVO.getReportContent());
                    detailVO.setPercentileRank(resultVO.getPercentileRank());

                    // 转换维度得分为JSON字符串
                    if (resultVO.getDimensionScores() != null) {
                        detailVO.setDimensionScores(JsonUtils.toJsonString(resultVO.getDimensionScores()));
                    }

                    // 获取问卷名称
                    QuestionnaireDO questionnaire = questionnaireMapper.selectById(resultVO.getQuestionnaireId());
                    if (questionnaire != null) {
                        detailVO.setQuestionnaireName(questionnaire.getTitle());
                    } else {
                        detailVO.setQuestionnaireName("未知问卷");
                    }

                    // 查询原始问卷结果获取答题详情
                    List<QuestionnaireResultDO> originalResults = questionnaireResultMapper.selectListByTaskNoAndUserId(assessmentResult.getTaskNo(), userId);
                    for (QuestionnaireResultDO originalResult : originalResults) {
                        if (originalResult.getQuestionnaireId().equals(resultVO.getQuestionnaireId())) {
                            detailVO.setAnswers(originalResult.getAnswers());
                            detailVO.setResultId(originalResult.getId());
                            detailVO.setCompletedTime(originalResult.getCompletedTime());
                            detailVO.setGenerationStatus(originalResult.getGenerationStatus());
                            detailVO.setGenerationStatusDescription(getGenerationStatusDescription(originalResult.getGenerationStatus()));

                            // 直接查询维度结果表，返回结构化维度明细
                            try {
                                java.util.List<DimensionResultDO> dimList =
                                    dimensionResultMapper.selectListByQuestionnaireResultId(originalResult.getId());
                                if (dimList != null && !dimList.isEmpty()) {
                                    java.util.List<AssessmentResultDetailRespVO.DimensionDetailVO> dimsVO = new java.util.ArrayList<>();
                                    for (DimensionResultDO dr : dimList) {
                                        AssessmentResultDetailRespVO.DimensionDetailVO dvo = new AssessmentResultDetailRespVO.DimensionDetailVO();
                                        dvo.setDimensionId(dr.getDimensionId());
                                        // 名称
                                        String dimName = null;
                                        String dimDesc = null;
                                        try {
                                            var dimVO = questionnaireDimensionService.getDimension(dr.getDimensionId());
                                            if (dimVO != null) {
                                                dimName = dimVO.getDimensionName();
                                                dimDesc = dimVO.getDescription();
                                            }
                                        } catch (Exception ignore) {}
                                        if (dimName == null || dimName.isEmpty()) {
                                            dimName = dr.getDimensionCode() != null ? dr.getDimensionCode() : ("DIM-" + dr.getDimensionId());
                                        }
                                        dvo.setName(dimName);
                                        dvo.setScore(dr.getScore());
                                        dvo.setIsAbnormal(dr.getIsAbnormal());
                                        dvo.setRiskLevel(dr.getRiskLevel());
                                        dvo.setLevel(dr.getLevel());
                                        dvo.setTeacherComment(dr.getTeacherComment());
                                        dvo.setStudentComment(dr.getStudentComment());
                                        dvo.setDescription(dimDesc);
                                        dimsVO.add(dvo);
                                    }
                                    detailVO.setDimensions(dimsVO);
                                }
                            } catch (Exception e) {
                                log.warn("查询维度结果失败, questionnaireResultId={}", originalResult.getId(), e);
                            }
                            break;
                        }
                    }
                    questionnaireResults.add(detailVO);
                }
            } catch (Exception e) {
                log.error("解析问卷结果JSON失败, id={}", id, e);
            }
        }

        respVO.setQuestionnaireResults(questionnaireResults);

        log.info("获取测评结果详情成功, id={}, 包含{}个问卷结果",
            id, questionnaireResults.size());
        return respVO;
    }

    /**
     * 生成当前风险等级的结构化干预建议
     */
    private RiskLevelInterventionVO generateCurrentRiskLevelIntervention(Integer currentRiskLevel) {
        List<RiskLevelInterventionVO> interventions = RiskLevelInterventionVO.getStandardInterventions();

        // 找到当前风险等级对应的干预建议
        RiskLevelInterventionVO currentIntervention = interventions.stream()
            .filter(intervention -> intervention.getRiskLevel().equals(currentRiskLevel))
            .findFirst()
            .orElse(null);

        if (currentIntervention != null) {
            currentIntervention.setIsCurrent(true);
        }

        return currentIntervention;
    }

    /**
     * 获取风险等级描述
     */
    private String getRiskLevelDescription(Integer riskLevel) {
        // 使用字典获取风险等级名称
        return RiskLevelUtils.getRiskLevelNameFromDict(riskLevel);
    }

    /**
     * 获取生成状态描述
     */
    private String getGenerationStatusDescription(Integer generationStatus) {
        if (generationStatus == null) {
            return "未知";
        }
        switch (generationStatus) {
            case 0:
                return "待生成";
            case 1:
                return "生成中";
            case 2:
                return "已生成";
            case 3:
                return "生成失败";
            default:
                return "未知";
        }
    }
    
    /**
     * 更新用户任务表的风险等级
     */
    private void updateUserTaskRiskLevel(String taskNo, Long userId, Integer riskLevel) {
        if (taskNo == null || userId == null || riskLevel == null) {
            return;
        }
        
        AssessmentUserTaskDO userTask = assessmentUserTaskMapper.selectOne(
            new LambdaQueryWrapperX<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .eq(AssessmentUserTaskDO::getUserId, userId)
        );
        
        if (userTask != null) {
            userTask.setRiskLevel(riskLevel);
            assessmentUserTaskMapper.updateById(userTask);
            log.info("已更新用户任务风险等级, taskNo={}, userId={}, riskLevel={}", taskNo, userId, riskLevel);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAssessmentResults(String taskNo) {
        // 调用带userIds参数的方法，传null表示计算所有用户
        recalculateAssessmentResults(taskNo, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAssessmentResults(String taskNo, List<Long> userIds) {
        log.info("开始重新计算测评任务结果, taskNo={}, 指定用户数={}",
            taskNo, userIds != null ? userIds.size() : "全部");

        // 1. 查找该测评任务下的所有问卷结果
        List<QuestionnaireResultDO> allResults = questionnaireResultMapper.selectListByTaskNo(taskNo);

        log.info("找到{}条问卷结果记录", allResults.size());

        if (CollUtil.isEmpty(allResults)) {
            log.warn("未找到测评任务的问卷结果, taskNo={}", taskNo);
            return;
        }

        // 2. 如果指定了用户ID，则只保留这些用户的结果
        if (userIds != null && !userIds.isEmpty()) {
            allResults = allResults.stream()
                .filter(result -> userIds.contains(result.getUserId()))
                .collect(Collectors.toList());
            log.info("过滤后剩余{}条问卷结果记录（仅指定用户）", allResults.size());

            if (allResults.isEmpty()) {
                log.warn("指定的用户在该任务下没有问卷结果, taskNo={}, userIds={}", taskNo, userIds);
                return;
            }
        }

        // 3. 按用户分组，为每个用户重新计算结果
        Map<Long, List<QuestionnaireResultDO>> userResultsMap = allResults.stream()
            .collect(Collectors.groupingBy(QuestionnaireResultDO::getUserId));

        int successCount = 0;
        int errorCount = 0;

        for (Map.Entry<Long, List<QuestionnaireResultDO>> entry : userResultsMap.entrySet()) {
            Long userId = entry.getKey();
            List<QuestionnaireResultDO> userResults = entry.getValue();

            try {
                log.info("开始重新计算用户{}的结果，包含{}个问卷", userId, userResults.size());

                // 3.1 获取学生档案ID
                List<StudentProfileDO> studentProfiles = studentProfileMapper.selectList(
                    new LambdaQueryWrapperX<StudentProfileDO>()
                        .eq(StudentProfileDO::getUserId, userId)
                        .orderByDesc(StudentProfileDO::getCreateTime)
                        .last("LIMIT 1")
                );

                if (CollUtil.isEmpty(studentProfiles)) {
                    log.warn("未找到用户{}的学生档案，跳过", userId);
                    errorCount++;
                    continue;
                }

                StudentProfileDO studentProfile = studentProfiles.get(0);
                if (studentProfiles.size() > 1) {
                    log.warn("用户{}存在多个学生档案，使用最新的档案ID: {}", userId, studentProfile.getId());
                }

                // 3.2 为每个问卷结果重新计算维度结果
                for (QuestionnaireResultDO result : userResults) {
                    try {
                        recalculateQuestionnaireResult(result);
                    } catch (Exception e) {
                        log.error("重新计算问卷结果失败, questionnaireResultId={}, questionnaireId={}",
                            result.getId(), result.getQuestionnaireId(), e);
                        errorCount++;
                    }
                }

                // 3.3 重新生成组合测评结果
                try {
                    generateAndSaveCombinedResult(taskNo, studentProfile.getId());
                    log.info("用户{}的组合测评结果重新生成成功", userId);
                    successCount++;
                } catch (Exception e) {
                    log.error("重新生成组合测评结果失败, taskNo={}, userId={}", taskNo, userId, e);
                    errorCount++;
                }

            } catch (Exception e) {
                log.error("处理用户{}的结果时发生异常", userId, e);
                errorCount++;
            }
        }

        log.info("重新计算测评任务结果完成, taskNo={}, 成功用户数: {}, 失败用户数: {}",
            taskNo, successCount, errorCount);
    }

    /**
     * 重新计算单个问卷结果的维度结果和评估信息
     */
    private void recalculateQuestionnaireResult(QuestionnaireResultDO result) {
        log.info("开始重新计算问卷结果, questionnaireResultId={}, questionnaireId={}",
            result.getId(), result.getQuestionnaireId());

        // 解析答题数据
        if (result.getAnswers() == null || result.getAnswers().trim().isEmpty()) {
            log.warn("问卷结果没有答题数据，跳过重新计算, questionnaireResultId={}", result.getId());
            return;
        }

        List<cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList;
        try {
            answerList = JsonUtils.parseArray(result.getAnswers(),
                cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO.AssessmentAnswerItem.class);
        } catch (Exception e) {
            log.error("解析答题数据失败, questionnaireResultId={}", result.getId(), e);
            return;
        }

        if (CollUtil.isEmpty(answerList)) {
            log.warn("问卷结果答题数据为空，跳过重新计算, questionnaireResultId={}", result.getId());
            return;
        }

        // 获取计算服务
        cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultCalculateService calculateService =
            cn.iocoder.yudao.framework.common.util.spring.SpringUtils.getBean(
                cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultCalculateService.class);

        // 重新计算维度结果
        List<cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO> calculatedResults;
        try {
            calculatedResults = calculateService.resultCalculate(
                result.getQuestionnaireId(), result.getUserId(), result.getId(), answerList);
        } catch (Exception e) {
            log.error("调用计算服务失败, questionnaireResultId={}", result.getId(), e);
            return;
        }

        // 更新问卷结果
        updateRecalculatedQuestionnaireResult(result, answerList, calculatedResults);

        log.info("问卷结果重新计算完成, questionnaireResultId={}", result.getId());
    }

    /**
     * 更新重新计算后的问卷结果
     */
    private void updateRecalculatedQuestionnaireResult(
            QuestionnaireResultDO originalResult,
            List<cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList,
            List<cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO> calculatedResults) {

        QuestionnaireResultDO updateResult = new QuestionnaireResultDO();
        updateResult.setId(originalResult.getId());

        // 1. 计算问卷总分（使用所有答题项的原始分数总和）
        int totalScore = answerList.stream().mapToInt(item -> item.getScore()).sum();
        updateResult.setScore(new BigDecimal(totalScore));
        log.info("问卷总分（原始答题分数）, questionnaireResultId={}, totalScore={}",
            originalResult.getId(), totalScore);

        // 2. 计算维度分数映射
        Map<String, BigDecimal> dimensionScores = new HashMap<>();
        if (calculatedResults != null) {
            for (cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO calcResult : calculatedResults) {
                if (calcResult.getDimensionName() != null && !calcResult.getDimensionName().isEmpty()) {
                    BigDecimal dimensionScore = new BigDecimal(calcResult.getScore());
                    dimensionScores.put(calcResult.getDimensionName(), dimensionScore);
                    log.info("  维度分数: dimensionName={}, score={}",
                        calcResult.getDimensionName(), calcResult.getScore());
                }
            }
        }

        if (!dimensionScores.isEmpty()) {
            updateResult.setDimensionScores(JsonUtils.toJsonString(dimensionScores));
            log.info("问卷维度分数, questionnaireResultId={}, 维度数={}",
                originalResult.getId(), dimensionScores.size());
        }

        // 重新计算风险等级和建议
        Integer maxRiskLevel = null;
        StringBuilder combinedSuggestions = new StringBuilder();
        StringBuilder combinedEvaluate = new StringBuilder();
        int abnormalDimensionCount = 0;

        for (cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO calcResult : calculatedResults) {
            // 统计异常维度数量
            if (calcResult.getIsAbnormal() != null && calcResult.getIsAbnormal() > 0) {
                abnormalDimensionCount++;
            }

            // 收集建议内容
            if (calcResult.getStudentComment() != null && !calcResult.getStudentComment().trim().isEmpty()) {
                if (combinedSuggestions.length() > 0) {
                    combinedSuggestions.append("\n");
                }
                combinedSuggestions.append(calcResult.getStudentComment());
            }

            // 收集评价内容
            if (calcResult.getDescription() != null && !calcResult.getDescription().trim().isEmpty()) {
                if (combinedEvaluate.length() > 0) {
                    combinedEvaluate.append("\n");
                }
                combinedEvaluate.append(calcResult.getDescription());
            }
        }

        // 根据异常维度数量设置风险等级
        if (abnormalDimensionCount == 0) {
            maxRiskLevel = 1; // 正常
        } else if (abnormalDimensionCount == 1) {
            maxRiskLevel = 2; // 关注
        } else if (abnormalDimensionCount == 2) {
            maxRiskLevel = 3; // 预警
        } else {
            maxRiskLevel = 4; // 高危
        }

        updateResult.setRiskLevel(maxRiskLevel);
        if (combinedSuggestions.length() > 0) {
            updateResult.setSuggestions(combinedSuggestions.toString());
        }
        if (combinedEvaluate.length() > 0) {
            updateResult.setEvaluate(combinedEvaluate.toString());
        }

        // 更新生成状态
        updateResult.setGenerationStatus(2); // 已生成

        // 重新生成维度明细到 result_data
        try {
            List<DimensionResultDO> dimList = dimensionResultMapper.selectListByQuestionnaireResultId(originalResult.getId());
            List<Map<String, Object>> payload = new ArrayList<>();
            if (dimList != null) {
                for (DimensionResultDO dr : dimList) {
                    Map<String, Object> item = new java.util.LinkedHashMap<>();
                    item.put("dimensionId", dr.getDimensionId());
                    item.put("dimensionCode", dr.getDimensionCode());
                    item.put("score", dr.getScore());
                    item.put("isAbnormal", dr.getIsAbnormal());
                    item.put("riskLevel", dr.getRiskLevel());
                    item.put("level", dr.getLevel());
                    item.put("teacherComment", dr.getTeacherComment());
                    item.put("studentComment", dr.getStudentComment());
                    item.put("questionnaireId", originalResult.getQuestionnaireId());

                    // 获取维度名称与描述
                    String dimName = null;
                    String dimDesc = null;
                    try {
                        var dimVO = questionnaireDimensionService.getDimension(dr.getDimensionId());
                        if (dimVO != null) {
                            dimName = dimVO.getDimensionName();
                            dimDesc = dimVO.getDescription();
                        }
                    } catch (Exception ignore) {}
                    if (dimName == null || dimName.isEmpty()) {
                        dimName = dr.getDimensionCode() != null ? dr.getDimensionCode() : ("DIM-" + dr.getDimensionId());
                    }
                    item.put("dimensionName", dimName);
                    item.put("description", dimDesc);
                    payload.add(item);
                }
            }
            updateResult.setResultData(JsonUtils.toJsonString(payload));
        } catch (Exception e) {
            log.warn("重新生成维度明细失败, questionnaireResultId={}", originalResult.getId(), e);
        }

        updateResult.setCompletedTime(new java.util.Date());

        // 执行更新
        questionnaireResultMapper.updateById(updateResult);

        log.info("问卷结果更新完成, questionnaireResultId={}, 异常维度数量={}, 风险等级={}",
            originalResult.getId(), abnormalDimensionCount, maxRiskLevel);
    }
}

