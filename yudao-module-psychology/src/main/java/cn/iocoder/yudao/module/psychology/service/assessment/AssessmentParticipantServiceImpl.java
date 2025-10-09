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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    protected final Logger logger = LoggerFactory.getLogger(getClass());

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

    @Resource
    private cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultTxService questionnaireResultTxService;

    @Resource
    private cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultAsyncService questionnaireResultAsyncService;

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

        // 使用新事务保存初始结果( generation_status = 1 )，确保立刻可见
        Long questionnaireResultId = questionnaireResultTxService.saveInitialResultNewTx(
                taskNo, userId, participateReqVO.getQuestionnaireId(), participateReqVO.getAnswers());

        // 异步进行结果计算和更新（在当前事务提交后执行）
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 完全异步处理，不阻塞响应
                    processAssessmentResultAsync(taskNo, userId, questionnaireResultId, participateReqVO);
                }
            }
        );

        // 方法立即返回，提交成功
    }

    /**
     * 异步处理测评结果计算和更新
     */
    private void processAssessmentResultAsync(String taskNo, Long userId, Long questionnaireResultId, WebAssessmentParticipateReqVO participateReqVO) {
        try {
            questionnaireResultAsyncService.calculateAfterCommit(
                    participateReqVO.getQuestionnaireId(), userId, questionnaireResultId,
                    participateReqVO.getAnswers(),
                    () -> {
                        List<QuestionnaireResultVO> resultCalculate = resultCalculateService.resultCalculate(
                                participateReqVO.getQuestionnaireId(), userId, questionnaireResultId, participateReqVO.getAnswers());
                        // 更新问卷结果（设置得分、维度、风险、generation_status=2 等）
                        QuestionnaireResultDO updated = updateQuestionnaireResult(
                                questionnaireResultId,
                                participateReqVO.getQuestionnaireId(),
                                participateReqVO.getAnswers(),
                                resultCalculate
                        );

                        // 更新任务风险等级与学生风险（若有）
                        try {
                            if (updated != null && (updated.getRiskLevel() != null
                                    || !StringUtils.isAnyBlank(updated.getEvaluate(), updated.getSuggestions()))) {
                                userTaskMapper.updateTaskRiskLevel(taskNo, userId, updated.getRiskLevel(), updated.getEvaluate(), updated.getSuggestions());
                                StudentProfileDO studentProfile2 = studentProfileService.getStudentProfileByUserId(userId);
                                if (studentProfile2 != null && updated.getRiskLevel() != null) {
                                    studentProfileService.updateStudentRiskLevel(studentProfile2.getId(), updated.getRiskLevel());
                                }
                            }
                        } catch (Exception ignore) {}

                        // 重新计算该任务下该用户的完成情况
                        List<Long> qids = taskQuestionnaireMapper.selectQuestionnaireIdsByTaskNo(taskNo, TenantContextHolder.getTenantId());
                        Long finished = questionnaireResultMapper.selectCountByTaskNoAndUserId(taskNo, userId);
                        if (qids != null && finished != null && Long.valueOf(qids.size()).equals(finished)) {
                            // 标记完成
                            userTaskMapper.updateFinishTask(taskNo, userId);

                            // 获取测评任务与学生信息（已在外层查过，这里兜底再查一次）
                            AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTaskByNo(taskNo);
                            StudentProfileDO studentProfile2 = studentProfileService.getStudentProfileByUserId(userId);

                            // 记录完成时间线（带上最新问卷结果简要信息）
                            Map<String, Object> meta = new HashMap<>();
                            meta.put("taskNo", taskNo);
                            if (assessmentTask != null) {
                                meta.put("taskName", assessmentTask.getTaskName());
                                meta.put("taskId", assessmentTask.getId());
                                meta.put("scenarioId", assessmentTask.getScenarioId());
                                meta.put("targetAudience", assessmentTask.getTargetAudience());
                            }
                            meta.put("questionnaireCount", qids.size());
                            meta.put("questionnaireIds", qids);
                            if (studentProfile2 != null) {
                                meta.put("studentId", studentProfile2.getId());
                                meta.put("studentNo", studentProfile2.getStudentNo());
                                meta.put("studentName", studentProfile2.getName());
                            }
                            if (updated != null) {
                                meta.put("riskLevel", updated.getRiskLevel());
                                meta.put("evaluate", updated.getEvaluate());
                                meta.put("suggestions", updated.getSuggestions());
                                meta.put("score", updated.getScore());
                                meta.put("resultId", updated.getId());
                            }
                            meta.put("completedAt", new Date());

                            String content = String.format("完成测评任务「%s」，包含%d份问卷",
                                    assessmentTask != null ? assessmentTask.getTaskName() : taskNo, qids.size());
                            if (studentProfile2 != null) {
                                studentTimelineService.saveTimelineWithMeta(studentProfile2.getId(),
                                        TimelineEventTypeEnum.ASSESSMENT_COMPLETED.getType(),
                                        TimelineEventTypeEnum.ASSESSMENT_COMPLETED.getName(),
                                        taskNo, content, meta);
                            }

                            // 触发组合测评结果生成
                            try {
                                if (studentProfile2 != null) {
                                    assessmentResultService.generateAndSaveCombinedResult(taskNo, studentProfile2.getId());
                                }
                            } catch (Exception e) {
                                log.error("生成组合测评结果失败, taskNo={}, userId={}, err= {}", taskNo, userId, e.getMessage(), e);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            log.error("异步处理测评结果失败, taskNo={}, userId={}, questionnaireResultId={}, err= {}",
                    taskNo, userId, questionnaireResultId, e.getMessage(), e);
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
        logger.info("问卷ID={} 保存问卷结果: {}", questionnaireId, resultDO);
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
    
        if (combinedSuggestions.length() > 0) {
            resultDO.setSuggestions(combinedSuggestions.toString());
        }
        if (combinedEvaluate.length() > 0) {
            resultDO.setEvaluate(combinedEvaluate.toString());
        }
        
        // 设置生成状态
        if (answerResultList != null && !answerResultList.isEmpty()) {
            resultDO.setGenerationStatus(2); // 已生成
            log.info("问卷ID={} 计算完成，异常维度数量={}", questionnaireId, abnormalDimensionCount);
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