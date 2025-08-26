package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultEvaluateConfigDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskQuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultEvaluateConfigMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultCalculateService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
    private AssessmentTaskQuestionnaireMapper taskQuestionnaireMapper;

    @Resource
    private QuestionnaireResultEvaluateConfigMapper evaluateConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startAssessment(String taskNo, Long userId) {
        Integer isParent = WebFrameworkUtils.getIsParent();
        // 校验任务存在
        if (assessmentTaskService.getAssessmentTask(taskNo) == null) {
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
        //更新参与测评状态
        userTaskMapper.updateStatusById(assessmentUserTaskDO.getId(), ParticipantCompletionStatusEnum.IN_PROGRESS.getStatus());
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
        //计算并返回问卷结果
        List<QuestionnaireResultVO> resultCalculate = resultCalculateService.resultCalculate(participateReqVO.getQuestionnaireId(),
                userId, participateReqVO.getAnswers());
        //更新问卷结果
        this.updateQuestionnaireResult(questionnaireResultId, participateReqVO.getQuestionnaireId(), participateReqVO.getAnswers(), resultCalculate);
        //判断问卷是否都已经完成,若已完成，则更新测评任务状态
        Long fishishQuestionnaire = questionnaireResultMapper.selectCountByTaskNoAndUserId(taskNo, userId);
        if (Long.valueOf(questionnaireIds.size()).equals(fishishQuestionnaire)) {
            userTaskMapper.updateFinishStatusById(taskNo);
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
        String result = JSON.toJSONString(answerList);
        resultDO.setAnswers(result);
        questionnaireResultMapper.insert(resultDO);
        return resultDO.getId();
    }

    private void updateQuestionnaireResult(Long questionnaireResultId, Long questionnaireId, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList, List<QuestionnaireResultVO> answerResultList) {
        QuestionnaireResultDO resultDO = new QuestionnaireResultDO();
        resultDO.setId(questionnaireResultId);
        int score = 0;
        //计算总分
        for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : answerList) {
            score = score + answerItem.getScore();
        }
        resultDO.setScore(new BigDecimal(score));
        //统计不正常的因子总数，计算风险登记
        int isAbnormalCount = 0;
        for (QuestionnaireResultVO answerResult : answerResultList) {
            isAbnormalCount = isAbnormalCount + answerResult.getIsAbnormal();
        }
        //查询评价内容，赋值
        QuestionnaireResultEvaluateConfigDO evaluateConfigDO = evaluateConfigMapper.selectByQuestionnaireIdAndAbnormalCount(questionnaireId, isAbnormalCount);
        if (evaluateConfigDO != null) {
            resultDO.setRiskLevel(evaluateConfigDO.getRiskLevel());
            resultDO.setEvaluate(evaluateConfigDO.getEvaluate());
            resultDO.setSuggestions(evaluateConfigDO.getSuggestions());
            resultDO.setGenerationStatus(2);
        } else {
            resultDO.setGenerationStatus(3);
        }
        resultDO.setResultData(JSON.toJSONString(answerResultList));
        resultDO.setCompletedTime(new Date());
        questionnaireResultMapper.updateById(resultDO);
    }

}