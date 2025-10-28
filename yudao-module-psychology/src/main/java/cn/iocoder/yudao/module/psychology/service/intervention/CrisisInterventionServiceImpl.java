package cn.iocoder.yudao.module.psychology.service.intervention;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisEventAssessmentDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisEventProcessDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisInterventionDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.intervention.InterventionLevelHistoryDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentResultMapper;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisEventAssessmentMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisEventProcessMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisInterventionMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.intervention.InterventionLevelHistoryMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentInterventionMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.enums.TimelineEventTypeEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.psychology.service.profile.StudentTimelineService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.psychology.service.counselor.StudentCounselorAssignmentService;
import cn.iocoder.yudao.module.infra.service.config.ConfigService;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.psychology.enums.InterventionAssignmentModeEnum;
import cn.iocoder.yudao.module.psychology.enums.DictTypeConstants;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserDeptMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserDeptDO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.*;

/**
 * 危机干预 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class CrisisInterventionServiceImpl implements CrisisInterventionService {

    @Resource
    private CrisisInterventionMapper crisisInterventionMapper;

    @Resource
    private CrisisEventProcessMapper eventProcessMapper;

    @Resource
    private CrisisEventAssessmentMapper eventAssessmentMapper;

    @Resource
    private StudentProfileService studentProfileService;
    
    @Resource
    private StudentInterventionMapper studentInterventionMapper;
    
    @Resource
    private InterventionLevelHistoryMapper levelHistoryMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private StudentTimelineService studentTimelineService;
    
    @Resource
    private StudentCounselorAssignmentService studentCounselorAssignmentService;

    @Resource
    private ConfigService configService;

    @Resource
    private AssessmentTaskMapper assessmentTaskMapper;

    @Resource
    private AssessmentUserTaskMapper assessmentUserTaskMapper;

    @Resource
    private AssessmentResultMapper assessmentResultMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private UserDeptMapper userDeptMapper;

    // 配置键常量
    private static final String CONFIG_KEY_ASSIGNMENT_MODE = "intervention.assignment.mode";

    @Override
    public InterventionDashboardSummaryVO getDashboardSummaryWithPage(InterventionDashboardReqVO reqVO) {
        InterventionDashboardSummaryVO summary = new InterventionDashboardSummaryVO();
        
        // 如果是只看我负责的
        if (Boolean.TRUE.equals(reqVO.getOnlyMine())) {
            reqVO.setCounselorUserId(SecurityFrameworkUtils.getLoginUserId());
        }
        
        // 获取统计数据（这里需要根据查询条件过滤）
        // 字典类型：crisis_level (1:待评, 2:持续观察, 3:一般, 4:严重, 5:重大)
        summary.setPendingAssessmentCount(getCountByRiskLevelWithFilter(1, reqVO)); // 待评
        summary.setObservationCount(getCountByRiskLevelWithFilter(2, reqVO));      // 持续观察
        summary.setGeneralCount(getCountByRiskLevelWithFilter(3, reqVO));          // 一般
        summary.setSevereCount(getCountByRiskLevelWithFilter(4, reqVO));           // 严重
        summary.setMajorCount(getCountByRiskLevelWithFilter(5, reqVO));            // 重大
        summary.setNormalCount(0); // 不再使用正常状态
        
        // 计算总数
        int total = summary.getPendingAssessmentCount() + summary.getObservationCount() + 
                   summary.getGeneralCount() + summary.getSevereCount() + 
                   summary.getMajorCount();
        summary.setTotalCount(total);
        
        // 构建详细统计
        Map<String, InterventionDashboardSummaryVO.LevelDetail> details = new HashMap<>();
        addLevelDetail(details, "pending", summary.getPendingAssessmentCount(), total); // 待评
        addLevelDetail(details, "observation", summary.getObservationCount(), total);   // 持续观察
        addLevelDetail(details, "general", summary.getGeneralCount(), total);           // 一般
        addLevelDetail(details, "severe", summary.getSevereCount(), total);             // 严重
        addLevelDetail(details, "major", summary.getMajorCount(), total);               // 重大
        summary.setLevelDetails(details);
        
        // 根据是否指定风险等级来获取学生列表
        // 如果指定了风险等级，只返回该等级的学生（用于分页点击）
        // 如果没有指定，返回所有学生（用于首次加载）
        PageResult<InterventionStudentRespVO> studentPage = getStudentPageByFilter(reqVO);
        summary.setStudentPage(studentPage);
        
        return summary;
    }

    private Integer getCountByRiskLevelWithFilter(Integer riskLevel, InterventionDashboardReqVO reqVO) {
        Long count = studentInterventionMapper.countByRiskLevelWithFilter(
            riskLevel, 
            reqVO.getClassId(), 
            reqVO.getGradeId(), 
            reqVO.getCounselorUserId()
        );
        return count != null ? count.intValue() : 0;
    }

    private Integer getPendingAssessmentCountWithFilter(InterventionDashboardReqVO reqVO) {
        Long count = studentInterventionMapper.countPendingAssessmentWithFilter(
            reqVO.getClassId(), 
            reqVO.getGradeId(), 
            reqVO.getCounselorUserId()
        );
        return count != null ? count.intValue() : 0;
    }

    private PageResult<InterventionStudentRespVO> getStudentPageByFilter(InterventionDashboardReqVO reqVO) {
        // 计算分页偏移量
        Integer offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        
        // 查询学生列表
        List<InterventionStudentRespVO> students = studentInterventionMapper.selectInterventionStudentPage(
            reqVO, offset, reqVO.getPageSize()
        );
        
        // 查询总数
        Long total = studentInterventionMapper.countInterventionStudent(reqVO);
        
        // 填充额外信息（如负责心理老师等）
        if (CollUtil.isNotEmpty(students)) {
            // TODO: 批量获取心理老师信息
            for (InterventionStudentRespVO student : students) {
                // 设置默认标签
                student.setTags(generateStudentTags(student));
            }
        }
        
        return new PageResult<>(students, total);
    }
    
    private PageResult<InterventionStudentRespVO> getPendingStudentPageByFilter(InterventionDashboardReqVO reqVO) {
        // 设置查询待评估状态
        reqVO.setRiskLevel(1); // 1 表示待评
        return getStudentPageByFilter(reqVO);
    }
    
    private String[] generateStudentTags(InterventionStudentRespVO student) {
        List<String> tags = new ArrayList<>();
        
        // 根据风险等级添加标签 (字典类型：crisis_level)
        if (student.getCurrentRiskLevel() != null) {
            switch (student.getCurrentRiskLevel()) {
                case 1: tags.add("待评"); break;
                case 2: tags.add("持续观察"); break;
                case 3: tags.add("一般"); break;
                case 4: tags.add("严重"); break;
                case 5: tags.add("重大"); break;
            }
        }
        
        // 根据危机事件和咨询次数添加标签
        if (student.getCrisisEventCount() != null && student.getCrisisEventCount() > 0) {
            tags.add("有危机事件");
        }
        if (student.getConsultationCount() != null && student.getConsultationCount() >= 3) {
            tags.add("频繁咨询");
        }
        
        return tags.toArray(new String[0]);
    }

    private void addLevelDetail(Map<String, InterventionDashboardSummaryVO.LevelDetail> details, 
                                String level, Integer count, Integer total) {
        InterventionDashboardSummaryVO.LevelDetail detail = new InterventionDashboardSummaryVO.LevelDetail();
        detail.setCount(count);
        // 计算百分比并保留2位小数
        double percentage = total > 0 ? (count * 100.0 / total) : 0;
        detail.setPercentage(Math.round(percentage * 100.0) / 100.0);
        detail.setChange(0); // TODO: 计算环比变化
        details.put(level, detail);
    }

    @Override
    public InterventionDashboardSummaryVO getDashboardSummary(Long classId, Long counselorUserId) {
        // 转换为新的查询对象，用于获取首屏全部数据
        InterventionDashboardReqVO reqVO = new InterventionDashboardReqVO();
        reqVO.setClassId(classId);
        reqVO.setCounselorUserId(counselorUserId);
        reqVO.setPageNo(1);
        reqVO.setPageSize(1000); // 返回所有数据，不分页
        
        InterventionDashboardSummaryVO summary = new InterventionDashboardSummaryVO();
        
        // 获取统计数据
        // 字典类型：crisis_level (1:一般, 2:严重, 3:重大, 4:持续观察, 5:待评)
        summary.setGeneralCount(getCountByRiskLevelWithFilter(1, reqVO));      // 一般
        summary.setSevereCount(getCountByRiskLevelWithFilter(2, reqVO));       // 严重
        summary.setMajorCount(getCountByRiskLevelWithFilter(3, reqVO));        // 重大
        summary.setObservationCount(getCountByRiskLevelWithFilter(4, reqVO));  // 持续观察
        summary.setPendingAssessmentCount(getCountByRiskLevelWithFilter(5, reqVO)); // 待评
        summary.setNormalCount(0); // 不再使用正常状态
        
        // 计算总数
        int total = summary.getPendingAssessmentCount() + summary.getObservationCount() + 
                   summary.getGeneralCount() + summary.getSevereCount() + 
                   summary.getMajorCount();
        summary.setTotalCount(total);
        
        // 构建详细统计
        Map<String, InterventionDashboardSummaryVO.LevelDetail> details = new HashMap<>();
        addLevelDetail(details, "pending", summary.getPendingAssessmentCount(), total); // 待评
        addLevelDetail(details, "observation", summary.getObservationCount(), total);   // 持续观察
        addLevelDetail(details, "general", summary.getGeneralCount(), total);           // 一般
        addLevelDetail(details, "severe", summary.getSevereCount(), total);             // 严重
        addLevelDetail(details, "major", summary.getMajorCount(), total);               // 重大
        summary.setLevelDetails(details);
        
        // 获取所有等级的学生列表（不分页，返回完整数据）
        Map<String, PageResult<InterventionStudentRespVO>> allLevelStudents = new HashMap<>();
        
        // 获取每个风险等级的学生列表
        if (summary.getPendingAssessmentCount() > 0) {
            reqVO.setRiskLevel(1);
            allLevelStudents.put("pending", getStudentPageByFilter(reqVO));
        }
        if (summary.getObservationCount() > 0) {
            reqVO.setRiskLevel(2);
            allLevelStudents.put("observation", getStudentPageByFilter(reqVO));
        }
        if (summary.getGeneralCount() > 0) {
            reqVO.setRiskLevel(3);
            allLevelStudents.put("general", getStudentPageByFilter(reqVO));
        }
        if (summary.getSevereCount() > 0) {
            reqVO.setRiskLevel(4);
            allLevelStudents.put("severe", getStudentPageByFilter(reqVO));
        }
        if (summary.getMajorCount() > 0) {
            reqVO.setRiskLevel(5);
            allLevelStudents.put("major", getStudentPageByFilter(reqVO));
        }
        
        // 将所有学生合并为一个列表返回（为了兼容性）
        List<InterventionStudentRespVO> allStudents = new ArrayList<>();
        Long totalStudents = 0L;
        for (PageResult<InterventionStudentRespVO> page : allLevelStudents.values()) {
            allStudents.addAll(page.getList());
            totalStudents += page.getTotal();
        }
        summary.setStudentPage(new PageResult<>(allStudents, totalStudents));
        
        return summary;
    }

    public List<InterventionDashboardLevelVO> getDashboardLevels(Long classId, Long counselorUserId) {
        // 调用重载方法，使用默认pageSize
        return getDashboardLevels(classId, counselorUserId, 10);
    }
    
    @Override
    public List<InterventionDashboardLevelVO> getDashboardLevels(Long classId, Long counselorUserId, Integer pageSize) {
        // 设置默认值
        if (pageSize == null || pageSize <= 0) {
            pageSize = 5;
        }
        
        // 转换为新的查询对象，用于获取首屏全部数据
        InterventionDashboardReqVO reqVO = new InterventionDashboardReqVO();
        reqVO.setClassId(classId);
        reqVO.setCounselorUserId(counselorUserId);
        reqVO.setPageNo(1);
        reqVO.setPageSize(pageSize); // 使用传入的pageSize限制每个等级返回的学生数量
        
        // 获取各等级统计数据
        List<InterventionDashboardLevelVO> levels = new ArrayList<>();
        
        // 字典类型：crisis_level (1:待评, 2:持续观察, 3:一般, 4:严重, 5:重大)
        // 获取各等级学生数量
        Integer pendingCount = getCountByRiskLevelWithFilter(1, reqVO);
        Integer observationCount = getCountByRiskLevelWithFilter(2, reqVO);
        Integer generalCount = getCountByRiskLevelWithFilter(3, reqVO);
        Integer severeCount = getCountByRiskLevelWithFilter(4, reqVO);
        Integer majorCount = getCountByRiskLevelWithFilter(5, reqVO);
        
        // 计算总数
        int total = pendingCount + observationCount + generalCount + severeCount + majorCount;
        
        // 创建待评等级
        InterventionDashboardLevelVO pending = new InterventionDashboardLevelVO();
        pending.setType("pending");
        pending.setLabel("待评");
        pending.setDictValue(1); // 字典值：1
        pending.setCount(pendingCount);
        pending.setPercentage(total > 0 ? Math.round(pendingCount * 100.0 / total * 100.0) / 100.0 : 0.0);
        if (pendingCount > 0) {
            reqVO.setRiskLevel(1);
            pending.setStudentPage(getStudentPageByFilter(reqVO));
        } else {
            pending.setStudentPage(new PageResult<>(new ArrayList<>(), 0L));
        }
        levels.add(pending);
        
        // 创建持续观察等级
        InterventionDashboardLevelVO observation = new InterventionDashboardLevelVO();
        observation.setType("observation");
        observation.setLabel("持续观察");
        observation.setDictValue(2); // 字典值：2
        observation.setCount(observationCount);
        observation.setPercentage(total > 0 ? Math.round(observationCount * 100.0 / total * 100.0) / 100.0 : 0.0);
        if (observationCount > 0) {
            reqVO.setRiskLevel(2);
            observation.setStudentPage(getStudentPageByFilter(reqVO));
        } else {
            observation.setStudentPage(new PageResult<>(new ArrayList<>(), 0L));
        }
        levels.add(observation);
        
        // 创建一般等级
        InterventionDashboardLevelVO general = new InterventionDashboardLevelVO();
        general.setType("general");
        general.setLabel("一般");
        general.setDictValue(3); // 字典值：3
        general.setCount(generalCount);
        general.setPercentage(total > 0 ? Math.round(generalCount * 100.0 / total * 100.0) / 100.0 : 0.0);
        if (generalCount > 0) {
            reqVO.setRiskLevel(3);
            general.setStudentPage(getStudentPageByFilter(reqVO));
        } else {
            general.setStudentPage(new PageResult<>(new ArrayList<>(), 0L));
        }
        levels.add(general);
        
        // 创建严重等级
        InterventionDashboardLevelVO severe = new InterventionDashboardLevelVO();
        severe.setType("severe");
        severe.setLabel("严重");
        severe.setDictValue(4); // 字典值：4
        severe.setCount(severeCount);
        severe.setPercentage(total > 0 ? Math.round(severeCount * 100.0 / total * 100.0) / 100.0 : 0.0);
        if (severeCount > 0) {
            reqVO.setRiskLevel(4);
            severe.setStudentPage(getStudentPageByFilter(reqVO));
        } else {
            severe.setStudentPage(new PageResult<>(new ArrayList<>(), 0L));
        }
        levels.add(severe);
        
        // 创建重大等级
        InterventionDashboardLevelVO major = new InterventionDashboardLevelVO();
        major.setType("major");
        major.setLabel("重大");
        major.setDictValue(5); // 字典值：5
        major.setCount(majorCount);
        major.setPercentage(total > 0 ? Math.round(majorCount * 100.0 / total * 100.0) / 100.0 : 0.0);
        if (majorCount > 0) {
            reqVO.setRiskLevel(5);
            major.setStudentPage(getStudentPageByFilter(reqVO));
        } else {
            major.setStudentPage(new PageResult<>(new ArrayList<>(), 0L));
        }
        levels.add(major);
        
        return levels;
    }

    @Override
    public PageResult<InterventionStudentRespVO> getStudentsByLevel(String level, InterventionStudentPageReqVO pageReqVO) {
        // TODO: 实现按等级查询学生列表
        // 需要关联学生档案表和风险等级信息
        return PageResult.empty();
    }

    @Override
    public PageResult<InterventionStudentRespVO> getStudentsByRiskLevel(
            Integer riskLevel, Long classId, Long counselorUserId, Integer pageNo, Integer pageSize) {
        // 构建查询对象
        InterventionDashboardReqVO reqVO = new InterventionDashboardReqVO();
        reqVO.setRiskLevel(riskLevel);
        reqVO.setClassId(classId);
        reqVO.setCounselorUserId(counselorUserId);
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        
        // 复用现有的分页查询方法
        return getStudentPageByFilter(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStudentLevel(Long studentProfileId, StudentLevelAdjustReqVO adjustReqVO) {
        // 验证学生是否存在
        StudentProfileVO student = studentProfileService.getStudentProfile(studentProfileId);
        if (student == null) {
            throw ServiceExceptionUtil.exception(STUDENT_PROFILE_NOT_EXISTS);
        }

        // 验证风险等级值范围（1-5）
        Integer targetLevel = adjustReqVO.getTargetLevel();
        if (targetLevel == null || targetLevel < 1 || targetLevel > 5) {
            throw ServiceExceptionUtil.exception(INVALID_RISK_LEVEL);
        }

        // 获取当前风险等级
        Integer currentLevel = student.getRiskLevel();
        if (currentLevel == null) {
            currentLevel = 1; // 默认为待评
        }

        // 如果等级没有变化，直接返回
        if (currentLevel.equals(targetLevel)) {
            log.info("学生 {} 的风险等级未发生变化，当前等级为 {}", studentProfileId, currentLevel);
            return;
        }

        // 更新学生档案中的风险等级
        studentProfileService.updateStudentRiskLevel(studentProfileId, targetLevel);

        // 记录风险等级变更历史
        InterventionLevelHistoryDO history = InterventionLevelHistoryDO.builder()
                .studentProfileId(studentProfileId)
                .oldLevel(currentLevel)
                .newLevel(targetLevel)
                .changeReason(adjustReqVO.getReason())
                .operatorUserId(SecurityFrameworkUtils.getLoginUserId())
                .build();
        levelHistoryMapper.insert(history);

        // 记录操作日志
        String levelChange = String.format("%s -> %s", 
                getRiskLevelName(currentLevel), 
                getRiskLevelName(targetLevel));
        log.info("用户 {} 调整学生 {} 的风险等级：{}，原因：{}", 
                SecurityFrameworkUtils.getLoginUserId(), 
                studentProfileId, 
                levelChange, 
                adjustReqVO.getReason());

        // 如果风险等级升高到严重或重大（4或5），自动创建关注记录
        if ((targetLevel == 4 || targetLevel == 5) && currentLevel < targetLevel) {
            createAutoInterventionRecord(studentProfileId, targetLevel, adjustReqVO.getReason());
        }

        // TODO: 发送通知给相关负责人（班主任、心理老师等）
        // notificationService.sendRiskLevelChangeNotification(studentProfileId, currentLevel, targetLevel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrisisEventCreateRespVO createCrisisEvent(CrisisEventCreateReqVO createReqVO) {
        // 验证学生是否存在
        StudentProfileVO student = studentProfileService.getStudentProfile(createReqVO.getStudentProfileId());
        if (student == null) {
            throw ServiceExceptionUtil.exception(STUDENT_PROFILE_NOT_EXISTS);
        }

        // 检查24小时内是否有重复上报
        LocalDateTime startTime = LocalDateTime.now().minusHours(24);
        if (crisisInterventionMapper.hasDuplicateEvent(createReqVO.getStudentProfileId(), startTime, LocalDateTime.now())) {
            log.warn("学生 {} 在24小时内已有危机事件上报", createReqVO.getStudentProfileId());
        }

        // 调试：打印当前登录用户ID
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        log.info("创建危机事件，当前登录用户ID: {}", currentUserId);

        // 创建危机事件
        CrisisInterventionDO event = BeanUtils.toBean(createReqVO, CrisisInterventionDO.class);

        // 生成事件编号：RPT_年份_随机数
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        String eventId = "RPT_" + currentYear + "_" + RandomUtil.randomNumbers(6);
        event.setEventId(eventId);

        event.setStatus(1); // 已上报
        event.setProcessStatus(0); // 处理状态设置为0
        event.setReporterUserId(currentUserId);
        event.setReportedAt(LocalDateTime.now());
        event.setProgress(0);
        event.setAutoAssigned(false);
        crisisInterventionMapper.insert(event);

        // 记录上报动作
        List<Long> attachments = createReqVO.getAttachments();
        String content = String.format("事件标题：%s，发生时间：%s，发生地点：%s，紧急程度：%s", createReqVO.getTitle(), createReqVO.getEventTime(), createReqVO.getLocation(), getPriorityLevelName(createReqVO.getPriority()));
        recordEventProcessWithUsers(event.getId(), "REPORT", createReqVO.getDescription(),
            content,
            null, null, CollUtil.isNotEmpty(attachments) ? attachments : null, null);

        // 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("eventId", event.getId());
        meta.put("riskLevel", createReqVO.getRiskLevel());
        meta.put("riskLevelName", getRiskLevelName(createReqVO.getRiskLevel()));
        meta.put("priorityLevel", createReqVO.getPriority());
        meta.put("priorityLevelName", getPriorityLevelName(createReqVO.getPriority()));
        meta.put("reporterUserId", currentUserId);
        meta.put("description", createReqVO.getDescription());
        meta.put("status", "已上报");
        studentTimelineService.saveTimelineWithMeta(
            createReqVO.getStudentProfileId(),
            TimelineEventTypeEnum.CRISIS_INTERVENTION.getType(),
            "危机事件(" + event.getEventId() + ")上报",
            event.getEventId(),
            content,
            meta
        );

        // 自动分配处理人（如果是自动模式）
        autoAssignHandler(event, student.getClassDeptId());

        // 返回创建结果
        return CrisisEventCreateRespVO.builder()
                .id(event.getId())
                .eventId(eventId)
                .title(event.getTitle())
                .build();
    }

    @Override
    public PageResult<CrisisEventRespVO> getCrisisEventPage(CrisisEventPageReqVO pageReqVO) {
        // 查询分页数据
        PageResult<CrisisInterventionDO> pageResult = crisisInterventionMapper.selectPage(pageReqVO);
        
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }

        // 转换为VO并填充关联信息
        List<CrisisEventRespVO> voList = convertToRespVOList(pageResult.getList());
        
        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public List<CrisisEventProcessStatisticsVO.StatisticsItem> getCrisisEventStatistics() {
        List<CrisisEventProcessStatisticsVO.StatisticsItem> result = new ArrayList<>();

        // 统计 process_status 0-6 的所有类型
        for (int i = 0; i <= 6; i++) {
            Long count = crisisInterventionMapper.countByProcessStatus(i);
            result.add(CrisisEventProcessStatisticsVO.StatisticsItem.builder()
                    .type(i)
                    .count(count != null ? count : 0L)
                    .build());
        }

        return result;
    }

    @Override
    public CrisisEventProcessStatisticsVO getCrisisEventStatusStatistics() {
        // 查询所有危机事件
        List<CrisisInterventionDO> allEvents = crisisInterventionMapper.selectList();

        // 初始化统计Map（所有类型初始值为0）
        Map<Integer, Long> processMethodMap = new HashMap<>();
        processMethodMap.put(1, 0L); // 心理访谈
        processMethodMap.put(2, 0L); // 量表评估
        processMethodMap.put(3, 0L); // 持续关注
        processMethodMap.put(4, 0L); // 直接解决

        Map<Integer, Long> followUpSuggestionMap = new HashMap<>();
        followUpSuggestionMap.put(1, 0L); // 继续访谈
        followUpSuggestionMap.put(2, 0L); // 继续评估
        followUpSuggestionMap.put(3, 0L); // 持续关注
        followUpSuggestionMap.put(4, 0L); // 问题解决
        followUpSuggestionMap.put(5, 0L); // 其他

        Map<Integer, Long> crisisEventStatusMap = new HashMap<>();
        crisisEventStatusMap.put(1, 0L); // 已上报
        crisisEventStatusMap.put(2, 0L); // 已分配

        // 遍历所有事件进行统计
        for (CrisisInterventionDO event : allEvents) {
            Integer status = event.getStatus();

            // 规则1：如果status是1或2，则crisis_event_status对应类型+1
            if (status != null && (status == 1 || status == 2)) {
                crisisEventStatusMap.put(status, crisisEventStatusMap.get(status) + 1);
            }
            // 规则2：如果status不是1或2
            else {
                // 查找最新的评估记录
                CrisisEventAssessmentDO latestAssessment = eventAssessmentMapper.selectLatestByEventId(event.getId());

                if (latestAssessment != null && latestAssessment.getFollowUpSuggestion() != null) {
                    // 如果找到评估记录，根据follow_up_suggestion统计
                    Integer followUpSuggestion = latestAssessment.getFollowUpSuggestion();
                    // 确保follow_up_suggestion值在1-5范围内
                    if (followUpSuggestion >= 1 && followUpSuggestion <= 5) {
                        followUpSuggestionMap.put(followUpSuggestion,
                            followUpSuggestionMap.getOrDefault(followUpSuggestion, 0L) + 1);
                    }
                } else if (event.getProcessMethod() != null) {
                    // 如果没有评估记录，根据process_method统计
                    Integer processMethod = event.getProcessMethod();
                    // 确保process_method值在1-4范围内
                    if (processMethod >= 1 && processMethod <= 4) {
                        processMethodMap.put(processMethod,
                            processMethodMap.getOrDefault(processMethod, 0L) + 1);
                    }
                }
            }
        }

        // 转换为返回VO
        List<CrisisEventProcessStatisticsVO.StatisticsItem> processMethodList = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            processMethodList.add(CrisisEventProcessStatisticsVO.StatisticsItem.builder()
                    .type(i)
                    .count(processMethodMap.get(i))
                    .build());
        }

        List<CrisisEventProcessStatisticsVO.StatisticsItem> followUpSuggestionList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            followUpSuggestionList.add(CrisisEventProcessStatisticsVO.StatisticsItem.builder()
                    .type(i)
                    .count(followUpSuggestionMap.get(i))
                    .build());
        }

        List<CrisisEventProcessStatisticsVO.StatisticsItem> crisisEventStatusList = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            crisisEventStatusList.add(CrisisEventProcessStatisticsVO.StatisticsItem.builder()
                    .type(i)
                    .count(crisisEventStatusMap.get(i))
                    .build());
        }

        return CrisisEventProcessStatisticsVO.builder()
                .interventionProcessMethod(processMethodList)
                .followUpSuggestion(followUpSuggestionList)
                .crisisEventStatus(crisisEventStatusList)
                .build();
    }

    @Override
    public CrisisEventRespVO getCrisisEvent(Long id) {
        CrisisInterventionDO event = crisisInterventionMapper.selectById(id);
        if (event == null) {
            throw ServiceExceptionUtil.exception(CRISIS_INTERVENTION_NOT_EXISTS);
        }

        List<CrisisEventRespVO> voList = convertToRespVOList(Collections.singletonList(event));
        CrisisEventRespVO vo = voList.get(0);

        // 加载处理历史
        List<CrisisEventProcessDO> processList = eventProcessMapper.selectListByEventId(id);
        vo.setProcessHistory(convertProcessHistory(processList, vo.getStudentProfileId()));

        // 加载所有评估记录（按创建时间倒序）
        List<CrisisEventAssessmentDO> assessmentList = eventAssessmentMapper.selectListByEventIdOrder(id);
        List<CrisisEventRespVO.AssessmentRecordVO> allAssessmentRecords = convertAssessmentRecords(assessmentList);

        // 从所有记录中筛选最新两条，并按创建时间正序排序
        List<CrisisEventRespVO.AssessmentRecordVO> latestTwoAssessments = allAssessmentRecords.stream()
            .limit(2)
            .sorted(Comparator.comparing(CrisisEventRespVO.AssessmentRecordVO::getCreateTime))
            .collect(Collectors.toList());
        vo.setLatestAssessments(latestTwoAssessments);

        // 添加正在进行的测评任务（未完成状态，包含未开始和进行中）
        if (vo.getStudentUserId() != null) {
            AssessmentUserTaskDO ongoingUserTask = assessmentUserTaskMapper.selectOngoingTaskByEventIdAndUserId(
                id, vo.getStudentUserId());
            
            if (ongoingUserTask != null) {
                AssessmentTaskDO task = assessmentTaskMapper.selectByTaskNo(ongoingUserTask.getTaskNo());
                if (task != null) {
                    CrisisEventRespVO.PendingAssessmentTaskVO taskVO = new CrisisEventRespVO.PendingAssessmentTaskVO();
                    taskVO.setTaskId(task.getId());
                    taskVO.setTaskNo(task.getTaskNo());
                    taskVO.setTaskName(task.getTaskName());
                    taskVO.setStatus(ongoingUserTask.getStatus());
                    vo.setPendingAssessmentTask(taskVO);
                }
            }
        }

        // 加载测评任务列表
        // if (vo.getStudentUserId() != null) {
        //     List<CrisisEventRespVO.AssessmentTaskVO> assessmentTasks =
        //         assessmentUserTaskMapper.selectAllTasksByEventIdAndUserId(id, vo.getStudentUserId());
        //     vo.setAssessmentTasks(assessmentTasks);
        // }

        // 设置所有评估记录
        vo.setAllAssessmentRecords(allAssessmentRecords);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignHandler(Long id, CrisisEventAssignReqVO assignReqVO) {
        // 验证事件存在
        CrisisInterventionDO event = validateEventExists(id);
        
        // 验证状态（只有已上报状态可以分配）
        if (event.getStatus() != 1) {
            throw ServiceExceptionUtil.exception(CRISIS_INTERVENTION_ALREADY_HANDLED);
        }

        // 更新处理人和状态
        event.setHandlerUserId(assignReqVO.getHandlerUserId());
        event.setStatus(2); // 已分配
        event.setProgress(50);
        event.setHandleAt(LocalDateTime.now());
        crisisInterventionMapper.updateById(event);

        // 获取负责人信息用于生成分配原因
        AdminUserRespDTO handler = adminUserApi.getUser(assignReqVO.getHandlerUserId());
        String handlerName = handler != null ? handler.getNickname() : "未知";
        
        // 使用结构化方式记录分配动作（原因后端自动生成）
        String autoAssignReason = String.format("分配负责人——>%s", handlerName);
        recordEventProcessWithUsers(id, "ASSIGN_HANDLER", autoAssignReason,
            null,
            assignReqVO.getHandlerUserId(),
            null, // 初次分配，没有原负责人
            null, // 无附件
            null); // 无评估ID
        
        // 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("eventId", id);
        meta.put("handlerUserId", assignReqVO.getHandlerUserId());
        meta.put("handlerName", handlerName);
        meta.put("assignReason", null);
        meta.put("status", "已分配");
        
        String content = String.format("危机事件已分配给 %s 处理", handlerName);
        studentTimelineService.saveTimelineWithMeta(
            event.getStudentProfileId(),
            TimelineEventTypeEnum.CRISIS_INTERVENTION.getType(),
            "危机事件(" + event.getEventId() + ")分配",
            event.getEventId(),
            content,
            meta
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reassignHandler(Long id, CrisisEventReassignReqVO reassignReqVO) {
        // 验证事件存在
        CrisisInterventionDO event = validateEventExists(id);

        // 记录原处理人
        Long oldHandlerId = event.getHandlerUserId();
        
        // 更新处理人
        event.setHandlerUserId(reassignReqVO.getNewHandlerUserId());
        event.setHandleAt(LocalDateTime.now());
        crisisInterventionMapper.updateById(event);

        // 获取新旧负责人信息用于生成重新分配原因
        AdminUserRespDTO oldHandler = oldHandlerId != null ? adminUserApi.getUser(oldHandlerId) : null;
        AdminUserRespDTO newHandler = adminUserApi.getUser(reassignReqVO.getNewHandlerUserId());
        String oldHandlerName = oldHandler != null ? oldHandler.getNickname() : "未知";
        String newHandlerName = newHandler != null ? newHandler.getNickname() : "未知";
        
        // 使用结构化方式记录变更
        String reassignReason = String.format("重新分配负责人：%s——>%s", 
            oldHandlerName, newHandlerName);
        recordEventProcessWithUsers(id, "REASSIGN_HANDLER", reassignReason, 
            reassignReqVO.getReason(), 
            reassignReqVO.getNewHandlerUserId(), 
            oldHandlerId,
            null, // 无附件
            null); // 无评估ID
        
        // 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("eventId", id);
        meta.put("oldHandlerUserId", oldHandlerId);
        meta.put("oldHandlerName", oldHandlerName);
        meta.put("newHandlerUserId", reassignReqVO.getNewHandlerUserId());
        meta.put("newHandlerName", newHandlerName);
        meta.put("reason", reassignReqVO.getReason());
        
        String content = String.format("危机事件负责人从 %s 变更为 %s，原因：%s",
            oldHandler != null ? oldHandler.getNickname() : "未知",
            newHandler != null ? newHandler.getNickname() : "未知",
            reassignReqVO.getReason());
        studentTimelineService.saveTimelineWithMeta(
            event.getStudentProfileId(),
            TimelineEventTypeEnum.CRISIS_INTERVENTION.getType(),
            "危机事件(" + event.getEventId() + ")负责人变更",
            event.getEventId(),
            content,
            meta
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processCrisisEvent(Long id, CrisisEventProcessReqVO processReqVO) {
        // 验证事件存在
        CrisisInterventionDO event = validateEventExists(id);

        Integer processMethod = processReqVO.getProcessMethod();

        // 更新处理方式和状态
        event.setProcessMethod(processMethod);
        event.setProcessReason(processReqVO.getProcessReason());
        if(processMethod == 3 || processMethod == 4) {
            event.setStatus(3); // 状态为3
        }
        event.setProcessStatus(processMethod); // 更新处理状态为选择方式
        event.setProgress(50); // 进度50%
        crisisInterventionMapper.updateById(event);

        // 记录处理动作
        // 从字典获取处理方式名称
        String methodName = getProcessMethodName(processMethod);
        recordEventProcessWithUsers(id, "CHOOSE_PROCESS", methodName,
            processReqVO.getProcessReason(),
            null, null, null, null); // 无附件，无评估ID
        
        // 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("eventId", id);
        meta.put("processMethod", processMethod);
        meta.put("processMethodName", methodName);
        meta.put("processReason", processReqVO.getProcessReason());
        meta.put("status", "处理中");
        meta.put("progress", 50);
        
        String content = String.format("开始处理危机事件，处理方式：%s", methodName);
        studentTimelineService.saveTimelineWithMeta(
            event.getStudentProfileId(),
            TimelineEventTypeEnum.CRISIS_INTERVENTION.getType(),
            "危机事件(" + event.getEventId() + ")处理",
            event.getEventId(),
            content,
            meta
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeCrisisEvent(Long id, CrisisEventCloseReqVO closeReqVO) {
        // 验证事件存在
        CrisisInterventionDO event = validateEventExists(id);

        // 更新事件状态
        event.setStatus(5); // 已结案
        event.setClosureSummary(closeReqVO.getSummary());
        event.setProgress(100);
        event.setProcessStatus(6); // 更新处理状态为最终评估
        crisisInterventionMapper.updateById(event);

        // 保存最终评估
        CrisisEventAssessmentDO assessment = new CrisisEventAssessmentDO();
        assessment.setEventId(id);
        assessment.setAssessorUserId(SecurityFrameworkUtils.getLoginUserId());
        assessment.setAssessmentType(2); // 最终评估
        assessment.setRiskLevel(closeReqVO.getRiskLevel());
        assessment.setProblemTypes(closeReqVO.getProblemTypes());
        assessment.setFollowUpSuggestion(closeReqVO.getFollowUpSuggestion());
        assessment.setContent(closeReqVO.getSummary());
        // 设置新字段
        assessment.setHasMedicalVisit(closeReqVO.getHasMedicalVisit());
        assessment.setMedicalVisitRecord(closeReqVO.getMedicalVisitRecord());
        assessment.setObservationRecord(closeReqVO.getObservationRecord());
        assessment.setAttachmentIds(closeReqVO.getAttachmentIds());
        eventAssessmentMapper.insert(assessment);
        
        // 获取评估记录ID
        Long assessmentId = assessment.getId();

        // 更新学生档案的风险等级和问题标签
        if (closeReqVO.getRiskLevel() != null) {
            studentProfileService.updateStudentRiskLevel(event.getStudentProfileId(), closeReqVO.getRiskLevel());
        }
        
        // 更新学生档案的问题类型到特殊标记字段
        if (closeReqVO.getProblemTypes() != null && !closeReqVO.getProblemTypes().isEmpty()) {
            String specialMarks = String.join(",", closeReqVO.getProblemTypes());
            studentProfileService.updateStudentSpecialMarks(event.getStudentProfileId(), specialMarks);
        }
        
        // 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("eventId", id);
        meta.put("finalRiskLevel", closeReqVO.getRiskLevel());
        meta.put("finalRiskLevelName", getRiskLevelName(closeReqVO.getRiskLevel()));
        meta.put("problemTypes", closeReqVO.getProblemTypes());
        meta.put("followUpSuggestion", closeReqVO.getFollowUpSuggestion());
        meta.put("summary", closeReqVO.getSummary());
        meta.put("status", "已结案");
        meta.put("progress", 100);
        
        String content = String.format("危机事件已结案，最终风险等级：%s", 
            getRiskLevelName(closeReqVO.getRiskLevel()));
        studentTimelineService.saveTimelineWithMeta(
            event.getStudentProfileId(),
            TimelineEventTypeEnum.CRISIS_INTERVENTION.getType(),
            "危机事件(" + event.getEventId() + ")结案",
            event.getEventId(),
            content,
            meta
        );

        // 构建结案评估信息
        String riskLevelInfo = "风险等级：" + getRiskLevelName(closeReqVO.getRiskLevel());

        // 处理问题类型数组
        String problemTypesInfo = "问题类型：";
        if (CollUtil.isNotEmpty(closeReqVO.getProblemTypes())) {
            problemTypesInfo += String.join("、", closeReqVO.getProblemTypes());
        } else {
            problemTypesInfo += "无";
        }

        // 获取后续建议
        String followUpInfo = "后续建议：" + getFollowUpSuggestionName(closeReqVO.getFollowUpSuggestion());

        // 组合完整的评估信息
        String closeAssessmentInfo = riskLevelInfo + "，" + problemTypesInfo + "，" + followUpInfo;

        // 使用结构化方式记录结案动作
        recordEventProcessWithUsers(id, "CLOSE", closeReqVO.getSummary(),
            closeAssessmentInfo,
            null, null, closeReqVO.getAttachmentIds(), assessmentId); // 传递 attachmentIds 和评估ID
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitStageAssessment(Long id, CrisisEventAssessmentReqVO assessmentReqVO) {
        // 验证事件存在
        CrisisInterventionDO event = validateEventExists(id);

        // 保存阶段性评估
        CrisisEventAssessmentDO assessment = BeanUtils.toBean(assessmentReqVO, CrisisEventAssessmentDO.class);
        assessment.setEventId(id);
        assessment.setAssessorUserId(SecurityFrameworkUtils.getLoginUserId());
        assessment.setAssessmentType(1); // 阶段性评估
        // 设置新字段
        assessment.setHasMedicalVisit(assessmentReqVO.getHasMedicalVisit());
        assessment.setMedicalVisitRecord(assessmentReqVO.getMedicalVisitRecord());
        assessment.setObservationRecord(assessmentReqVO.getObservationRecord());
        assessment.setAttachmentIds(assessmentReqVO.getAttachmentIds());
        eventAssessmentMapper.insert(assessment);
        
        // 获取评估记录ID
        Long assessmentId = assessment.getId();

        // 更新进度和状态
        event.setStatus(3); // 状态为3
        // event.setProgress(88);
        event.setProcessStatus(assessmentReqVO.getFollowUpSuggestion()); // 更新处理状态为阶段性评估
        crisisInterventionMapper.updateById(event);

        // 构建评估信息
        String riskLevelInfo = "风险等级：" + getRiskLevelName(assessmentReqVO.getRiskLevel());

        // 处理问题类型数组
        String problemTypesInfo = "问题类型：";
        if (CollUtil.isNotEmpty(assessmentReqVO.getProblemTypes())) {
            problemTypesInfo += String.join("、", assessmentReqVO.getProblemTypes());
        } else {
            problemTypesInfo += "无";
        }

        // 获取后续建议
        String followUpInfo = "后续建议：" + getFollowUpSuggestionName(assessmentReqVO.getFollowUpSuggestion());

        // 组合完整的评估信息
        String assessmentInfo = riskLevelInfo + "，" + problemTypesInfo + "，" + followUpInfo;

        // 记录评估动作
        recordEventProcessWithUsers(id, "STAGE_ASSESSMENT", assessmentReqVO.getContent(),
            assessmentInfo,
            null, null, assessmentReqVO.getAttachmentIds(), assessmentId); // 传递 attachmentIds 和评估ID

        // 根据后续建议决定下一步
        if (assessmentReqVO.getFollowUpSuggestion() == 5) { // 转介
            event.setStatus(4); // 可以考虑结案
            crisisInterventionMapper.updateById(event);
        }
    }

    @Override
    public PageResult<CrisisEventProcessHistoryVO> getProcessHistory(Long id, Integer pageNo, Integer pageSize) {
        List<CrisisEventProcessDO> processList = eventProcessMapper.selectListByEventId(id);
        List<CrisisEventProcessHistoryVO> voList = convertProcessHistoryVO(processList);
        
        return new PageResult<>(voList, (long) voList.size());
    }

    @Override
    public Boolean checkDuplicateEvent(Long studentProfileId) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(24);
        return crisisInterventionMapper.hasDuplicateEvent(studentProfileId, startTime, LocalDateTime.now());
    }

    @Override
    public InterventionAssignmentSettingVO getAssignmentSettings() {
        InterventionAssignmentSettingVO settingVO = new InterventionAssignmentSettingVO();

        // 从数据库配置中读取分配模式，如果不存在则初始化
        var modeConfig = configService.getConfigByKey(CONFIG_KEY_ASSIGNMENT_MODE);
        if (modeConfig == null) {
            // 配置不存在，初始化配置，默认值为 auto-psychology
            var createModeReqVO = new cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigSaveReqVO();
            createModeReqVO.setCategory("intervention");
            createModeReqVO.setName("干预系统分配模式");
            createModeReqVO.setKey(CONFIG_KEY_ASSIGNMENT_MODE);
            createModeReqVO.setValue(InterventionAssignmentModeEnum.AUTO_PSYCHOLOGY.getMode());
            createModeReqVO.setVisible(true);
            createModeReqVO.setRemark("危机事件分配模式：manual-手动分配, auto-psychology-自动分配给心理老师, auto-head-teacher-自动分配给班主任");
            configService.createConfig(createModeReqVO);
            settingVO.setMode(InterventionAssignmentModeEnum.AUTO_PSYCHOLOGY.getMode());
            log.info("初始化配置: key={}, value={}", CONFIG_KEY_ASSIGNMENT_MODE, InterventionAssignmentModeEnum.AUTO_PSYCHOLOGY.getMode());
        } else {
            settingVO.setMode(modeConfig.getValue());
        }

        // 从角色中获取默认心理老师ID
        Long defaultPsychologyId = null;
        RoleDO role = roleMapper.selectByCode(RoleCodeEnum.DEFAULT_PSYCHOLOGY_TEACHER.getCode());
        if (role != null) {
            List<UserRoleDO> userRoles = userRoleMapper.selectListByRoleId(role.getId());
            if (CollUtil.isNotEmpty(userRoles)) {
                // 理论上只有一个用户拥有该角色，取第一个
                defaultPsychologyId = userRoles.get(0).getUserId();
            }
        }
        settingVO.setDefaultPsychologyId(defaultPsychologyId);

        return settingVO;
    }

    @Override
    @Transactional
    public void setAssignmentSettings(InterventionAssignmentSettingVO settingVO) {
        // 验证模式是否有效
        if (!InterventionAssignmentModeEnum.isValid(settingVO.getMode())) {
            throw ServiceExceptionUtil.exception(CRISIS_EVENT_INVALID_ASSIGNMENT_MODE);
        }

        // 更新分配模式配置
        var modeConfig = configService.getConfigByKey(CONFIG_KEY_ASSIGNMENT_MODE);
        if (modeConfig != null) {
            var updateModeReqVO = new cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigSaveReqVO();
            updateModeReqVO.setId(modeConfig.getId());
            updateModeReqVO.setCategory(modeConfig.getCategory());
            updateModeReqVO.setName(modeConfig.getName());
            updateModeReqVO.setKey(modeConfig.getConfigKey());
            updateModeReqVO.setValue(settingVO.getMode());
            updateModeReqVO.setVisible(modeConfig.getVisible());
            updateModeReqVO.setRemark(modeConfig.getRemark());
            configService.updateConfig(updateModeReqVO);
        }

        // 处理默认心理老师角色
        if (settingVO.getDefaultPsychologyId() != null && settingVO.getDefaultPsychologyId() > 0) {
            RoleDO role = roleMapper.selectByCode(RoleCodeEnum.DEFAULT_PSYCHOLOGY_TEACHER.getCode());
            if (role != null) {
                // 检查传入的用户是否已经拥有默认心理老师角色
                UserRoleDO existingUserRole = userRoleMapper.selectByUserIdAndRoleId(
                    settingVO.getDefaultPsychologyId(), role.getId());
                
                // 如果传入的用户已经拥有该角色，则不处理
                if (existingUserRole != null) {
                    log.info("默认心理老师角色已经分配给用户：{}，无需重复设置", settingVO.getDefaultPsychologyId());
                } else {
                    // 1. 删除所有用户的默认心理老师角色记录
                    userRoleMapper.deleteListByRoleId(role.getId());
                    
                    // 2. 为新用户添加默认心理老师角色记录
                    UserRoleDO userRole = new UserRoleDO();
                    userRole.setUserId(settingVO.getDefaultPsychologyId());
                    userRole.setRoleId(role.getId());
                    userRoleMapper.insert(userRole);
                    
                    log.info("默认心理老师角色已设置给用户：{}", settingVO.getDefaultPsychologyId());
                }
            } else {
                log.warn("未找到默认心理老师角色，角色代码：{}", RoleCodeEnum.DEFAULT_PSYCHOLOGY_TEACHER.getCode());
            }
        }

        log.info("危机事件分配模式已设置为：{}，默认心理老师ID：{}", settingVO.getMode(), settingVO.getDefaultPsychologyId());
    }

    private CrisisInterventionDO validateEventExists(Long id) {
        CrisisInterventionDO event = crisisInterventionMapper.selectById(id);
        if (event == null) {
            throw ServiceExceptionUtil.exception(CRISIS_INTERVENTION_NOT_EXISTS);
        }
        return event;
    }

    private void recordEventProcess(Long eventId, String action, String content) {
        CrisisEventProcessDO process = new CrisisEventProcessDO();
        process.setEventId(eventId);
        process.setOperatorUserId(SecurityFrameworkUtils.getLoginUserId());
        process.setAction(action);
        process.setContent(content);
        eventProcessMapper.insert(process);
    }

    private void recordEventProcessWithUsers(Long eventId, String action, String content,
                                            String reason, Long relatedUserId, Long originalUserId,
                                            List<Long> attachmentIds, Long assessmentId) {
        CrisisEventProcessDO process = new CrisisEventProcessDO();
        process.setEventId(eventId);
        process.setOperatorUserId(SecurityFrameworkUtils.getLoginUserId());
        process.setAction(action);
        process.setContent(StrUtil.blankToDefault(content, action));
        process.setReason(reason);
        process.setRelatedUserId(relatedUserId);
        process.setOriginalUserId(originalUserId);
        process.setAssessmentId(assessmentId);
        // 只有附件列表非空时才设置
        if (CollUtil.isNotEmpty(attachmentIds)) {
            process.setAttachmentIds(attachmentIds);
        }
        eventProcessMapper.insert(process);
    }

    private List<CrisisEventRespVO> convertToRespVOList(List<CrisisInterventionDO> events) {
        if (CollUtil.isEmpty(events)) {
            return new ArrayList<>();
        }

        // 批量获取学生信息
        List<Long> studentIds = events.stream()
            .map(CrisisInterventionDO::getStudentProfileId)
            .distinct()
            .collect(Collectors.toList());
        
        // 逐个获取学生信息（因为现有接口不支持批量查询）
        Map<Long, StudentProfileVO> studentMap = new HashMap<>();
        for (Long studentId : studentIds) {
            StudentProfileVO student = studentProfileService.getStudentProfile(studentId);
            if (student != null) {
                studentMap.put(studentId, student);
            }
        }

        // 批量获取用户信息
        Set<Long> userIds = new HashSet<>();
        events.forEach(e -> {
            if (e.getHandlerUserId() != null) userIds.add(e.getHandlerUserId());
            if (e.getReporterUserId() != null) userIds.add(e.getReporterUserId());
        });
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(new ArrayList<>(userIds));

        // 转换为VO
        return events.stream().map(event -> {
            CrisisEventRespVO vo = BeanUtils.toBean(event, CrisisEventRespVO.class);

            // 填充学生信息
            StudentProfileVO student = studentMap.get(event.getStudentProfileId());
            if (student != null) {
                vo.setStudentName(student.getName());
                vo.setStudentNumber(student.getStudentNo());
                vo.setClassName(student.getClassName());
                vo.setStudentUserId(student.getUserId());
            }

            // 填充处理人信息
            if (event.getHandlerUserId() != null) {
                AdminUserRespDTO handler = userMap.get(event.getHandlerUserId());
                if (handler != null) {
                    vo.setHandlerName(handler.getNickname());
                }
            }

            // 填充上报人信息
            if (event.getReporterUserId() != null) {
                AdminUserRespDTO reporter = userMap.get(event.getReporterUserId());
                if (reporter != null) {
                    vo.setReporterName(reporter.getNickname());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    private List<CrisisEventRespVO.ProcessHistoryVO> convertProcessHistory(List<CrisisEventProcessDO> processList, Long studentProfileId) {
        if (CollUtil.isEmpty(processList)) {
            return new ArrayList<>();
        }

        // 批量获取操作人信息
        List<Long> userIds = processList.stream()
            .map(CrisisEventProcessDO::getOperatorUserId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);

        // 批量查询测评结果
        Map<String, Long> taskResultMap = new HashMap<>();
        if (studentProfileId != null) {
            for (CrisisEventProcessDO process : processList) {
                if (StrUtil.isNotBlank(process.getTaskNo())) {
                    AssessmentResultDO result = assessmentResultMapper.selectByTaskNoAndParticipantId(
                        process.getTaskNo(), studentProfileId);
                    if (result != null) {
                        taskResultMap.put(process.getTaskNo(), result.getId());
                    }
                }
            }
        }

        return processList.stream().map(process -> {
            CrisisEventRespVO.ProcessHistoryVO vo = new CrisisEventRespVO.ProcessHistoryVO();
            vo.setId(process.getId());
            vo.setEventId(process.getEventId());
            vo.setOperateTime(process.getCreateTime());
            vo.setAction(process.getAction());
            vo.setContent(process.getContent());
            vo.setReason(process.getReason());
            vo.setAttachmentIds(process.getAttachmentIds());
            vo.setAssessmentId(process.getAssessmentId());

            AdminUserRespDTO operator = userMap.get(process.getOperatorUserId());
            if (operator != null) {
                vo.setOperatorName(operator.getNickname());
            }

            // 设置测评结果ID
            if (StrUtil.isNotBlank(process.getTaskNo())) {
                Long taskResultId = taskResultMap.get(process.getTaskNo());
                if (taskResultId != null) {
                    vo.setTaskResultId(taskResultId);
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    private List<CrisisEventProcessHistoryVO> convertProcessHistoryVO(List<CrisisEventProcessDO> processList) {
        if (CollUtil.isEmpty(processList)) {
            return new ArrayList<>();
        }

        // 收集所有需要查询的用户ID
        Set<Long> allUserIds = new HashSet<>();
        for (CrisisEventProcessDO process : processList) {
            if (process.getOperatorUserId() != null) {
                allUserIds.add(process.getOperatorUserId());
            }
            if (process.getRelatedUserId() != null) {
                allUserIds.add(process.getRelatedUserId());
            }
            if (process.getOriginalUserId() != null) {
                allUserIds.add(process.getOriginalUserId());
            }
        }
        
        // 批量获取用户信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(new ArrayList<>(allUserIds));

        return processList.stream().map(process -> {
            CrisisEventProcessHistoryVO vo = new CrisisEventProcessHistoryVO();
            vo.setId(process.getId());
            vo.setEventId(process.getEventId());
            vo.setOperatorUserId(process.getOperatorUserId());
            vo.setAction(process.getAction());
            vo.setContent(process.getContent());
            vo.setReason(process.getReason());
            vo.setRelatedUserId(process.getRelatedUserId());
            vo.setOriginalUserId(process.getOriginalUserId());
            vo.setAttachmentIds(process.getAttachmentIds());
            vo.setCreateTime(process.getCreateTime());

            // 设置操作人姓名
            AdminUserRespDTO operator = userMap.get(process.getOperatorUserId());
            if (operator != null) {
                vo.setOperatorName(operator.getNickname());
            }

            // 设置涉及用户姓名
            if (process.getRelatedUserId() != null) {
                AdminUserRespDTO relatedUser = userMap.get(process.getRelatedUserId());
                if (relatedUser != null) {
                    vo.setRelatedUserName(relatedUser.getNickname());
                }
            }

            // 设置原用户姓名
            if (process.getOriginalUserId() != null) {
                AdminUserRespDTO originalUser = userMap.get(process.getOriginalUserId());
                if (originalUser != null) {
                    vo.setOriginalUserName(originalUser.getNickname());
                }
            }

            // 设置操作类型名称
            vo.setActionName(getActionName(process.getAction()));

            return vo;
        }).collect(Collectors.toList());
    }



    /**
     * 自动创建干预记录
     * 当风险等级升高到重大或严重时自动创建
     */
    private void createAutoInterventionRecord(Long studentProfileId, Integer riskLevel, String reason) {
        try {
            // 检查是否已有未结案的危机事件
            List<CrisisInterventionDO> activeEvents = crisisInterventionMapper.selectList(
                    new LambdaQueryWrapperX<CrisisInterventionDO>()
                            .eq(CrisisInterventionDO::getStudentProfileId, studentProfileId)
                            .in(CrisisInterventionDO::getStatus, Arrays.asList(1, 2, 3, 5)) // 未结案状态
                            .orderByDesc(CrisisInterventionDO::getCreateTime)
            );

            if (CollUtil.isNotEmpty(activeEvents)) {
                log.info("学生 {} 已有未结案的危机事件，跳过自动创建", studentProfileId);
                return;
            }

            // 创建新的危机事件记录
            CrisisInterventionDO event = new CrisisInterventionDO();
            
            // 生成事件编号：RPT_年份_随机数
            String currentYear = String.valueOf(LocalDateTime.now().getYear());
            String eventId = "RPT_" + currentYear + "_" + RandomUtil.randomNumbers(6);
            event.setEventId(eventId);
            
            event.setStudentProfileId(studentProfileId);
            event.setTitle("风险等级调整");
            event.setDescription("风险等级调整至" + getRiskLevelName(riskLevel) + "：" + reason);
            event.setRiskLevel(riskLevel);
            event.setPriority(riskLevel == 5 ? 1 : 2); // 重大设为高优先级，严重设为中优先级
            event.setStatus(1); // 已上报
            event.setReporterUserId(SecurityFrameworkUtils.getLoginUserId());
            event.setReportedAt(LocalDateTime.now());
            event.setProgress(0);
            event.setProcessStatus(0); // 处理状态设置为0
            event.setAutoAssigned(true);  // 自动分配
            event.setSourceType(2); // 来源类型：AI预警

            crisisInterventionMapper.insert(event);

            // 记录事件处理历史
            recordEventProcess(event.getId(), "系统自动创建", 
                    "因风险等级调整至" + getRiskLevelName(riskLevel) + "自动创建危机事件");

            log.info("为学生 {} 自动创建危机事件，ID: {}", studentProfileId, event.getId());
        } catch (Exception e) {
            log.error("自动创建干预记录失败，学生ID: {}", studentProfileId, e);
        }
    }

    @Override
    public void updateCrisisEventDescription(Long id, String description) {
        // 校验事件是否存在
        CrisisInterventionDO event = crisisInterventionMapper.selectById(id);
        if (event == null) {
            throw ServiceExceptionUtil.exception(CRISIS_INTERVENTION_NOT_EXISTS);
        }

        // 更新描述
        CrisisInterventionDO updateObj = new CrisisInterventionDO();
        updateObj.setId(id);
        updateObj.setDescription(description);
        crisisInterventionMapper.updateById(updateObj);

        // 记录处理历史
        recordEventProcess(id, "UPDATE_DESCRIPTION", "更新事件描述：" + description);

        log.info("更新危机事件描述，ID: {}, 描述: {}", id, description);
    }

    @Override
    public void updateProcessRecord(Long id, String content) {
        // 查询处理记录
        CrisisEventProcessDO processRecord = eventProcessMapper.selectById(id);
        if (processRecord == null) {
            throw ServiceExceptionUtil.exception(CRISIS_INTERVENTION_NOT_EXISTS);
        }

        // 根据action类型决定更新哪个字段
        CrisisEventProcessDO updateObj = new CrisisEventProcessDO();
        updateObj.setId(id);

        if ("REASSIGN_HANDLER".equals(processRecord.getAction()) ||
            "CHOOSE_PROCESS".equals(processRecord.getAction())) {
            // 如果是更改负责人或选择处理方式的操作，更新reason字段
            updateObj.setReason(content);
            log.info("更新危机事件处理记录reason，ID: {}, 内容: {}", id, content);
        } else {
            // 其他情况更新content字段
            updateObj.setContent(content);
            log.info("更新危机事件处理记录content，ID: {}, 内容: {}", id, content);
        }

        eventProcessMapper.updateById(updateObj);
    }

    /**
     * 转换评估记录为VO
     */
    private List<CrisisEventRespVO.AssessmentRecordVO> convertAssessmentRecords(List<CrisisEventAssessmentDO> assessments) {
        if (CollUtil.isEmpty(assessments)) {
            return new ArrayList<>();
        }

        // 收集所有评估人ID
        List<Long> assessorIds = assessments.stream()
                .map(CrisisEventAssessmentDO::getAssessorUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取评估人信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(assessorIds);

        return assessments.stream().map(assessment -> {
            CrisisEventRespVO.AssessmentRecordVO vo = new CrisisEventRespVO.AssessmentRecordVO();
            vo.setId(assessment.getId());
            vo.setAssessorUserId(assessment.getAssessorUserId());
            vo.setAssessmentType(assessment.getAssessmentType());
            vo.setRiskLevel(assessment.getRiskLevel());
            vo.setRiskLevelName(getRiskLevelName(assessment.getRiskLevel()));
            vo.setProblemTypes(assessment.getProblemTypes());
            vo.setFollowUpSuggestion(assessment.getFollowUpSuggestion());
            vo.setFollowUpSuggestionName(getFollowUpSuggestionName(assessment.getFollowUpSuggestion()));
            vo.setContent(assessment.getContent());
            vo.setCreateTime(assessment.getCreateTime());

            // 设置附件ID列表
            vo.setAttachments(assessment.getAttachmentIds());

            // 设置评估人姓名
            AdminUserRespDTO assessor = userMap.get(assessment.getAssessorUserId());
            if (assessor != null) {
                vo.setAssessorName(assessor.getNickname());
            }

            return vo;
        }).collect(Collectors.toList());
    }


    // ============================= 获取字典标签 =============================
    /**
     * 获取评估后续建议名称
     */
    private String getFollowUpSuggestionName(Integer followUpSuggestion) {
        if (followUpSuggestion == null) {
            return "未知";
        }

        String label = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.FOLLOW_UP_SUGGESTION, followUpSuggestion);
        return label != null ? label : "未知";
    }

    /**
     * 获取危机事件处理方式名称
     */
    private String getProcessMethodName(Integer method) {
        if (method == null) {
            return "未知";
        }

        String label = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.INTERVENTION_PROCESS_METHOD, method);
        return label != null ? label : "未知";
    }

    /**
     * 获取事件历史记录处理动作名称
     */
    private String getActionName(String action) {
        if (StrUtil.isBlank(action)) {
            return "未知";
        }
        
        String label = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.CRISIS_EVENT_ACTION, action);
        return label != null ? label : "未知";
    }

    /**
     * 获取测评风险等级名称
     */
    private String getRiskLevelName(Integer level) {
        if(level == null){
            return "未知";
        }

        String label = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.RISK_LEVEL, level);
        return label != null ? label : "未知";
    }

    /**
     * 获取来源类型名称
     */
    private String getSourceTypeName(Integer sourceType) {
        if (sourceType == null) {
            return "未知";
        }
        String label = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.CRISIS_SOURCE_TYPE, sourceType);
        return label != null ? label : "未知";
    }

    /**
     * 获取上报紧急程度名称
     */
    private String getPriorityLevelName(Integer urgencyLevel) {
        if (urgencyLevel == null) {
            return "未知";
        }
        String label = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.CRISIS_EVENT_REPORT_SOURCE, urgencyLevel);
        return label != null ? label : "未知";
    }

    /**
     * 构建从当前部门到根部门的完整路径
     * 
     * @param deptId 起始部门ID
     * @return 部门路径列表（从子到父的顺序）
     */
    private List<Long> buildDeptPath(Long deptId) {
        List<Long> path = new ArrayList<>();
        if (deptId == null) {
            return path;
        }

        Long currentDeptId = deptId;
        // 防止死循环，最多查询100层
        int maxDepth = 100;
        int depth = 0;

        while (currentDeptId != null && !currentDeptId.equals(0L) && depth < maxDepth) {
            path.add(currentDeptId);
            
            // 查询当前部门信息
            DeptDO dept = deptMapper.selectById(currentDeptId);
            if (dept == null) {
                break;
            }
            
            currentDeptId = dept.getParentId();
            depth++;
        }

        return path;
    }

    /**
     * 自动分配处理人
     * 
     * @param event 危机事件
     * @param studentClassDeptId 学生班级部门ID
     */
    private void autoAssignHandler(CrisisInterventionDO event, Long studentClassDeptId) {
        try {
            // 1. 读取分配模式配置
            ConfigDO config = configService.getConfigByKey(CONFIG_KEY_ASSIGNMENT_MODE);
            if (config == null || StrUtil.isBlank(config.getValue())) {
                log.info("未配置自动分配模式，跳过自动分配");
                return;
            }

            String assignmentMode = config.getValue().trim();
            log.info("危机事件 {} 自动分配模式: {}", event.getEventId(), assignmentMode);

            // 2. 根据模式确定要查询的角色code
            String targetRoleCode;
            String defaultRoleCode = null;
            
            if ("auto-psychology".equals(assignmentMode)) {
                targetRoleCode = "psychology_teacher";
                defaultRoleCode = "default_psychology_teacher";
            } else if ("auto-head-teacher".equals(assignmentMode)) {
                targetRoleCode = "head_teacher";
            } else {
                log.info("手动分配模式，不进行自动分配");
                return;
            }

            // 3. 查询目标角色ID
            RoleDO targetRole = roleMapper.selectByCode(targetRoleCode);
            if (targetRole == null) {
                log.warn("未找到角色 {}，无法自动分配", targetRoleCode);
                return;
            }
            Long targetRoleId = targetRole.getId();

            // 4. 构建部门路径链（从班级到根部门）
            List<Long> deptPath = buildDeptPath(studentClassDeptId);
            if (CollUtil.isEmpty(deptPath)) {
                log.warn("无法构建部门路径，学生班级部门ID: {}", studentClassDeptId);
                return;
            }

            log.info("部门路径: {}", deptPath);

            // 5. 批量查询这些部门下的用户
            List<UserDeptDO> userDepts = userDeptMapper.selectListByDeptIds(deptPath);
            
            if (CollUtil.isEmpty(userDepts)) {
                log.info("部门路径上没有找到用户");
            } else {
                // 6. 按部门分组
                Map<Long, List<Long>> deptUserMap = userDepts.stream()
                    .collect(Collectors.groupingBy(
                        UserDeptDO::getDeptId,
                        Collectors.mapping(UserDeptDO::getUserId, Collectors.toList())
                    ));

                // 7. 批量查询用户信息（过滤启用状态）
                Set<Long> allUserIds = userDepts.stream()
                    .map(UserDeptDO::getUserId)
                    .collect(Collectors.toSet());
                
                Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(allUserIds);
                if (userMap != null) {
                    userMap = userMap.entrySet().stream()
                        .filter(e -> e.getValue() != null && 
                            CommonStatusEnum.ENABLE.getStatus().equals(e.getValue().getStatus()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }

                // 8. 查询用户角色
                if (CollUtil.isNotEmpty(userMap)) {
                    List<UserRoleDO> userRoles = userRoleMapper.selectList(
                        new LambdaQueryWrapperX<UserRoleDO>()
                            .in(UserRoleDO::getUserId, userMap.keySet())
                    );
                    
                    Map<Long, List<Long>> userRoleMap = userRoles.stream()
                        .collect(Collectors.groupingBy(
                            UserRoleDO::getUserId,
                            Collectors.mapping(UserRoleDO::getRoleId, Collectors.toList())
                        ));

                    // 9. 按部门顺序查找第一个符合条件的用户
                    for (Long deptId : deptPath) {
                        List<Long> userIds = deptUserMap.get(deptId);
                        if (CollUtil.isEmpty(userIds)) {
                            continue;
                        }
                        
                        for (Long userId : userIds) {
                            // 检查用户是否启用
                            if (!userMap.containsKey(userId)) {
                                continue;
                            }
                            
                            // 检查用户是否拥有目标角色
                            List<Long> roleIds = userRoleMap.get(userId);
                            if (CollUtil.isNotEmpty(roleIds) && roleIds.contains(targetRoleId)) {
                                // 找到第一个符合条件的用户
                                assignHandlerToEvent(event, userId);
                                log.info("危机事件 {} 自动分配给用户 {} (部门ID: {})", 
                                    event.getEventId(), userId, deptId);
                                return;
                            }
                        }
                    }
                }
            }

            // 10. 如果未找到且是心理老师模式，查询默认心理老师
            if ("auto-psychology".equals(assignmentMode) && StrUtil.isNotBlank(defaultRoleCode)) {
                log.info("在部门路径上未找到心理老师，尝试查询默认心理老师");
                
                RoleDO defaultRole = roleMapper.selectByCode(defaultRoleCode);
                if (defaultRole != null) {
                    List<UserRoleDO> defaultUserRoles = userRoleMapper.selectListByRoleId(defaultRole.getId());
                    
                    if (CollUtil.isNotEmpty(defaultUserRoles)) {
                        // 查询用户状态，过滤启用的用户
                        Set<Long> defaultUserIds = defaultUserRoles.stream()
                            .map(UserRoleDO::getUserId)
                            .collect(Collectors.toSet());
                        
                        Map<Long, AdminUserRespDTO> defaultUserMap = adminUserApi.getUserMap(defaultUserIds);
                        if (defaultUserMap != null) {
                            for (Map.Entry<Long, AdminUserRespDTO> entry : defaultUserMap.entrySet()) {
                                AdminUserRespDTO user = entry.getValue();
                                if (user != null && 
                                    CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus())) {
                                    // 找到第一个启用的默认心理老师
                                    assignHandlerToEvent(event, entry.getKey());
                                    log.info("危机事件 {} 自动分配给默认心理老师 {}", 
                                        event.getEventId(), entry.getKey());
                                    return;
                                }
                            }
                        }
                    }
                }
                
                log.warn("未找到默认心理老师，危机事件 {} 无法自动分配", event.getEventId());
            } else {
                log.warn("在部门路径上未找到符合条件的用户，危机事件 {} 无法自动分配", event.getEventId());
            }
        } catch (Exception e) {
            log.error("自动分配处理人失败，危机事件: {}", event.getEventId(), e);
        }
    }

    /**
     * 分配处理人到危机事件
     * @param event 危机事件
     * @param handlerUserId 处理人用户ID
     */
    private void assignHandlerToEvent(CrisisInterventionDO event, Long handlerUserId) {
        event.setHandlerUserId(handlerUserId);
        event.setStatus(2); // 已分配
        event.setHandleAt(LocalDateTime.now());
        event.setProgress(50);
        event.setAutoAssigned(true);
        crisisInterventionMapper.updateById(event);
    }
}