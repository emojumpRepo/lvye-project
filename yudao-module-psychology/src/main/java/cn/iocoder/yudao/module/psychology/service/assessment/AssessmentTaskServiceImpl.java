package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentParticipantDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentDeptTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentParticipantMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.enums.AssessmentTaskStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
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

    @Resource
    private DeptService deptService;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private AssessmentUserTaskMapper userTaskMapper;

    @Resource
    private AssessmentDeptTaskMapper deptTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssessmentTask(@Valid AssessmentTaskSaveReqVO createReqVO) {
        // 校验任务编号唯一性
        validateTaskNoUnique(null, createReqVO.getTaskNo());
        // 插入测评信息
        AssessmentTaskDO assessmentTask = BeanUtils.toBean(createReqVO, AssessmentTaskDO.class);
        assessmentTask.setStatus(AssessmentTaskStatusEnum.NOT_STARTED.getStatus());
        assessmentTaskMapper.insert(assessmentTask);

        // 插入部门测评关联信息
        List<DeptDO> deptList = deptService.getChildDeptList(createReqVO.getDeptIdList());
        List<AssessmentDeptTaskDO> deptTaskList = new ArrayList<>();
        List<Long> deptIds = new ArrayList<>();
        //父部门
        for(Long deptId : createReqVO.getDeptIdList()){
            AssessmentDeptTaskDO assessmentDeptTaskDO = new AssessmentDeptTaskDO();
            assessmentDeptTaskDO.setTaskNo(createReqVO.getTaskNo());
            assessmentDeptTaskDO.setDeptId(deptId);
            deptTaskList.add(assessmentDeptTaskDO);
            deptIds.add(deptId);
        }
        //子部门
        for(DeptDO deptDO : deptList){
            AssessmentDeptTaskDO assessmentDeptTaskDO = new AssessmentDeptTaskDO();
            assessmentDeptTaskDO.setTaskNo(createReqVO.getTaskNo());
            assessmentDeptTaskDO.setDeptId(deptDO.getId());
            deptTaskList.add(assessmentDeptTaskDO);
            deptIds.add(deptDO.getId());
        }
        deptTaskMapper.insertBatch(deptTaskList);
        // 插入用户测评关联信息
        List<AdminUserDO> userList = adminUserService.getUserListByDeptIds(deptIds);
        List<AssessmentUserTaskDO> userTaskList = new ArrayList<>();
        //遍历部门用户
        for(AdminUserDO userDO : userList){
            AssessmentUserTaskDO assessmentUserTaskDO = new AssessmentUserTaskDO();
            assessmentUserTaskDO.setTaskNo(createReqVO.getTaskNo());
            assessmentUserTaskDO.setUserId(userDO.getId());
            assessmentUserTaskDO.setParentFlag(createReqVO.getParentFlag());
            assessmentUserTaskDO.setStatus(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
            userTaskList.add(assessmentUserTaskDO);
        }
        //请求报文的用户
        for(Long userId: createReqVO.getUserIdList()){
            AssessmentUserTaskDO assessmentUserTaskDO = new AssessmentUserTaskDO();
            assessmentUserTaskDO.setTaskNo(createReqVO.getTaskNo());
            assessmentUserTaskDO.setUserId(userId);
            assessmentUserTaskDO.setParentFlag(createReqVO.getParentFlag());
            assessmentUserTaskDO.setStatus(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
            userTaskList.add(assessmentUserTaskDO);
        }
        userTaskMapper.insertBatch(userTaskList);
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
    public void deleteAssessmentTask(String taskNo) {
        // 校验存在
        AssessmentTaskDO assessmentTaskDO = getAssessmentTaskByNo(taskNo);
        if (!Objects.isNull(assessmentTaskDO)){
            // 删除
            assessmentTaskMapper.deleteById(assessmentTaskDO.getId());
            // 删除相关参与者
            userTaskMapper.deleteByTaskNo(taskNo);
        } else {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
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
    public AssessmentTaskDO getAssessmentTask(String taskNo) {
        return assessmentTaskMapper.selectByTaskNo(taskNo);
    }

    @Override
    public PageResult<AssessmentTaskDO> getAssessmentTaskPage(AssessmentTaskPageReqVO pageReqVO) {
        return assessmentTaskMapper.selectPage(pageReqVO);
    }

    @Override
    public void publishAssessmentTask(String taskNo) {
        // 校验存在
        AssessmentTaskDO task = assessmentTaskMapper.selectByTaskNo(taskNo);
        if (task == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }

        // 校验状态
        if (!Objects.equals(task.getStatus(), AssessmentTaskStatusEnum.NOT_STARTED.getStatus())) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_STARTED);
        }

        // 更新状态为进行中
        AssessmentTaskDO updateObj = new AssessmentTaskDO();
        updateObj.setTaskNo(taskNo);
        updateObj.setStatus(AssessmentTaskStatusEnum.IN_PROGRESS.getStatus());
        assessmentTaskMapper.updateStatusByTaskNo(updateObj);
    }

    @Override
    public void closeAssessmentTask(String taskNo) {
        // 校验存在
        AssessmentTaskDO task = assessmentTaskMapper.selectByTaskNo(taskNo);
        if (task == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }

        // 更新状态为已关闭
        AssessmentTaskDO updateObj = new AssessmentTaskDO();
        updateObj.setTaskNo(taskNo);
        updateObj.setStatus(AssessmentTaskStatusEnum.CLOSED.getStatus());
        assessmentTaskMapper.updateStatusByTaskNo(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addParticipants(String taskNo, List<Long> userIds) {
        // 校验任务存在
        if (Objects.isNull(getAssessmentTaskByNo(taskNo))){
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
        for (Long userId : userIds) {
            // 校验学生档案存在
            if (adminUserService.getUser(userId) == null) {
                throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
            }

            // 校验是否已存在参与者
            if (userTaskMapper.selectByTaskNoAndUserId(taskNo, userId) != null) {
                throw exception(ErrorCodeConstants.ASSESSMENT_TASK_PARTICIPANT_EXISTS);
            }

            // 插入参与者记录
            AssessmentUserTaskDO userTaskDO = new AssessmentUserTaskDO();
            userTaskDO.setTaskNo(taskNo);
            userTaskDO.setUserId(userId);
            userTaskDO.setParentFlag(0);
            userTaskDO.setStatus(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
            userTaskMapper.insert(userTaskDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeParticipants(String taskNo, List<Long> userIds) {
        // 校验任务存在
        if (Objects.isNull(getAssessmentTaskByNo(taskNo))){
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }

        for (Long userId : userIds) {
            userTaskMapper.deleteByTaskNoAndUserId(taskNo, userId);
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