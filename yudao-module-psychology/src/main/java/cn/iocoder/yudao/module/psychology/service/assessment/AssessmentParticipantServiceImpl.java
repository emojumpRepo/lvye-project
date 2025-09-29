package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskQuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.DimensionResultMapper;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.TimelineEventTypeEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.psychology.service.profile.StudentTimelineService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultCalculateService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireDimensionService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 测评参与 Service 实现类
 */
@Service
@Validated
@Slf4j
public class AssessmentParticipantServiceImpl implements AssessmentParticipantService {

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @Resource
    private StudentProfileService studentProfileService;

    @Resource
    private AssessmentUserTaskMapper userTaskMapper;

    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;

    @Resource
    private AssessmentTaskMapper assessmentTaskMapper;

    @Resource
    private QuestionnaireResultCalculateService resultCalculateService;
    
    @Resource
    private DimensionResultMapper dimensionResultMapper;
    
    @Resource
    private QuestionnaireDimensionService questionnaireDimensionService;

    @Resource
    private AssessmentTaskQuestionnaireMapper taskQuestionnaireMapper;

    @Resource
    private StudentTimelineService studentTimelineService;

    @Resource
    private AssessmentResultService assessmentResultService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startAssessment(String taskNo, Long userId) {
        Integer isParent = WebFrameworkUtils.getIsParent();
        // 校验任务存在
        AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTask(taskNo);
        if (assessmentTask == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
        // 获取学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByUserId(userId);
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
        AssessmentUserTaskDO assessmentUserTaskDO = userTaskMapper.selectByTaskNoAndUserId(taskNo, userId);
        //判断身份是否相符
        if (!isParent.equals(assessmentUserTaskDO.getParentFlag())) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_PARTICIPANT_CANNOT_START);
        }
        // 检查是否已经参与(即状态为进行中或已完成)
        if (assessmentUserTaskDO.getStatus().equals(ParticipantCompletionStatusEnum.COMPLETED.getStatus())) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_PARTICIPANT_EXISTS);
        }
        
        // 只有当状态从NOT_STARTED变为IN_PROGRESS时才记录时间线
        boolean isFirstStart = assessmentUserTaskDO.getStatus().equals(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
        
        //更新参与测评状态
        userTaskMapper.updateStatusById(assessmentUserTaskDO.getId(), ParticipantCompletionStatusEnum.IN_PROGRESS.getStatus());
        
        // 只在第一次开始时记录时间线
        if (isFirstStart) {
            // 记录开始测评的时间线
            Map<String, Object> meta = new HashMap<>();
        meta.put("taskNo", taskNo);
        meta.put("taskName", assessmentTask.getTaskName());
        meta.put("taskId", assessmentTask.getId());
        meta.put("targetAudience", assessmentTask.getTargetAudience());
        meta.put("scenarioId", assessmentTask.getScenarioId());
        meta.put("studentId", studentProfile.getId());
        meta.put("studentNo", studentProfile.getStudentNo());
        meta.put("studentName", studentProfile.getName());
        meta.put("isParent", isParent);
        meta.put("startTime", new Date());
        
        // 获取问卷数量
        List<Long> questionnaireIds = taskQuestionnaireMapper.selectQuestionnaireIdsByTaskNo(taskNo, TenantContextHolder.getTenantId());
        meta.put("questionnaireCount", questionnaireIds.size());
        meta.put("questionnaireIds", questionnaireIds);
        
        String content = String.format("开始参与测评任务「%s」，共%d份问卷", 
            assessmentTask.getTaskName(), questionnaireIds.size());
        
            studentTimelineService.saveTimelineWithMeta(studentProfile.getId(),
                TimelineEventTypeEnum.ASSESSMENT_COMPLETED.getType(), // 使用同一个类型，通过meta区分开始/完成
                "开始测评",
                "assessment_start_" + taskNo,
                content,
                meta);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAssessment(String taskNo, Long userId, WebAssessmentParticipateReqVO participateReqVO) {
        //查询任务是否存在
        AssessmentTaskDO assessmentTaskDO = assessmentTaskMapper.selectByTaskNo(taskNo);
        if (assessmentTaskDO == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
        //查询是否已提交
        QuestionnaireResultDO resultDO = questionnaireResultMapper.selectByUnique(taskNo, userId, participateReqVO.getQuestionnaireId());
        if (resultDO != null) {
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_RESULT_ALREADY_EXISTS);
        }
        // 获取学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByUserId(userId);
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
        // 获取参与记录
        AssessmentUserTaskDO assessmentUserTaskDO = userTaskMapper.selectByTaskNoAndUserId(taskNo, userId);
        if (ParticipantCompletionStatusEnum.COMPLETED.getStatus().equals(assessmentUserTaskDO.getStatus())) {
            throw exception(ErrorCodeConstants.ASSESSMENT_ALREADY_COMPLETED);
        }
        // 检查问卷id是否一致
        List<Long> questionnaireIds = taskQuestionnaireMapper.selectQuestionnaireIdsByTaskNo(taskNo, TenantContextHolder.getTenantId());
        if(!questionnaireIds.contains(participateReqVO.getQuestionnaireId())){
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_NOT_EXISTS);
        }
        //保存问卷提交答案
        Long questionnaireResultId = this.saveQuestionnaireResult(taskNo, userId, participateReqVO.getQuestionnaireId(), participateReqVO.getAnswers());
        //计算并返回问卷结果（带问卷结果ID，支持维度结果保存）
        List<QuestionnaireResultVO> resultCalculate = resultCalculateService.resultCalculate(participateReqVO.getQuestionnaireId(),
                userId, questionnaireResultId, participateReqVO.getAnswers());
        //更新问卷结果
        QuestionnaireResultDO questionnaireResultDO = this.updateQuestionnaireResult(questionnaireResultId, participateReqVO.getQuestionnaireId(), participateReqVO.getAnswers(), resultCalculate);
        //如果问卷结果有返回风险等级/评价/建议，则更新学生测评表,更新学生档案
        if(questionnaireResultDO.getRiskLevel() != null || !StringUtils.isAnyBlank(questionnaireResultDO.getEvaluate(), questionnaireResultDO.getSuggestions())){
            userTaskMapper.updateTaskRiskLevel(taskNo, userId, questionnaireResultDO.getRiskLevel(), questionnaireResultDO.getEvaluate(), questionnaireResultDO.getSuggestions());
            studentProfileService.updateStudentRiskLevel(studentProfile.getId(), questionnaireResultDO.getRiskLevel());
        }
        //判断问卷是否都已经完成,若已完成，则更新测评任务状态
        Long fishishQuestionnaire = questionnaireResultMapper.selectCountByTaskNoAndUserId(taskNo, userId);
        if (Long.valueOf(questionnaireIds.size()).equals(fishishQuestionnaire)) {
            userTaskMapper.updateFinishTask(taskNo, userId);
            
            // 获取测评任务信息
            AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTaskByNo(taskNo);
            
            //登记时间线（添加meta数据）
            Map<String, Object> meta = new HashMap<>();
            // 任务信息
            meta.put("taskNo", taskNo);
            if (assessmentTask != null) {
                meta.put("taskName", assessmentTask.getTaskName());
                meta.put("taskId", assessmentTask.getId());
                meta.put("scenarioId", assessmentTask.getScenarioId());
                meta.put("targetAudience", assessmentTask.getTargetAudience());
            }
            // 问卷信息
            meta.put("questionnaireCount", questionnaireIds.size());
            meta.put("questionnaireIds", questionnaireIds);
            // 学生信息
            meta.put("studentId", studentProfile.getId());
            meta.put("studentNo", studentProfile.getStudentNo());
            meta.put("studentName", studentProfile.getName());
            // 测评结果信息
            if (questionnaireResultDO != null) {
                meta.put("riskLevel", questionnaireResultDO.getRiskLevel());
                meta.put("evaluate", questionnaireResultDO.getEvaluate());
                meta.put("suggestions", questionnaireResultDO.getSuggestions());
                meta.put("score", questionnaireResultDO.getScore());
                meta.put("resultId", questionnaireResultDO.getId());
            }
            // 完成时间
            meta.put("completedAt", new Date());
            
            String content = String.format("完成测评任务「%s」，包含%d份问卷", 
                assessmentTask != null ? assessmentTask.getTaskName() : taskNo, 
                questionnaireIds.size());
            studentTimelineService.saveTimelineWithMeta(studentProfile.getId(), 
                TimelineEventTypeEnum.ASSESSMENT_COMPLETED.getType(), 
                TimelineEventTypeEnum.ASSESSMENT_COMPLETED.getName(), 
                taskNo, content, meta);
            try {
                // 问卷全部完成后，触发组合测评结果生成并保存
                assessmentResultService.generateAndSaveCombinedResult(taskNo, studentProfile.getId());
            } catch (Exception e) {
                log.error("生成组合测评结果失败, taskNo={}, studentProfileId={}, err={}", taskNo, studentProfile.getId(), e.getMessage(), e);
            }
        }
    }

    @Override
    public Integer getParticipantStatus(String taskNo, Long userId) {
        // 获取学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByUserId(userId);
        if (studentProfile == null) {
            return ParticipantCompletionStatusEnum.NOT_STARTED.getStatus();
        }
        // 获取参与记录
        AssessmentUserTaskDO assessmentUserTaskDO = userTaskMapper.selectByTaskNoAndUserId(taskNo, userId);
        if (assessmentUserTaskDO == null) {
            return ParticipantCompletionStatusEnum.NOT_STARTED.getStatus();
        }
        return assessmentUserTaskDO.getStatus();

    }

    private Long saveQuestionnaireResult(String taskNo, Long userId, Long questionnaireId, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
        QuestionnaireResultDO resultDO = new QuestionnaireResultDO();
        resultDO.setAssessmentTaskNo(taskNo);
        resultDO.setUserId(userId);
        resultDO.setQuestionnaireId(questionnaireId);
        resultDO.setGenerationStatus(1);
        String result = JSON.toJSONString(answerList);
        resultDO.setAnswers(result);
        questionnaireResultMapper.insert(resultDO);
        return resultDO.getId();
    }

    private QuestionnaireResultDO updateQuestionnaireResult(Long questionnaireResultId, Long questionnaireId, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList, List<QuestionnaireResultVO> answerResultList) {
        QuestionnaireResultDO resultDO = new QuestionnaireResultDO();
        resultDO.setId(questionnaireResultId);
        int score = 0;
        //计算总分
        for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : answerList) {
            score = score + answerItem.getScore();
        }
        resultDO.setScore(new BigDecimal(score));
        
        // 提取并保存维度分数
        Map<String, BigDecimal> dimensionScores = new HashMap<>();
        for (QuestionnaireResultVO answerResult : answerResultList) {
            if (answerResult.getDimensionName() != null && !answerResult.getDimensionName().isEmpty()) {
                dimensionScores.put(answerResult.getDimensionName(), new BigDecimal(answerResult.getScore()));
            }
        }
        if (!dimensionScores.isEmpty()) {
            resultDO.setDimensionScores(JSON.toJSONString(dimensionScores));
            log.info("问卷ID={} 保存维度分数: {}", questionnaireId, dimensionScores);
        }
        
        // 基于维度计算结果设置风险等级和建议
        Integer maxRiskLevel = null;
        StringBuilder combinedSuggestions = new StringBuilder();
        StringBuilder combinedEvaluate = new StringBuilder();
        int abnormalDimensionCount = 0;
        
        for (QuestionnaireResultVO answerResult : answerResultList) {
            // 统计异常维度数量
            if (answerResult.getIsAbnormal() != null && answerResult.getIsAbnormal() > 0) {
                abnormalDimensionCount++;
            }
            
            // 收集建议内容
            if (answerResult.getStudentComment() != null && !answerResult.getStudentComment().trim().isEmpty()) {
                if (combinedSuggestions.length() > 0) {
                    combinedSuggestions.append("\n");
                }
                combinedSuggestions.append(answerResult.getStudentComment());
            }
            
            // 收集评价内容
            if (answerResult.getDescription() != null && !answerResult.getDescription().trim().isEmpty()) {
                if (combinedEvaluate.length() > 0) {
                    combinedEvaluate.append("\n");
                }
                combinedEvaluate.append(answerResult.getDescription());
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
        
        // 设置计算结果
        resultDO.setRiskLevel(maxRiskLevel);
        if (combinedSuggestions.length() > 0) {
            resultDO.setSuggestions(combinedSuggestions.toString());
        }
        if (combinedEvaluate.length() > 0) {
            resultDO.setEvaluate(combinedEvaluate.toString());
        }
        
        // 设置生成状态
        if (answerResultList != null && !answerResultList.isEmpty()) {
            resultDO.setGenerationStatus(2); // 已生成
            log.info("问卷ID={} 计算完成，异常维度数量={}, 风险等级={}", questionnaireId, abnormalDimensionCount, maxRiskLevel);
        } else {
            // 未配置维度：跳过计算，标记成功且无/低风险
            resultDO.setGenerationStatus(2);
            resultDO.setRiskLevel(1);
            log.info("问卷ID={} 未配置维度，跳过计算并标记为成功（无/低风险）", questionnaireId);
        }
        // 写入维度明细到 result_data
        try {
            java.util.List<DimensionResultDO> dimList = dimensionResultMapper.selectListByQuestionnaireResultId(questionnaireResultId);
            java.util.List<java.util.Map<String, Object>> payload = new java.util.ArrayList<>();
            if (dimList != null) {
                for (DimensionResultDO dr : dimList) {
                    java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                    item.put("dimensionId", dr.getDimensionId());
                    item.put("dimensionCode", dr.getDimensionCode());
                    item.put("score", dr.getScore());
                    item.put("isAbnormal", dr.getIsAbnormal());
                    item.put("riskLevel", dr.getRiskLevel());
                    item.put("level", dr.getLevel());
                    item.put("teacherComment", dr.getTeacherComment());
                    item.put("studentComment", dr.getStudentComment());
                    item.put("questionnaireId", questionnaireId);
                    // 维度名称与描述
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
            resultDO.setResultData(com.alibaba.fastjson.JSON.toJSONString(payload));
        } catch (Exception e) {
            // 失败不阻塞主流程
            log.warn("写入问卷维度明细失败 questionnaireResultId={}, err= {}", questionnaireResultId, e.getMessage());
        }
        resultDO.setCompletedTime(new Date());
        questionnaireResultMapper.updateById(resultDO);
        return resultDO;
    }

}