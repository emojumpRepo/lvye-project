package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentTaskDetailVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentTaskHisVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskQuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentDeptTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskQuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.enums.AssessmentTaskStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.RiskLevelEnum;
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
import java.util.*;
import java.util.stream.Collectors;


import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * 测评任务 Service 实现类
 */
@Service
@Validated
@Slf4j
public class AssessmentTaskServiceImpl implements AssessmentTaskService {

    static final String SCHOOL_YEAR = "school.year";

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

    @Resource
    private ConfigApi configApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAssessmentTask(@Valid AssessmentTaskSaveReqVO createReqVO) {
        // 检查是否需要立即发布
        boolean isPublish = createReqVO.getIsPublish() != null && createReqVO.getIsPublish();
        return createAssessmentTask(createReqVO, isPublish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAssessmentTask(@Valid AssessmentTaskSaveReqVO createReqVO, boolean isPublish) {
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
            scenarioService.validateScenarioActive(createReqVO.getScenarioId());
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
            //userTaskList去重
            List<AssessmentUserTaskDO> uniqueuserTaskList = userTaskList.stream().collect(collectingAndThen(
                    toCollection(() -> new TreeSet<>(comparingLong(AssessmentUserTaskDO::getUserId))), ArrayList::new));
            userTaskMapper.insertBatch(uniqueuserTaskList);
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
        return assessmentTask.getTaskNo();
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

        // 只有在明确传入问卷ID列表时才重建问卷关联
        if (updateReqVO.getQuestionnaireIds() != null) {
            // 重建任务-问卷关联
            taskQuestionnaireMapper.deleteByTaskNo(updateReqVO.getTaskNo());
            if (!updateReqVO.getQuestionnaireIds().isEmpty()) {
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
    public AssessmentTaskStatisticsRespVO getTaskStatistics(String taskNo, Integer includeDeptTree) {
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

        // include dept tree if requested
        if (includeDeptTree != null && includeDeptTree == 1) {
            statistics.setDeptTree(buildDeptTreeStatistics(taskNo));
        }
        return statistics;
    }

    private List<cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentDeptNodeVO> buildDeptTreeStatistics(String taskNo) {
        // 1) 聚合查询：按年级/班级分组，得到 total/completed
        List<cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptAggregationRow> rows = userTaskMapper.selectAggregationByDept(taskNo);
        if (rows == null || rows.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        // 2) 收集所有涉及的 deptId（剔除 -1）
        java.util.Set<Long> deptIds = rows.stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getGradeDeptId(), r.getClassDeptId()))
                .filter(id -> id != null && id > 0)
                .collect(java.util.stream.Collectors.toSet());
        // 3) 批量查询部门名称与排序
        List<cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO> deptList = deptService.getDeptList(deptIds);
        Map<Long, cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO> deptMap = deptList.stream()
                .collect(Collectors.toMap(cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO::getId, d -> d));
        // 4) 先按年级分组，再生成班级 children
        Map<Long, List<cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptAggregationRow>> byGrade = rows.stream()
                .collect(Collectors.groupingBy(r -> r.getGradeDeptId() == null ? -1L : r.getGradeDeptId()));
        List<cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentDeptNodeVO> gradeNodes = new ArrayList<>();
        for (Map.Entry<Long, List<cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptAggregationRow>> e : byGrade.entrySet()) {
            Long gradeId = e.getKey();
            List<cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptAggregationRow> classRows = e.getValue();
            // 构建班级节点
            List<cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentDeptNodeVO> classNodes = new ArrayList<>();
            for (var r : classRows) {
                Long classId = r.getClassDeptId() == null ? -1L : r.getClassDeptId();
                cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO classDept = classId > 0 ? deptMap.get(classId) : null;
                String className = classDept != null ? classDept.getName() : "未知班级";
                long total = r.getTotalCnt() == null ? 0L : r.getTotalCnt();
                long completed = r.getCompletedCnt() == null ? 0L : r.getCompletedCnt();
                java.math.BigDecimal rate = total == 0 ? java.math.BigDecimal.ZERO
                        : new java.math.BigDecimal(completed).divide(new java.math.BigDecimal(total), 2, java.math.RoundingMode.HALF_UP)
                        .multiply(new java.math.BigDecimal("100"));
                cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentDeptNodeVO node = new cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentDeptNodeVO();
                node.setDeptId(classId);
                node.setDeptName(className);
                node.setTotalParticipants(total);
                node.setCompletedParticipants(completed);
                node.setCompletionRate(rate);
                classNodes.add(node);
            }
            // 班级排序：按 system_dept.sort 升序，未知(-1)最后
            classNodes.sort((a, b) -> {
                Long aId = a.getDeptId();
                Long bId = b.getDeptId();
                if (aId != null && aId < 0 && (bId == null || bId >= 0)) return 1;
                if (bId != null && bId < 0 && (aId == null || aId >= 0)) return -1;
                cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO ad = (aId != null && aId > 0) ? deptMap.get(aId) : null;
                cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO bd = (bId != null && bId > 0) ? deptMap.get(bId) : null;
                Integer as = ad != null ? ad.getSort() : Integer.MAX_VALUE;
                Integer bs = bd != null ? bd.getSort() : Integer.MAX_VALUE;
                return java.util.Objects.compare(as, bs, java.util.Comparator.naturalOrder());
            });
            // 年级节点汇总
            long gTotal = classRows.stream().mapToLong(r -> r.getTotalCnt() == null ? 0L : r.getTotalCnt()).sum();
            long gCompleted = classRows.stream().mapToLong(r -> r.getCompletedCnt() == null ? 0L : r.getCompletedCnt()).sum();
            java.math.BigDecimal gRate = gTotal == 0 ? java.math.BigDecimal.ZERO
                    : new java.math.BigDecimal(gCompleted).divide(new java.math.BigDecimal(gTotal), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(new java.math.BigDecimal("100"));
            cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO gradeDept = gradeId != null && gradeId > 0 ? deptMap.get(gradeId) : null;
            String gradeName = gradeDept != null ? gradeDept.getName() : "未知年级";
            cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentDeptNodeVO gNode = new cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentDeptNodeVO();
            gNode.setDeptId(gradeId);
            gNode.setDeptName(gradeName);
            gNode.setTotalParticipants(gTotal);
            gNode.setCompletedParticipants(gCompleted);
            gNode.setCompletionRate(gRate);
            gNode.setChildren(classNodes);
            gradeNodes.add(gNode);
        }
        // 年级排序：按 system_dept.sort 升序，未知(-1)最后
        gradeNodes.sort((a, b) -> {
            Long aId = a.getDeptId();
            Long bId = b.getDeptId();
            if (aId != null && aId < 0 && (bId == null || bId >= 0)) return 1;
            if (bId != null && bId < 0 && (aId == null || aId >= 0)) return -1;
            cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO ad = (aId != null && aId > 0) ? deptMap.get(aId) : null;
            cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO bd = (bId != null && bId > 0) ? deptMap.get(bId) : null;
            Integer as = ad != null ? ad.getSort() : Integer.MAX_VALUE;
            Integer bs = bd != null ? bd.getSort() : Integer.MAX_VALUE;
            return java.util.Objects.compare(as, bs, java.util.Comparator.naturalOrder());
        });
        return gradeNodes;
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
        log.info("查询用户任务列表，userId: {}, isParent: {}", userId, isParent);
        List<WebAssessmentTaskVO> list = assessmentTaskMapper.selectListByUserId(userId, isParent);
        log.info("查询到任务数量: {}", list != null ? list.size() : 0);
        if (list != null && !list.isEmpty()) {
            for (WebAssessmentTaskVO vo : list) {
                log.info("任务信息: taskNo={}, scenarioId={}", vo.getTaskNo(), vo.getScenarioId());
                if (vo.getQuestionnaireIds() == null && vo.getQuestionnaireIdsStr() != null) {
                    vo.setQuestionnaireIds(parseQuestionnaireIds(vo.getQuestionnaireIdsStr()));
                }
            }
        }
        return list;
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

    @Override
    public PageResult<QuestionnaireUserVO> selectQuestionnaireUserListByTaskNoAndQuestionnaire(QuestionnaireUserPageVO pageVO){
        // taskNo 必填保护
        if (pageVO.getTaskNo() == null || pageVO.getTaskNo().trim().isEmpty()) {
            return new PageResult<>(java.util.Collections.emptyList(), 0L);
        }
        IPage<QuestionnaireUserVO> page = new Page<>(pageVO.getPageNo(), pageVO.getPageSize());
        String qid = pageVO.getQuestionnaireId();
        boolean isAllByTaskNo = (qid == null || qid.trim().isEmpty() || "0".equals(qid.trim()));
        if (isAllByTaskNo) {
            userTaskMapper.selectQuestionnaireUserListByTaskNo(page, pageVO);
        } else {
            userTaskMapper.selectQuestionnaireUserListByTaskNoAndQuestionnaire(page, pageVO);
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<AssessmentTaskQuestionnaireDO> selectQuestionnaireListByTaskNo(String taskNo){
        return taskQuestionnaireMapper.selectListByTaskNo(taskNo, TenantContextHolder.getTenantId());
    }

    @Override
    public List<AssessmentTaskQuestionnaireDO> getTaskQuestionnairesByTaskNo(String taskNo) {
        return taskQuestionnaireMapper.selectListByTaskNo(taskNo, TenantContextHolder.getTenantId());
    }

    @Override
    public void updateExpireStatus() {
        List<AssessmentTaskDO> taskList = assessmentTaskMapper.selectList();
        for(AssessmentTaskDO taskDO : taskList){
            Date startline = taskDO.getStartline();
            Date endline = taskDO.getDeadline();
            if(!DateUtils.isInDateRange(new Date(), startline, endline)){
                log.info("任务：" + taskDO.getTaskNo() + "已到deadline，关闭任务");
                taskDO.setStatus(AssessmentTaskStatusEnum.CLOSED.getStatus());
                assessmentTaskMapper.updateStatusByTaskNo(taskDO);
            }
        }
    }

    @Override
    public AssessmentTaskRiskLevelStatisticsVO getTaskRiskStatistics(String taskNo){
        AssessmentTaskRiskLevelStatisticsVO assessmentTaskRiskLevelStatisticsVO = new AssessmentTaskRiskLevelStatisticsVO();
        String schoolYear = configApi.getConfigValueByKey(SCHOOL_YEAR);
        List<RiskLevelDeptStatisticsVO> riskLevelDeptStatisticsList = userTaskMapper.selectRiskLevelListByTaskNo(taskNo, schoolYear);
        //计算年级/班级风险数量
        Set<Long> gradeDeptIds = new HashSet<>();
        List<RiskLevelStatisticsVO> totalList = new ArrayList<>();
        //计算风险等级总数
        List<RiskLevelGradeStatisticsVO> gradeList = new ArrayList<>();
        Map<Integer, Integer> map = new HashMap<>();
        map.put(RiskLevelEnum.NORMAL.getLevel(), 0);
        map.put(RiskLevelEnum.ATTENTION.getLevel(), 0);
        map.put(RiskLevelEnum.WARNING.getLevel(), 0);
        map.put(RiskLevelEnum.HIGH_RISK.getLevel(), 0);
        if (riskLevelDeptStatisticsList != null && !riskLevelDeptStatisticsList.isEmpty()){
            for (RiskLevelDeptStatisticsVO statistics : riskLevelDeptStatisticsList){
                if (statistics.getRiskLevel() != null){
                    Integer count = map.get(RiskLevelEnum.fromLevel(statistics.getRiskLevel()).getLevel()) + 1;
                    map.put(RiskLevelEnum.fromLevel(statistics.getRiskLevel()).getLevel(),count);
                }
                gradeDeptIds.add(statistics.getGradeDeptId());
            }
        }
        //map转对象
        for (Map.Entry<Integer, Integer> entry : map.entrySet()){
            RiskLevelStatisticsVO riskLevelStatisticsVO = new RiskLevelStatisticsVO();
            riskLevelStatisticsVO.setRiskLevel(entry.getKey());
            riskLevelStatisticsVO.setCount(entry.getValue());
            totalList.add(riskLevelStatisticsVO);
        }

        assessmentTaskRiskLevelStatisticsVO.setTotalList(totalList);

        for (Long gradeDeptId : gradeDeptIds){
            Map<Integer, Integer> gradeMap = new HashMap<>();
            gradeMap.put(RiskLevelEnum.NORMAL.getLevel(), 0);
            gradeMap.put(RiskLevelEnum.ATTENTION.getLevel(), 0);
            gradeMap.put(RiskLevelEnum.WARNING.getLevel(), 0);
            gradeMap.put(RiskLevelEnum.HIGH_RISK.getLevel(), 0);
            List<RiskLevelStatisticsVO> riskLevelGradeStatisticsList = new ArrayList<>();
            for (RiskLevelDeptStatisticsVO statistics : riskLevelDeptStatisticsList){
                if (statistics.getRiskLevel() != null && statistics.getGradeDeptId().equals(gradeDeptId)){
                    Integer count = gradeMap.get(RiskLevelEnum.fromLevel(statistics.getRiskLevel()).getLevel()) + 1;
                    gradeMap.put(RiskLevelEnum.fromLevel(statistics.getRiskLevel()).getLevel(),count);
                }
            }
            //map转对象
            for (Map.Entry<Integer, Integer> entry : map.entrySet()){
                RiskLevelStatisticsVO riskLevelStatisticsVO = new RiskLevelStatisticsVO();
                riskLevelStatisticsVO.setRiskLevel(entry.getKey());
                riskLevelStatisticsVO.setCount(entry.getValue());
                riskLevelGradeStatisticsList.add(riskLevelStatisticsVO);
            }
            RiskLevelGradeStatisticsVO riskLevelGradeStatisticsVO = new RiskLevelGradeStatisticsVO();
            riskLevelGradeStatisticsVO.setGradeDeptId(gradeDeptId);
            riskLevelGradeStatisticsVO.setGradeName(deptService.getDept(gradeDeptId).getName());
            riskLevelGradeStatisticsVO.setRiskLevelList(riskLevelGradeStatisticsList);
            gradeList.add(riskLevelGradeStatisticsVO);
        }
        assessmentTaskRiskLevelStatisticsVO.setGradeList(gradeList);
        return assessmentTaskRiskLevelStatisticsVO;
    }

}