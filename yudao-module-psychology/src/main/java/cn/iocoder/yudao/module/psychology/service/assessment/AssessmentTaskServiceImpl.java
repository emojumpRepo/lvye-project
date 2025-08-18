package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.psychology.service.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentParticipantMapper;
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
        // 简化实现：创建基本任务
        AssessmentTaskDO assessmentTask = BeanUtils.toBean(createReqVO, AssessmentTaskDO.class);
        if (assessmentTask.getTaskNo() == null) {
            assessmentTask.setTaskNo("TASK_" + System.currentTimeMillis());
        }
        assessmentTaskMapper.insert(assessmentTask);
        return assessmentTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssessmentTask(@Valid AssessmentTaskSaveReqVO createReqVO, boolean isPublish) {
        // 简化实现：创建任务并可选发布
        AssessmentTaskDO assessmentTask = BeanUtils.toBean(createReqVO, AssessmentTaskDO.class);
        assessmentTask.setTaskNo("TASK_" + System.currentTimeMillis());
        assessmentTaskMapper.insert(assessmentTask);
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

        // 根据任务编号删除任务
        AssessmentTaskDO task = assessmentTaskMapper.selectByTaskNo(taskNo);
        if (task != null) {
            assessmentTaskMapper.deleteById(task.getId());
        }
    }

    @Override
    public AssessmentTaskDO getAssessmentTask(String taskNo) {
        return assessmentTaskMapper.selectByTaskNo(taskNo);
    }

    @Override
    public AssessmentTaskDO getAssessmentTaskByNo(String taskNo) {
        return assessmentTaskMapper.selectByTaskNo(taskNo);
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
        // 简化实现：更新状态
    }

    @Override
    public void closeAssessmentTask(String taskNo) {
        // 简化实现：更新状态
    }

    @Override
    public void addParticipants(AssessmentTaskParticipantsReqVO reqVO) {
        // 简化实现：添加参与者
    }

    @Override
    public void removeParticipants(AssessmentTaskParticipantsReqVO reqVO) {
        // 简化实现：移除参与者
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
        statistics.setCompletionRate(totalParticipants > 0 ? completionRate : new BigDecimal("0.00"));
        return statistics;
    }

    @Override
    public List<AssessmentTaskUserVO> selectListByTaskNo(String taskNo) {
        // 简化实现：返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<WebAssessmentTaskVO> selectListByUserId(Long userId) {
        // 简化实现：返回空列表
        return new ArrayList<>();
    }

    @Override
    public void validateTaskNameUnique(Long id, String taskName) {
        // 简化实现：跳过验证
    }

}