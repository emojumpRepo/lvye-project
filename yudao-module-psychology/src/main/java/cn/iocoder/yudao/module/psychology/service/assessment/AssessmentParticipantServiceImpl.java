package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentParticipantDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentAnswerDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentParticipantMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentAnswerMapper;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 测评参与 Service 实现类
 */
@Service
@Validated
@Slf4j
public class AssessmentParticipantServiceImpl implements AssessmentParticipantService {

    @Resource
    private AssessmentParticipantMapper participantMapper;
    
    @Resource
    private AssessmentAnswerMapper answerMapper;
    
    @Resource
    private StudentProfileService studentProfileService;
    
    @Resource
    private AssessmentTaskService assessmentTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startAssessment(String taskNo, Long userId, Boolean isParent) {
        // 校验任务存在
        if (assessmentTaskService.getAssessmentTask(taskNo) == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }

        // 获取学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByUserId(userId);
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }

        // 检查是否已经参与
//        AssessmentParticipantDO existingParticipant = participantMapper.selectByTaskIdAndStudentId(taskNo, studentProfile.getId());
//        if (existingParticipant != null) {
//            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_PARTICIPANT_EXISTS);
//        }
//
//        // 创建参与记录
//        AssessmentParticipantDO participant = new AssessmentParticipantDO();
//        participant.setTaskId(taskNo);
//        participant.setStudentProfileId(studentProfile.getId());
//        participant.setIsParent(isParent);
//        participant.setCompletionStatus(ParticipantCompletionStatusEnum.IN_PROGRESS.getStatus());
//        participant.setStartTime(LocalDateTime.now());
//        participantMapper.insert(participant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAssessment(Long taskId, Long userId, WebAssessmentParticipateReqVO participateReqVO) {
        // 获取学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByUserId(userId);
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }

        // 获取参与记录
        AssessmentParticipantDO participant = participantMapper.selectByTaskIdAndStudentId(taskId, studentProfile.getId());
        if (participant == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_NOT_COMPLETED);
        }

        // 保存答案
        for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : participateReqVO.getAnswers()) {
            AssessmentAnswerDO answer = new AssessmentAnswerDO();
            answer.setParticipantId(participant.getId());
            answer.setQuestionIndex(answerItem.getQuestionIndex());
            answer.setAnswer(answerItem.getAnswer());
            answer.setScore(answerItem.getScore());
            answerMapper.insert(answer);
        }

        // 更新参与状态
        AssessmentParticipantDO updateObj = new AssessmentParticipantDO();
        updateObj.setId(participant.getId());
        updateObj.setCompletionStatus(ParticipantCompletionStatusEnum.COMPLETED.getStatus());
        updateObj.setSubmitTime(LocalDateTime.now());
        participantMapper.updateById(updateObj);
    }

    @Override
    public Integer getParticipantStatus(Long taskId, Long userId) {
        // 获取学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByUserId(userId);
        if (studentProfile == null) {
            return ParticipantCompletionStatusEnum.NOT_STARTED.getStatus();
        }

        // 获取参与记录
        AssessmentParticipantDO participant = participantMapper.selectByTaskIdAndStudentId(taskId, studentProfile.getId());
        if (participant == null) {
            return ParticipantCompletionStatusEnum.NOT_STARTED.getStatus();
        }

        return participant.getCompletionStatus();
    }

}