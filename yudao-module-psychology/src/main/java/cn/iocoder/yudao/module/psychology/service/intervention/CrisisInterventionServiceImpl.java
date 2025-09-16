package cn.iocoder.yudao.module.psychology.service.intervention;

import cn.hutool.core.collection.CollUtil;
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
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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

    // TODO: 从系统配置中读取
    private static String assignmentMode = "manual";

    @Override
    public InterventionDashboardSummaryVO getDashboardSummaryWithPage(InterventionDashboardReqVO reqVO) {
        InterventionDashboardSummaryVO summary = new InterventionDashboardSummaryVO();
        
        // 如果是只看我负责的
        if (Boolean.TRUE.equals(reqVO.getOnlyMine())) {
            reqVO.setCounselorUserId(SecurityFrameworkUtils.getLoginUserId());
        }
        
        // 获取统计数据（这里需要根据查询条件过滤）
        summary.setMajorCount(getCountByRiskLevelWithFilter(1, reqVO));
        summary.setSevereCount(getCountByRiskLevelWithFilter(2, reqVO));
        summary.setGeneralCount(getCountByRiskLevelWithFilter(3, reqVO));
        summary.setObservationCount(getCountByRiskLevelWithFilter(4, reqVO));
        summary.setNormalCount(getCountByRiskLevelWithFilter(5, reqVO));
        summary.setPendingAssessmentCount(getPendingAssessmentCountWithFilter(reqVO));
        
        // 计算总数
        int total = summary.getMajorCount() + summary.getSevereCount() + 
                   summary.getGeneralCount() + summary.getObservationCount() + 
                   summary.getNormalCount() + summary.getPendingAssessmentCount();
        summary.setTotalCount(total);
        
        // 构建详细统计
        Map<String, InterventionDashboardSummaryVO.LevelDetail> details = new HashMap<>();
        addLevelDetail(details, "major", summary.getMajorCount(), total);
        addLevelDetail(details, "severe", summary.getSevereCount(), total);
        addLevelDetail(details, "general", summary.getGeneralCount(), total);
        addLevelDetail(details, "observation", summary.getObservationCount(), total);
        addLevelDetail(details, "normal", summary.getNormalCount(), total);
        addLevelDetail(details, "pending", summary.getPendingAssessmentCount(), total);
        summary.setLevelDetails(details);
        
        // 获取学生分页列表
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
    
    private String[] generateStudentTags(InterventionStudentRespVO student) {
        List<String> tags = new ArrayList<>();
        
        // 根据风险等级添加标签
        if (student.getCurrentRiskLevel() != null) {
            switch (student.getCurrentRiskLevel()) {
                case 1: tags.add("重大风险"); break;
                case 2: tags.add("严重风险"); break;
                case 3: tags.add("一般风险"); break;
                case 4: tags.add("观察"); break;
                case 5: tags.add("正常"); break;
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
        // 转换为新的查询对象
        InterventionDashboardReqVO reqVO = new InterventionDashboardReqVO();
        reqVO.setClassId(classId);
        reqVO.setCounselorUserId(counselorUserId);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10); // 默认返回前10条
        
        return getDashboardSummaryWithPage(reqVO);
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
            currentLevel = 5; // 默认为正常
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

        // 如果风险等级升高到重大或严重（1或2），自动创建关注记录
        if (targetLevel <= 2 && currentLevel > targetLevel) {
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
        recordEventProcess(event.getId(), "上报事件", "危机事件已上报：" + createReqVO.getDescription());

        // 自动分配处理人（如果是自动模式）
        if ("auto".equals(assignmentMode)) {
            autoAssignHandler(event);
        }

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
        crisisInterventionMapper.updateById(event);

        // 记录分配动作
        String content = "分配负责人：" + adminUserApi.getUser(assignReqVO.getHandlerUserId()).getNickname();
        if (assignReqVO.getAssignReason() != null) {
            content += "，原因：" + assignReqVO.getAssignReason();
        }
        recordEventProcess(id, "分配负责人", content);
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
        crisisInterventionMapper.updateById(event);

        // 记录变更动作
        String content = String.format("负责人变更：%s -> %s，原因：%s",
            oldHandlerId != null ? adminUserApi.getUser(oldHandlerId).getNickname() : "无",
            adminUserApi.getUser(reassignReqVO.getNewHandlerUserId()).getNickname(),
            reassignReqVO.getReason());
        recordEventProcess(id, "更改负责人", content);
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
        recordEventProcess(id, "选择处理方式", "处理方式：" + methodName + "，原因：" + processReqVO.getProcessReason());
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

        // 记录结案动作
        recordEventProcess(id, "结案", "事件已结案：" + closeReqVO.getSummary());
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
        recordEventProcess(id, "阶段性评估", "完成阶段性评估，风险等级：" + getRiskLevelName(assessmentReqVO.getRiskLevel()));

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
    public String getAssignmentMode() {
        return assignmentMode;
    }

    @Override
    public void setAssignmentMode(String mode) {
        assignmentMode = mode;
        log.info("危机事件分配模式已设置为：{}", mode);
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

    private void autoAssignHandler(CrisisInterventionDO event) {
        // TODO: 实现自动分配逻辑
        // 根据学生绑定的心理老师或班主任自动分配
        log.info("自动分配处理人，事件ID：{}", event.getId());
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

        // 批量获取操作人信息
        List<Long> userIds = processList.stream()
            .map(CrisisEventProcessDO::getOperatorUserId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);

        return processList.stream().map(process -> {
            CrisisEventProcessHistoryVO vo = BeanUtils.toBean(process, CrisisEventProcessHistoryVO.class);

            AdminUserRespDTO operator = userMap.get(process.getOperatorUserId());
            if (operator != null) {
                vo.setOperatorName(operator.getNickname());
            }

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

    private String getRiskLevelName(Integer level) {
        // TODO: 从字典获取
        switch (level) {
            case 1: return "重大";
            case 2: return "严重";
            case 3: return "一般";
            case 4: return "观察";
            case 5: return "正常";
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
            event.setStudentProfileId(studentProfileId);
            event.setTitle("风险等级调整");
            event.setDescription("风险等级调整至" + getRiskLevelName(riskLevel) + "：" + reason);
            event.setRiskLevel(riskLevel);
            event.setUrgencyLevel(riskLevel <= 2 ? 1 : 2); // 重大和严重设为高紧急度
            event.setPriority(riskLevel <= 2 ? 1 : 2); // 重大和严重设为高优先级
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
}