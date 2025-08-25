package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentTaskDetailVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentTaskHisVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentScenarioDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentScenarioSlotDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskQuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentDeptTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentScenarioService;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskQuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.enums.AssessmentTaskStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
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
    private AssessmentTaskQuestionnaireMapper taskQuestionnaireMapper;

    @Resource
    private AssessmentScenarioService scenarioService;

    @Resource
    private PermissionApi permissionApi;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;

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
        // 校验场景（如选择）并写入任务
        if (createReqVO.getScenarioId() != null) {
            AssessmentScenarioDO scenario = scenarioService.validateScenarioActive(createReqVO.getScenarioId());
            assessmentTask.setScenarioId(createReqVO.getScenarioId());

            // 校验问卷数量限制
            int questionnaireCount = 0;
            if (createReqVO.getAssignments() != null) {
                questionnaireCount = createReqVO.getAssignments().size();
            } else if (createReqVO.getQuestionnaireIds() != null) {
                questionnaireCount = createReqVO.getQuestionnaireIds().size();
            }
            scenarioService.validateQuestionnaireCount(createReqVO.getScenarioId(), questionnaireCount);
        }
        assessmentTaskMapper.insert(assessmentTask);

        // 维护任务-问卷关联（多选）
        if (createReqVO.getAssignments() != null && !createReqVO.getAssignments().isEmpty()) {
            // 有槽位分配，逐条写入并带 slotKey
            for (var a : createReqVO.getAssignments()) {
                AssessmentTaskQuestionnaireDO rel = new AssessmentTaskQuestionnaireDO();
                rel.setTaskNo(createReqVO.getTaskNo());
                rel.setQuestionnaireId(a.getQuestionnaireId());
                rel.setTenantId(TenantContextHolder.getTenantId());
                rel.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                rel.setUpdater(rel.getCreator());
                rel.setSlotKey(a.getSlotKey());
                taskQuestionnaireMapper.insert(rel);
            }
        } else if (createReqVO.getQuestionnaireIds() != null && !createReqVO.getQuestionnaireIds().isEmpty()) {
            // 无槽位场景，按 questionnaireIds 简单写入
            for (Long qid : createReqVO.getQuestionnaireIds()) {
                AssessmentTaskQuestionnaireDO rel = new AssessmentTaskQuestionnaireDO();
                rel.setTaskNo(createReqVO.getTaskNo());
                rel.setQuestionnaireId(qid);
                rel.setTenantId(TenantContextHolder.getTenantId());
                rel.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                rel.setUpdater(rel.getCreator());
                taskQuestionnaireMapper.insert(rel);
            }
        }

        // 插入部门测评关联信息
        List<AssessmentDeptTaskDO> deptTaskList = new ArrayList<>();
        List<Long> deptIds = new ArrayList<>();

        if (createReqVO.getDeptIdList() != null && !createReqVO.getDeptIdList().isEmpty()) {
            List<DeptDO> deptList = deptService.getChildDeptList(createReqVO.getDeptIdList());
            //父部门
            for (Long deptId : createReqVO.getDeptIdList()) {
                AssessmentDeptTaskDO assessmentDeptTaskDO = new AssessmentDeptTaskDO();
                assessmentDeptTaskDO.setTaskNo(createReqVO.getTaskNo());
                assessmentDeptTaskDO.setDeptId(deptId);
                deptTaskList.add(assessmentDeptTaskDO);
                deptIds.add(deptId);
            }
            //子部门
            if (deptList != null) {
                for (DeptDO deptDO : deptList) {
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
            List<StudentProfileDO> userList = studentProfileService.getStudentListByClassIds(deptIds);
            if (!CollUtil.isEmpty(userList)) {
                for (StudentProfileDO studentProfileDO : userList) {
                    AssessmentUserTaskDO assessmentUserTaskDO = new AssessmentUserTaskDO();
                    assessmentUserTaskDO.setTaskNo(createReqVO.getTaskNo());
                    assessmentUserTaskDO.setUserId(studentProfileDO.getUserId());
                    assessmentUserTaskDO.setParentFlag(createReqVO.getTargetAudience());
                    assessmentUserTaskDO.setStatus(ParticipantCompletionStatusEnum.NOT_STARTED.getStatus());
                    userTaskList.add(assessmentUserTaskDO);
                }
            }
        }

        //请求报文的用户
        if (createReqVO.getUserIdList() != null && !createReqVO.getUserIdList().isEmpty()) {
            for (Long userId : createReqVO.getUserIdList()) {
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

        // 重建任务-问卷关联
        taskQuestionnaireMapper.deleteByTaskNo(updateReqVO.getTaskNo());
        if (updateReqVO.getQuestionnaireIds() != null && !updateReqVO.getQuestionnaireIds().isEmpty()) {
            List<AssessmentTaskQuestionnaireDO> relations = new ArrayList<>();
            for (Long qid : updateReqVO.getQuestionnaireIds()) {
                AssessmentTaskQuestionnaireDO rel = new AssessmentTaskQuestionnaireDO();
                rel.setTaskNo(updateReqVO.getTaskNo());
                rel.setQuestionnaireId(qid);
                rel.setTenantId(TenantContextHolder.getTenantId());
                relations.add(rel);
            }
            taskQuestionnaireMapper.insertBatch(relations);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAssessmentTask(String taskNo) {
        // 校验存在
        AssessmentTaskDO assessmentTaskDO = getAssessmentTaskByNo(taskNo);
        if (!Objects.isNull(assessmentTaskDO)) {
            // 删除
            assessmentTaskMapper.deleteById(assessmentTaskDO.getId());
            // 删除相关参与者
            userTaskMapper.deleteByTaskNo(taskNo);
            // 删除相关年级/班级你
            deptTaskMapper.deleteByTaskNo(taskNo);
            // 删除任务-问卷关联
            taskQuestionnaireMapper.deleteByTaskNo(taskNo);
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
        AssessmentTaskDO task = assessmentTaskMapper.selectByTaskNo(taskNo);
        if (task == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的任务
        if (id == null) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NO_DUPLICATE);
        }
        if (!Objects.equals(task.getId(), id)) {
            throw exception(ErrorCodeConstants.ASSESSMENT_TASK_NO_DUPLICATE);
        }
    }

    @Override
    public AssessmentTaskDO getAssessmentTask(String taskNo) {
        AssessmentTaskDO assessmentTaskDO = assessmentTaskMapper.selectByTaskNo(taskNo);
        AdminUserDO userDO = adminUserService.getUser(assessmentTaskDO.getPublishUserId());
        assessmentTaskDO.setPublishUser(userDO != null ? userDO.getNickname() : "");
        assessmentTaskDO.setTotalNum(userTaskMapper.selectCount(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)));
        assessmentTaskDO.setFinishNum(userTaskMapper.selectCount(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo).eq(AssessmentUserTaskDO::getStatus, 2)));
        // 填充关联问卷 ID 列表
        List<Long> qids = taskQuestionnaireMapper.selectQuestionnaireIdsByTaskNo(taskNo, TenantContextHolder.getTenantId());
        assessmentTaskDO.setQuestionnaireIds(qids);
        return assessmentTaskDO;
    }

    @Override
    public PageResult<AssessmentTaskVO> getAssessmentTaskPage(AssessmentTaskPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<Long> taskNos = new ArrayList<>();
        DeptDataPermissionRespDTO deptDataPermissionRespDTO = permissionApi.getDeptDataPermission(userId);
        if (!deptDataPermissionRespDTO.getAll()) {
            taskNos = deptTaskMapper.selectTaskListByDeptIds(deptDataPermissionRespDTO.getDeptIds());
        }
        IPage<AssessmentTaskVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        assessmentTaskMapper.selectPageList(page, pageReqVO, taskNos);
        // 填充每条记录的问卷ID列表与槽位映射
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            for (AssessmentTaskVO record : page.getRecords()) {
                List<Long> qids = taskQuestionnaireMapper.selectQuestionnaireIdsByTaskNo(record.getTaskNo(), TenantContextHolder.getTenantId());
                record.setQuestionnaireIds(qids);
                var items = new java.util.ArrayList<cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.SlotAssignmentVO>();
                var rows = taskQuestionnaireMapper.selectListByTaskNo(record.getTaskNo(), TenantContextHolder.getTenantId());
                if (rows != null) {
                    for (var r : rows) {
                        if (r.getSlotKey() != null) {
                            var item = new cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.SlotAssignmentVO();
                            item.setSlotKey(r.getSlotKey());
                            item.setQuestionnaireId(r.getQuestionnaireId());
                            item.setSlotOrder(r.getSlotOrder());
                            items.add(item);
                        }
                    }
                }
                record.setAssignments(items);
            }
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    /**
     * 解析问卷ID字符串为List
     */
    private List<Long> parseQuestionnaireIds(String questionnaireIdsStr) {
        List<Long> result = new ArrayList<>();
        if (questionnaireIdsStr != null && !questionnaireIdsStr.trim().isEmpty()) {
            String[] ids = questionnaireIdsStr.split(",");
            for (String id : ids) {
                try {
                    result.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    log.warn("Invalid questionnaire ID: {}", id);
                }
            }
        }
        return result;
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
        if (Objects.isNull(assessmentTaskDO)) {
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
        if (Objects.isNull(getAssessmentTaskByNo(reqVO.getTaskNo()))) {
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
        BigDecimal completionRate = total.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : completed.divide(total, 2, java.math.RoundingMode.HALF_UP);
        statistics.setCompletionRate(totalParticipants > 0 ? completionRate.multiply(new BigDecimal("100")) : new BigDecimal("0.00"));
        return statistics;
    }

    @Override
    public AssessmentTaskDO getAssessmentTaskByNo(String taskNo) {
        return assessmentTaskMapper.selectByTaskNo(taskNo);
    }

    @Override
    public List<AssessmentTaskUserVO> selectListByTaskNo(String taskNo) {
        return userTaskMapper.selectListByTaskNo(taskNo);
    }

    @Override
    public List<WebAssessmentTaskVO> selectListByUserId() {
        Long userId = WebFrameworkUtils.getLoginUserId();
        Integer isParent = WebFrameworkUtils.getIsParent();
        return assessmentTaskMapper.selectListByUserId(userId, isParent);
    }

    @Override
    public List<StudentAssessmentTaskHisVO> selectStudentAssessmentTaskList(Long studentProfileId) {
        List<StudentAssessmentTaskHisVO> result = userTaskMapper.selectStudentAssessmentTaskList(studentProfileId);
        return result;
    }

    @Override
    public StudentAssessmentTaskDetailVO selectStudentAssessmentTaskDetail(String taskNo, Long studentProfileId) {
        StudentProfileVO studentProfileVO = studentProfileService.getStudentProfile(studentProfileId);
        if (studentProfileVO == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
        StudentAssessmentTaskDetailVO detail = userTaskMapper.selectStudentAssessmentTaskDetail(studentProfileId, taskNo);
        //问卷内容
        List<AssessmentTaskQuestionnaireDO> questionnaireList = taskQuestionnaireMapper.selectListByTaskNo(taskNo, TenantContextHolder.getTenantId());
        if (!questionnaireList.isEmpty()) {
            detail.setQuestionnaireList(questionnaireList);
        }
        //问卷结果
        List<StudentAssessmentQuestionnaireDetailVO> questionnaireDetailList = new ArrayList<>();
        List<QuestionnaireResultDO> questionnaireResultList = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, studentProfileVO.getUserId());
        if (detail != null && !questionnaireResultList.isEmpty()) {
            for (QuestionnaireResultDO questionnaireResultDO : questionnaireResultList) {
                StudentAssessmentQuestionnaireDetailVO questionnaireDetailVO = BeanUtils.toBean(questionnaireResultDO, StudentAssessmentQuestionnaireDetailVO.class);
                questionnaireDetailList.add(questionnaireDetailVO);
            }
        }
        detail.setQuestionnaireDetailList(questionnaireDetailList);
        return detail;
    }

}