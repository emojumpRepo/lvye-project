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
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisEventAssessmentDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisEventProcessDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisInterventionDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.intervention.InterventionLevelHistoryDO;
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
import cn.iocoder.yudao.module.psychology.enums.InterventionAssignmentModeEnum;
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

    // 配置键常量
    private static final String CONFIG_KEY_ASSIGNMENT_MODE = "intervention.assignment.mode";
    private static final String CONFIG_KEY_DEFAULT_PSYCHOLOGY = "intervention.assignment.defaultPsychology";

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
            pageSize = 10;
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
    public Long createCrisisEvent(CrisisEventCreateReqVO createReqVO) {
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
        event.setReporterUserId(currentUserId);
        event.setReportedAt(LocalDateTime.now());
        event.setProgress(0);
        event.setAutoAssigned(false);
        
        // 不要手动设置creator和updater，让框架自动填充
        // event.setCreator(null);
        // event.setUpdater(null);

        crisisInterventionMapper.insert(event);

        // 记录上报动作
        recordEventProcessWithUsers(event.getId(), "REPORT", createReqVO.getDescription(),
            "风险等级：" + getRiskLevelName(createReqVO.getRiskLevel()),
            null, null);
        
        // 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("eventId", event.getId());
        meta.put("riskLevel", createReqVO.getRiskLevel());
        meta.put("riskLevelName", getRiskLevelName(createReqVO.getRiskLevel()));
        meta.put("reporterUserId", currentUserId);
        meta.put("description", createReqVO.getDescription());
        meta.put("status", "已上报");
        
        String content = String.format("上报了危机事件，风险等级：%s", getRiskLevelName(createReqVO.getRiskLevel()));
        studentTimelineService.saveTimelineWithMeta(
            createReqVO.getStudentProfileId(),
            TimelineEventTypeEnum.CRISIS_INTERVENTION.getType(),
            "危机事件上报",
            "crisis_event_" + event.getId(),
            content,
            meta
        );

        // 自动分配处理人（如果是自动模式）
        // if ("auto".equals(assignmentMode)) {
        //     autoAssignHandler(event);
        // }

        return event.getId();
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
    public Map<String, Long> getCrisisEventStatistics() {
        Map<String, Long> statistics = new HashMap<>();

        statistics.put("pending", crisisInterventionMapper.countByStatus(1)); // 待处理
        statistics.put("processing", crisisInterventionMapper.countByStatus(3)); // 处理中
        statistics.put("resolved", crisisInterventionMapper.countByStatus(4)); // 已解决
        statistics.put("monitoring", crisisInterventionMapper.countByStatus(5)); // 持续关注

        return statistics;
    }

    @Override
    public List<CrisisEventStatusStatisticsVO> getCrisisEventStatusStatistics() {
        // 从数据库获取实际的统计数据
        List<CrisisEventStatusStatisticsVO> dbStatistics = crisisInterventionMapper.selectStatusStatistics();

        // 创建一个包含所有状态的Map，初始值都为0
        Map<Integer, Long> statusMap = new HashMap<>();
        // 1-已上报、2-已分配、3-处理中、4-已结案、5-持续关注
        statusMap.put(1, 0L);
        statusMap.put(2, 0L);
        statusMap.put(3, 0L);
        statusMap.put(4, 0L);
        statusMap.put(5, 0L);

        // 用数据库查询结果更新Map
        for (CrisisEventStatusStatisticsVO stat : dbStatistics) {
            if (stat.getStatus() != null) {
                statusMap.put(stat.getStatus(), stat.getCount());
            }
        }

        // 将Map转换为List返回
        List<CrisisEventStatusStatisticsVO> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : statusMap.entrySet()) {
            result.add(CrisisEventStatusStatisticsVO.builder()
                    .status(entry.getKey())
                    .count(entry.getValue())
                    .build());
        }

        // 按状态值排序
        result.sort((a, b) -> a.getStatus().compareTo(b.getStatus()));

        return result;
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
        vo.setProcessHistory(convertProcessHistory(processList));

        // 加载最新评估
        CrisisEventAssessmentDO latestAssessment = eventAssessmentMapper.selectLatestByEventId(id);
        if (latestAssessment != null) {
            CrisisEventRespVO.LatestAssessmentVO assessmentVO = new CrisisEventRespVO.LatestAssessmentVO();
            assessmentVO.setAssessTime(latestAssessment.getCreateTime());
            assessmentVO.setRiskLevel(latestAssessment.getRiskLevel());
            assessmentVO.setProblemTypes(latestAssessment.getProblemTypes());
            assessmentVO.setFollowUpSuggestion(latestAssessment.getFollowUpSuggestion());
            vo.setLatestAssessment(assessmentVO);
        }

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
            null); // 初次分配，没有原负责人
        
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
            "crisis_event_" + id,
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
            oldHandlerId);
        
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
            "crisis_event_" + id,
            content,
            meta
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processCrisisEvent(Long id, CrisisEventProcessReqVO processReqVO) {
        // 验证事件存在
        CrisisInterventionDO event = validateEventExists(id);

        // 更新处理方式和状态
        event.setProcessMethod(processReqVO.getProcessMethod());
        event.setProcessReason(processReqVO.getProcessReason());
        event.setStatus(3); // 处理中
        event.setProgress(25);
        crisisInterventionMapper.updateById(event);

        // 记录处理动作
        String methodName = getProcessMethodName(processReqVO.getProcessMethod());
        recordEventProcessWithUsers(id, "CHOOSE_PROCESS", methodName,
            processReqVO.getProcessReason(),
            null, null);
        
        // 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("eventId", id);
        meta.put("processMethod", processReqVO.getProcessMethod());
        meta.put("processMethodName", methodName);
        meta.put("processReason", processReqVO.getProcessReason());
        meta.put("status", "处理中");
        meta.put("progress", 25);
        
        String content = String.format("开始处理危机事件，处理方式：%s", methodName);
        studentTimelineService.saveTimelineWithMeta(
            event.getStudentProfileId(),
            TimelineEventTypeEnum.CRISIS_INTERVENTION.getType(),
            "危机事件处理",
            "crisis_event_" + id,
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
        event.setStatus(4); // 已结案
        event.setClosureSummary(closeReqVO.getSummary());
        event.setProgress(100);
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
        eventAssessmentMapper.insert(assessment);
        
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
            "危机事件结案",
            "crisis_event_" + id,
            content,
            meta
        );

        // 使用结构化方式记录结案动作
        recordEventProcessWithUsers(id, "CLOSE", closeReqVO.getSummary(),
            "风险等级：" + getRiskLevelName(closeReqVO.getRiskLevel()),
            null, null);
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
        eventAssessmentMapper.insert(assessment);

        // 更新进度
        event.setProgress(Math.min(event.getProgress() + 25, 75));
        crisisInterventionMapper.updateById(event);

        // 记录评估动作
        recordEventProcessWithUsers(id, "STAGE_ASSESSMENT", assessmentReqVO.getContent(),
            "风险等级：" + getRiskLevelName(assessmentReqVO.getRiskLevel()),
            null, null);

        // 根据后续建议决定下一步
        if (assessmentReqVO.getFollowUpSuggestion() == 4) { // 问题基本解决
            event.setStatus(4); // 可以考虑结案
            crisisInterventionMapper.updateById(event);
        }
    }

    @Override
    public PageResult<CrisisEventProcessHistoryVO> getProcessHistory(Long id, Integer pageNo, Integer pageSize) {
        // TODO: 实现分页查询处理历史
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
        // 确保配置已初始化
        initializeConfigs();

        InterventionAssignmentSettingVO settingVO = new InterventionAssignmentSettingVO();

        // 从数据库配置中读取分配模式
        var modeConfig = configService.getConfigByKey(CONFIG_KEY_ASSIGNMENT_MODE);
        String mode = modeConfig != null ? modeConfig.getValue() : InterventionAssignmentModeEnum.MANUAL.getMode();
        settingVO.setMode(mode);

        // 从数据库配置中读取默认心理老师ID
        Long defaultPsychologyId = null;
        var psychologyConfig = configService.getConfigByKey(CONFIG_KEY_DEFAULT_PSYCHOLOGY);
        if (psychologyConfig != null && StrUtil.isNotBlank(psychologyConfig.getValue())) {
            try {
                Long configValue = Long.parseLong(psychologyConfig.getValue());
                // 0表示未设置，不使用默认值
                if (configValue > 0) {
                    defaultPsychologyId = configValue;
                }
            } catch (NumberFormatException e) {
                log.error("默认心理老师ID配置值无效: {}", psychologyConfig.getValue());
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

        // 确保配置已初始化
        initializeConfigs();

        // 更新配置值
        // 更新分配模式
        var modeConfig = configService.getConfigByKey(CONFIG_KEY_ASSIGNMENT_MODE);
        if (modeConfig != null) {
            var updateModeReqVO = new cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigSaveReqVO();
            updateModeReqVO.setId(modeConfig.getId()); // 设置ID用于更新
            updateModeReqVO.setCategory(modeConfig.getCategory());
            updateModeReqVO.setName(modeConfig.getName());
            updateModeReqVO.setKey(modeConfig.getConfigKey());
            updateModeReqVO.setValue(settingVO.getMode());
            updateModeReqVO.setVisible(modeConfig.getVisible());
            updateModeReqVO.setRemark(modeConfig.getRemark());
            configService.updateConfig(updateModeReqVO);
        }

        // 更新默认心理老师ID
        var psychologyConfig = configService.getConfigByKey(CONFIG_KEY_DEFAULT_PSYCHOLOGY);
        if (psychologyConfig != null) {
            var updatePsychologyReqVO = new cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigSaveReqVO();
            updatePsychologyReqVO.setId(psychologyConfig.getId()); // 设置ID用于更新
            updatePsychologyReqVO.setCategory(psychologyConfig.getCategory());
            updatePsychologyReqVO.setName(psychologyConfig.getName());
            updatePsychologyReqVO.setKey(psychologyConfig.getConfigKey());
            // 如果defaultPsychologyId为null或0，存储为"0"
            String valueToStore = (settingVO.getDefaultPsychologyId() == null || settingVO.getDefaultPsychologyId() == 0) ?
                                  "0" : settingVO.getDefaultPsychologyId().toString();
            updatePsychologyReqVO.setValue(valueToStore);
            updatePsychologyReqVO.setVisible(psychologyConfig.getVisible());
            updatePsychologyReqVO.setRemark(psychologyConfig.getRemark());
            configService.updateConfig(updatePsychologyReqVO);
        }

        System.out.println("危机事件分配模式已设置为：{}，默认心理老师ID：{}"+settingVO.getMode()+", "+settingVO.getDefaultPsychologyId());
    }

    /**
     * 初始化干预系统模式配置（如果不存在）
     */
    private void initializeConfigs() {
        // 检查并初始化分配模式配置
        var modeConfig = configService.getConfigByKey(CONFIG_KEY_ASSIGNMENT_MODE);
        if (modeConfig == null) {
            var createModeReqVO = new cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigSaveReqVO();
            createModeReqVO.setCategory("intervention");
            createModeReqVO.setName("干预系统分配模式");
            createModeReqVO.setKey(CONFIG_KEY_ASSIGNMENT_MODE);
            createModeReqVO.setValue(InterventionAssignmentModeEnum.MANUAL.getMode());
            createModeReqVO.setVisible(true);
            createModeReqVO.setRemark("危机事件分配模式：manual-手动分配, auto-psychology-自动分配给心理老师, auto-head-teacher-自动分配给班主任");
            configService.createConfig(createModeReqVO);
            log.info("初始化配置: key={}, name={}, defaultValue={}", CONFIG_KEY_ASSIGNMENT_MODE, "干预系统模式", InterventionAssignmentModeEnum.MANUAL.getMode());
        }

        // 检查并初始化默认心理老师配置
        var psychologyConfig = configService.getConfigByKey(CONFIG_KEY_DEFAULT_PSYCHOLOGY);
        if (psychologyConfig == null) {
            var createPsychologyReqVO = new cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigSaveReqVO();
            createPsychologyReqVO.setCategory("intervention");
            createPsychologyReqVO.setName("干预默认心理老师");
            createPsychologyReqVO.setKey(CONFIG_KEY_DEFAULT_PSYCHOLOGY);
            createPsychologyReqVO.setValue("0"); // 使用"0"表示未设置，而不是空字符串
            createPsychologyReqVO.setVisible(true);
            createPsychologyReqVO.setRemark("当学生档案未绑定责任心理老师时使用的默认心理老师ID");
            configService.createConfig(createPsychologyReqVO);
            log.info("初始化配置: key={}, name={}, defaultValue={}", CONFIG_KEY_DEFAULT_PSYCHOLOGY, "干预默认心理老师", "0");
        }
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
                                            String reason, Long relatedUserId, Long originalUserId) {
        CrisisEventProcessDO process = new CrisisEventProcessDO();
        process.setEventId(eventId);
        process.setOperatorUserId(SecurityFrameworkUtils.getLoginUserId());
        process.setAction(action);
        process.setContent(StrUtil.blankToDefault(content, action));
        process.setReason(reason);
        process.setRelatedUserId(relatedUserId);
        process.setOriginalUserId(originalUserId);
        eventProcessMapper.insert(process);
    }

    // private void autoAssignHandler(CrisisInterventionDO event) {
    //     // 获取学生的主责咨询师，如果没有则使用默认处理人
    //     Long handlerUserId = studentCounselorAssignmentService.getCounselorUserIdOrDefault(
    //             event.getStudentProfileId(), defaultHandlerUserId);
        
    //     String assignReason = "自动分配";
    //     if (studentCounselorAssignmentService.hasPrimaryCounselor(event.getStudentProfileId())) {
    //         assignReason = "自动分配给学生的责任心理老师";
    //     } else if (handlerUserId != null) {
    //         assignReason = "自动分配给默认心理老师";
    //     }
        
    //     // 如果找到了合适的处理人，进行分配
    //     if (handlerUserId != null) {
    //         // 更新事件处理人和状态
    //         event.setHandlerUserId(handlerUserId);
    //         event.setStatus(2); // 已分配
    //         event.setAutoAssigned(true);
    //         crisisInterventionMapper.updateById(event);
            
    //         // 记录分配动作
    //         recordEventProcessWithUsers(event.getId(), "AUTO_ASSIGN", assignReason,
    //                 assignReason, handlerUserId, null);
            
    //         // 添加时间线记录
    //         AdminUserRespDTO handler = adminUserApi.getUser(handlerUserId);
    //         Map<String, Object> meta = new HashMap<>();
    //         meta.put("eventId", event.getId());
    //         meta.put("handlerUserId", handlerUserId);
    //         meta.put("handlerName", handler != null ? handler.getNickname() : "未知");
    //         meta.put("assignReason", assignReason);
    //         meta.put("status", "已分配");
    //         meta.put("autoAssigned", true);
            
    //         String content = String.format("危机事件已自动分配给 %s 处理", 
    //             handler != null ? handler.getNickname() : "未知");
    //         studentTimelineService.saveTimelineWithMeta(
    //             event.getStudentProfileId(),
    //             TimelineEventTypeEnum.CRISIS_INTERVENTION.getType(),
    //             "危机事件自动分配",
    //             "crisis_event_" + event.getId(),
    //             content,
    //             meta
    //         );
            
    //         log.info("自动分配处理人成功，事件ID：{}，处理人ID：{}，分配原因：{}", 
    //                 event.getId(), handlerUserId, assignReason);
    //     } else {
    //         log.warn("自动分配处理人失败，事件ID：{}，未找到合适的处理人", event.getId());
    //     }
    // }

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

    private List<CrisisEventRespVO.ProcessHistoryVO> convertProcessHistory(List<CrisisEventProcessDO> processList) {
        if (CollUtil.isEmpty(processList)) {
            return new ArrayList<>();
        }

        // 批量获取操作人信息
        List<Long> userIds = processList.stream()
            .map(CrisisEventProcessDO::getOperatorUserId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);

        return processList.stream().map(process -> {
            CrisisEventRespVO.ProcessHistoryVO vo = new CrisisEventRespVO.ProcessHistoryVO();
            vo.setOperateTime(process.getCreateTime());
            vo.setAction(process.getAction());
            vo.setContent(process.getContent());
            vo.setAttachments(process.getAttachments());

            AdminUserRespDTO operator = userMap.get(process.getOperatorUserId());
            if (operator != null) {
                vo.setOperatorName(operator.getNickname());
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
            vo.setAttachments(process.getAttachments());
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

    private String getProcessMethodName(Integer method) {
        // TODO: 从字典获取
        switch (method) {
            case 1: return "心理访谈";
            case 2: return "量表评估";
            case 3: return "持续关注";
            case 4: return "直接解决";
            default: return "未知";
        }
    }

    private String getActionName(String action) {
        // TODO: 从字典获取操作类型名称
        if (StrUtil.isBlank(action)) {
            return "";
        }
        switch (action) {
            case "REPORT": return "上报事件";
            case "ASSIGN_HANDLER": return "分配负责人";
            case "REASSIGN_HANDLER": return "更改负责人";
            case "CHOOSE_PROCESS": return "选择处理方式";
            case "STAGE_ASSESSMENT": return "阶段性评估";
            case "CLOSE": return "结案";
            case "REOPEN": return "重新开启";
            default: return action;
        }
    }

    private String getRiskLevelName(Integer level) {
        // 字典类型：crisis_level
        switch (level) {
            case 1: return "待评";
            case 2: return "持续观察";
            case 3: return "一般";
            case 4: return "严重";
            case 5: return "重大";
            default: return "未知";
        }
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
            event.setAutoAssigned(true);
            event.setSourceType(1); // 系统自动

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
}