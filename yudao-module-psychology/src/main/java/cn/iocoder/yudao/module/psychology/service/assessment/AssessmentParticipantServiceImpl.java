package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startAssessment(String taskNo, Long userId) {
        // 校验任务存在
        if (assessmentTaskService.getAssessmentTask(taskNo) == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
        // 获取学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByUserId(userId);
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
        // 检查是否已经参与(即状态为进行中或已完成)
        AssessmentUserTaskDO assessmentUserTaskDO = userTaskMapper.selectByTaskNoAndUserId(taskNo, userId);
        if (assessmentUserTaskDO != null && !assessmentUserTaskDO.getStatus().equals(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus())) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_PARTICIPANT_EXISTS);
        }
        //更新参与测评状态
        userTaskMapper.updateStatusById(assessmentUserTaskDO.getId(), ParticipantCompletionStatusEnum.IN_PROGRESS.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAssessment(String taskNo, Long userId, WebAssessmentParticipateReqVO participateReqVO) {
        // 获取学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByUserId(userId);
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
        // 获取参与记录
        AssessmentUserTaskDO assessmentUserTaskDO = userTaskMapper.selectByTaskNoAndUserId(taskNo, studentProfile.getId());
        if (ParticipantCompletionStatusEnum.COMPLETED.getStatus().equals(assessmentUserTaskDO.getStatus())) {
            throw exception(ErrorCodeConstants.ASSESSMENT_ALREADY_COMPLETED);
        }

        // TODO: 实现具体的提交测评逻辑
        // 当前为简化实现，后续可以添加具体的业务逻辑
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

}