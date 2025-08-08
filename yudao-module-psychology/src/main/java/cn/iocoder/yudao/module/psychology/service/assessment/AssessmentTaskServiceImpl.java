package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentParticipantDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentParticipantMapper;
import cn.iocoder.yudao.module.psychology.enums.AssessmentTaskStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 测评任务 Service 实现类
 */
@Service
@Validated
@Slf4j
public class AssessmentTaskServiceImpl implements AssessmentTaskService {

    @Resource
    private AssessmentTaskMapper assessmentTaskMapper;
    
    @Resource
    private AssessmentParticipantMapper participantMapper;
    
    @Resource
    private StudentProfileService studentProfileService;

    @Override
    public Long createAssessmentTask(@Valid AssessmentTaskSaveReqVO createReqVO) {
        // 校验任务编号唯一性
        validateTaskNoUnique(null, createReqVO.getTaskNo());

        // 插入
        AssessmentTaskDO assessmentTask = BeanUtils.toBean(createReqVO, AssessmentTaskDO.class);
        assessmentTask.setStatus(AssessmentTaskStatusEnum.NOT_STARTED.getStatus());
        assessmentTaskMapper.insert(assessmentTask);
        
        // 返回
        return assessmentTask.getId();
    }

    @Override
    public void updateAssessmentTask(@Valid AssessmentTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateAssessmentTaskExists(updateReqVO.getId());
        // 校验任务编号唯一性
        validateTaskNoUnique(updateReqVO.getId(), updateReqVO.getTaskNo());

        // 更新
        AssessmentTaskDO updateObj = BeanUtils.toBean(updateReqVO, AssessmentTaskDO.class);
        assessmentTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteAssessmentTask(Long id) {
        // 校验存在
        validateAssessmentTaskExists(id);
        // 删除
        assessmentTaskMapper.deleteById(id);
        // 删除相关参与者
        participantMapper.deleteByTaskId(id);
    }

    private void validateAssessmentTaskExists(Long id) {
        if (assessmentTaskMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
    }

    private void validateTaskNoUnique(Long id, String taskNo) {
        AssessmentTaskDO task = assessmentTaskMapper.selectByTaskNo(taskNo);
        if (task == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的任务
        if (id == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_CODE_DUPLICATE);
        }
        if (!Objects.equals(task.getId(), id)) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_CODE_DUPLICATE);
        }
    }

    @Override
    public AssessmentTaskDO getAssessmentTask(Long id) {
        return assessmentTaskMapper.selectById(id);
    }

    @Override
    public PageResult<AssessmentTaskDO> getAssessmentTaskPage(AssessmentTaskPageReqVO pageReqVO) {
        return assessmentTaskMapper.selectPage(pageReqVO);
    }

    @Override
    public void publishAssessmentTask(Long id) {
        // 校验存在
        AssessmentTaskDO task = assessmentTaskMapper.selectById(id);
        if (task == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }

        // 校验状态
        if (!Objects.equals(task.getStatus(), AssessmentTaskStatusEnum.NOT_STARTED.getStatus())) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_STARTED);
        }

        // 更新状态为进行中
        AssessmentTaskDO updateObj = new AssessmentTaskDO();
        updateObj.setId(id);
        updateObj.setStatus(AssessmentTaskStatusEnum.IN_PROGRESS.getStatus());
        assessmentTaskMapper.updateById(updateObj);
    }

    @Override
    public void closeAssessmentTask(Long id) {
        // 校验存在
        validateAssessmentTaskExists(id);

        // 更新状态为已关闭
        AssessmentTaskDO updateObj = new AssessmentTaskDO();
        updateObj.setId(id);
        updateObj.setStatus(AssessmentTaskStatusEnum.CLOSED.getStatus());
        assessmentTaskMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addParticipants(Long taskId, List<Long> studentProfileIds) {
        // 校验任务存在
        validateAssessmentTaskExists(taskId);

        for (Long studentProfileId : studentProfileIds) {
            // 校验学生档案存在
            if (studentProfileService.getStudentProfile(studentProfileId) == null) {
                throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
            }

            // 校验是否已存在参与者
            if (participantMapper.selectByTaskIdAndStudentId(taskId, studentProfileId) != null) {
                throw exception(ErrorCodeConstants.ASSESSMENT_TASK_PARTICIPANT_EXISTS);
            }

            // 插入参与者记录
            AssessmentParticipantDO participant = new AssessmentParticipantDO();
            participant.setTaskId(taskId);
            participant.setStudentProfileId(studentProfileId);
            participant.setIsParent(false); // 默认学生参与
            participant.setCompletionStatus(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
            participantMapper.insert(participant);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeParticipants(Long taskId, List<Long> studentProfileIds) {
        // 校验任务存在
        validateAssessmentTaskExists(taskId);

        for (Long studentProfileId : studentProfileIds) {
            participantMapper.deleteByTaskIdAndStudentId(taskId, studentProfileId);
        }
    }

    @Override
    public AssessmentTaskStatisticsRespVO getTaskStatistics(Long taskId) {
        // 校验任务存在
        validateAssessmentTaskExists(taskId);

        // 统计参与者信息
        long totalParticipants = participantMapper.countByTaskId(taskId);
        long completedParticipants = participantMapper.countByTaskIdAndStatus(taskId, ParticipantCompletionStatusEnum.COMPLETED.getStatus());
        long inProgressParticipants = participantMapper.countByTaskIdAndStatus(taskId, ParticipantCompletionStatusEnum.IN_PROGRESS.getStatus());

        AssessmentTaskStatisticsRespVO statistics = new AssessmentTaskStatisticsRespVO();
        statistics.setTotalParticipants(totalParticipants);
        statistics.setCompletedParticipants(completedParticipants);
        statistics.setInProgressParticipants(inProgressParticipants);
        statistics.setNotStartedParticipants(totalParticipants - completedParticipants - inProgressParticipants);
        statistics.setCompletionRate(totalParticipants > 0 ? (double) completedParticipants / totalParticipants * 100 : 0.0);

        return statistics;
    }

    @Override
    public AssessmentTaskDO getAssessmentTaskByNo(String taskNo) {
        return assessmentTaskMapper.selectByTaskNo(taskNo);
    }

}