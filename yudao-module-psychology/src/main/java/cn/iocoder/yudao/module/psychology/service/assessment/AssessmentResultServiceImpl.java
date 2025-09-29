package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
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
        AssessmentResultDO exist = assessmentResultMapper.selectOne(
            new LambdaQueryWrapperX<AssessmentResultDO>()
                .eq(AssessmentResultDO::getTaskNo, taskNo)
                .eq(AssessmentResultDO::getParticipantId, participantId)
                .eq(AssessmentResultDO::getDimensionCode, save.getDimensionCode())
        );
        if (exist == null) {
            assessmentResultMapper.insert(save);
            log.info("已保存组合测评结果(新增), participantId={}, id={}", participantId, save.getId());
        } else {
            save.setId(exist.getId());
            assessmentResultMapper.updateById(save);
            log.info("已保存组合测评结果(更新), participantId={}, id={}", participantId, save.getId());
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
}

