package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentDeptTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.enums.AssessmentTaskStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
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
    private StudentProfileService studentProfileService;

    @Resource
    private DeptService deptService;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private AssessmentUserTaskMapper userTaskMapper;

    @Resource
    private AssessmentDeptTaskMapper deptTaskMapper;

    @Resource
    private PermissionApi permissionApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssessmentTask(@Valid AssessmentTaskSaveReqVO createReqVO) {
        // 检查是否需要立即发布
        boolean isPublish = createReqVO.getIsPublish() != null && createReqVO.getIsPublish();
        return createAssessmentTask(createReqVO, isPublish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssessmentTask(@Valid AssessmentTaskSaveReqVO createReqVO, boolean isPublish) {
        // 校验任务编号唯一性
        validateTaskNameUnique(null, createReqVO.getTaskName());
        // 插入测评信息
        createReqVO.setTaskNo("TASK_" + DateUtils.getNowDatetimeStr() + "_" + NumberUtils.randomNumber());
        AssessmentTaskDO assessmentTask = BeanUtils.toBean(createReqVO, AssessmentTaskDO.class);
        //生成任务编号
        assessmentTask.setStatus(AssessmentTaskStatusEnum.NOT_STARTED.getStatus());
        assessmentTask.setPublishUserId(SecurityFrameworkUtils.getLoginUserId());
        assessmentTaskMapper.insert(assessmentTask);

        // 插入部门测评关联信息
        List<AssessmentDeptTaskDO> deptTaskList = new ArrayList<>();
        List<Long> deptIds = new ArrayList<>();
        
        if (createReqVO.getDeptIdList() != null && !createReqVO.getDeptIdList().isEmpty()) {
            List<DeptDO> deptList = deptService.getChildDeptList(createReqVO.getDeptIdList());
            //父部门
            for(Long deptId : createReqVO.getDeptIdList()){
                AssessmentDeptTaskDO assessmentDeptTaskDO = new AssessmentDeptTaskDO();
                assessmentDeptTaskDO.setTaskNo(createReqVO.getTaskNo());
                assessmentDeptTaskDO.setDeptId(deptId);
                deptTaskList.add(assessmentDeptTaskDO);
                deptIds.add(deptId);
            }
            //子部门
            if (deptList != null) {
                for(DeptDO deptDO : deptList){
                    AssessmentDeptTaskDO assessmentDeptTaskDO = new AssessmentDeptTaskDO();
                    assessmentDeptTaskDO.setTaskNo(createReqVO.getTaskNo());
                    assessmentDeptTaskDO.setDeptId(deptDO.getId());
                    deptTaskList.add(assessmentDeptTaskDO);
                    deptIds.add(deptDO.getId());
                }
            }
        }
        if (!deptTaskList.isEmpty()) {
            deptTaskMapper.insertBatch(deptTaskList);
        }
        
        // 插入用户测评关联信息
        List<AssessmentUserTaskDO> userTaskList = new ArrayList<>();
        
        // 遍历部门用户
        if (!deptIds.isEmpty()) {
            List<AdminUserDO> userList = adminUserService.getUserListByDeptIds(deptIds);
            if (userList != null) {
                for(AdminUserDO userDO : userList){
                    AssessmentUserTaskDO assessmentUserTaskDO = new AssessmentUserTaskDO();
                    assessmentUserTaskDO.setTaskNo(createReqVO.getTaskNo());
                    assessmentUserTaskDO.setUserId(userDO.getId());
                    assessmentUserTaskDO.setParentFlag(createReqVO.getTargetAudience());
                    assessmentUserTaskDO.setStatus(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
                    userTaskList.add(assessmentUserTaskDO);
                }
            }
        }
        
        //请求报文的用户
        if (createReqVO.getUserIdList() != null && !createReqVO.getUserIdList().isEmpty()) {
            for(Long userId: createReqVO.getUserIdList()){
                AssessmentUserTaskDO assessmentUserTaskDO = new AssessmentUserTaskDO();
                assessmentUserTaskDO.setTaskNo(createReqVO.getTaskNo());
                assessmentUserTaskDO.setUserId(userId);
                assessmentUserTaskDO.setParentFlag(createReqVO.getTargetAudience());
                assessmentUserTaskDO.setStatus(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
                userTaskList.add(assessmentUserTaskDO);
            }
        }
        if (!userTaskList.isEmpty()) {
            userTaskMapper.insertBatch(userTaskList);
        }

        // 如果需要立即发布，则发布任务
        if (isPublish) {
            try {
                publishAssessmentTask(createReqVO.getTaskNo());
            } catch (Exception e) {
                log.error("创建任务后自动发布失败，任务编号：{}，错误信息：{}", createReqVO.getTaskNo(), e.getMessage(), e);
                // 这里可以选择抛出异常或者记录日志继续执行
                // 为了保证任务创建成功，这里选择记录日志继续执行
            }
        }

        // 返回
        return assessmentTask.getId();
    }

    @Override
    public void updateAssessmentTask(@Valid AssessmentTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateAssessmentTaskExists(updateReqVO.getTaskNo());
        // 校验任务编号唯一性
        validateTaskNoUnique(updateReqVO.getId(), updateReqVO.getTaskNo());
        // 更新
        AssessmentTaskDO updateObj = BeanUtils.toBean(updateReqVO, AssessmentTaskDO.class);
        assessmentTaskMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAssessmentTask(String taskNo) {
        // 校验存在
        AssessmentTaskDO assessmentTaskDO = getAssessmentTaskByNo(taskNo);
        if (!Objects.isNull(assessmentTaskDO)){
            // 删除
            assessmentTaskMapper.deleteById(assessmentTaskDO.getId());
            // 删除相关参与者
            userTaskMapper.deleteByTaskNo(taskNo);
            // 删除相关年级/班级你
            deptTaskMapper.deleteByTaskNo(taskNo);
        } else {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
    }

    private void validateAssessmentTaskExists(String taskNo) {
        if (assessmentTaskMapper.selectByTaskNo(taskNo) == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
    }

    @Override
    public void validateTaskNameUnique(Long id, String taskName) {
        AssessmentTaskDO task = assessmentTaskMapper.selectByTaskName(taskName);
        if (task == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的任务
        if (id == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NAME_DUPLICATE);
        }
        if (!Objects.equals(task.getId(), id)) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NAME_DUPLICATE);
        }
    }

    private void validateTaskNoUnique(Long id, String taskNo) {
        AssessmentTaskDO task = assessmentTaskMapper.selectByTaskName(taskNo);
        if (task == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的任务
        if (id == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NAME_DUPLICATE);
        }
        if (!Objects.equals(task.getId(), id)) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NAME_DUPLICATE);
        }
    }

    @Override
    public AssessmentTaskDO getAssessmentTask(String taskNo) {
        AssessmentTaskDO assessmentTaskDO = assessmentTaskMapper.selectByTaskNo(taskNo);
        AdminUserDO userDO = adminUserService.getUser(assessmentTaskDO.getPublishUserId());
        assessmentTaskDO.setPublishUser(userDO != null ? userDO.getNickname() : "");
        assessmentTaskDO.setTotalNum(userTaskMapper.selectCount(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo,taskNo)));
        assessmentTaskDO.setFinishNum(userTaskMapper.selectCount(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo,taskNo).eq(AssessmentUserTaskDO::getStatus,2)));
        return assessmentTaskDO;
    }

    @Override
    public PageResult<AssessmentTaskVO> getAssessmentTaskPage(AssessmentTaskPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<Long> taskNos = new ArrayList<>();
        DeptDataPermissionRespDTO deptDataPermissionRespDTO = permissionApi.getDeptDataPermission(userId);
        if (!deptDataPermissionRespDTO.getAll()){
            taskNos = deptTaskMapper.selectTaskListByDeptIds(deptDataPermissionRespDTO.getDeptIds());
        }
        IPage<AssessmentTaskVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        assessmentTaskMapper.selectPageList(page, pageReqVO, taskNos);
        return new PageResult<>(page.getRecords(), page.getTotal());
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
    public void addParticipants(AssessmentTaskParticipantsReqVO reqVO) {
        // 校验任务存在
        AssessmentTaskDO assessmentTaskDO = getAssessmentTaskByNo(reqVO.getTaskNo());
        if (Objects.isNull(assessmentTaskDO)){
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }
        for (Long userId : reqVO.getUserIds()) {
            // 校验用户档案存在
            if (adminUserService.getUser(userId) == null) {
                throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
            }

            // 校验是否已存在参与者
            if (userTaskMapper.selectByTaskNoAndUserId(reqVO.getTaskNo(), userId) != null) {
                throw exception(ErrorCodeConstants.ASSESSMENT_TASK_PARTICIPANT_EXISTS);
            }

            // 插入参与者记录
            AssessmentUserTaskDO userTaskDO = new AssessmentUserTaskDO();
            userTaskDO.setTaskNo(reqVO.getTaskNo());
            userTaskDO.setUserId(userId);
            userTaskDO.setParentFlag(assessmentTaskDO.getTargetAudience());
            userTaskDO.setStatus(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
            userTaskMapper.insert(userTaskDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeParticipants(AssessmentTaskParticipantsReqVO reqVO) {
        // 校验任务存在
        if (Objects.isNull(getAssessmentTaskByNo(reqVO.getTaskNo()))){
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NOT_EXISTS);
        }

        for (Long userId : reqVO.getUserIds()) {
            userTaskMapper.deleteByTaskNoAndUserId(reqVO.getTaskNo(), userId);
        }
    }

    @Override
    public AssessmentTaskStatisticsRespVO getTaskStatistics(String taskNo) {
        // 校验任务存在
        validateAssessmentTaskExists(taskNo);

        // 统计参与者信息
        long totalParticipants = userTaskMapper.selectCountByTaskNo(taskNo);
        long completedParticipants = userTaskMapper.selectCountByTaskNoAndStatus(taskNo, ParticipantCompletionStatusEnum.COMPLETED.getStatus());
        long inProgressParticipants = userTaskMapper.selectCountByTaskNoAndStatus(taskNo, ParticipantCompletionStatusEnum.IN_PROGRESS.getStatus());

        AssessmentTaskStatisticsRespVO statistics = new AssessmentTaskStatisticsRespVO();
        statistics.setTotalParticipants(totalParticipants);
        statistics.setCompletedParticipants(completedParticipants);
        statistics.setInProgressParticipants(inProgressParticipants);
        statistics.setNotStartedParticipants(totalParticipants - completedParticipants - inProgressParticipants);
        BigDecimal total = new BigDecimal(statistics.getTotalParticipants());
        BigDecimal completed = new BigDecimal(statistics.getCompletedParticipants());
        BigDecimal completionRate = completed.divide(total, 2, BigDecimal.ROUND_HALF_UP);
        statistics.setCompletionRate(totalParticipants > 0 ? completionRate.multiply(new BigDecimal("100")) : new BigDecimal("0.00"));
        return statistics;
    }

    @Override
    public AssessmentTaskDO getAssessmentTaskByNo(String taskNo) {
        return assessmentTaskMapper.selectByTaskNo(taskNo);
    }

    @Override
    public List<AssessmentTaskUserVO> selectListByTaskNo(String taskNo){
        return userTaskMapper.selectListByTaskNo(taskNo);
    }

    @Override
    public List<WebAssessmentTaskVO> selectListByUserId(){
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return assessmentTaskMapper.selectListByUserId(userId);
    }

}