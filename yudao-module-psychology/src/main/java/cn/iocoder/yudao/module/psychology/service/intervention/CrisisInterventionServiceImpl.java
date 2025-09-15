package cn.iocoder.yudao.module.psychology.service.intervention;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisEventAssessmentDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisEventProcessDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisInterventionDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisEventAssessmentMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisEventProcessMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisInterventionMapper;
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
    private AdminUserApi adminUserApi;

    // TODO: 从系统配置中读取
    private static String assignmentMode = "manual";

    @Override
    public InterventionDashboardSummaryVO getDashboardSummary(Long classId, Long counselorUserId) {
        InterventionDashboardSummaryVO summary = new InterventionDashboardSummaryVO();
        
        // 获取各风险等级的学生数
        summary.setMajorCount(crisisInterventionMapper.countByRiskLevel(1).intValue());
        summary.setSevereCount(crisisInterventionMapper.countByRiskLevel(2).intValue());
        summary.setGeneralCount(crisisInterventionMapper.countByRiskLevel(3).intValue());
        summary.setObservationCount(crisisInterventionMapper.countByRiskLevel(4).intValue());
        summary.setNormalCount(crisisInterventionMapper.countByRiskLevel(5).intValue());
        
        // TODO: 获取待评估学生数（需要关联学生档案表）
        summary.setPendingAssessmentCount(0);
        
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
        
        return summary;
    }

    private void addLevelDetail(Map<String, InterventionDashboardSummaryVO.LevelDetail> details, 
                                String level, Integer count, Integer total) {
        InterventionDashboardSummaryVO.LevelDetail detail = new InterventionDashboardSummaryVO.LevelDetail();
        detail.setCount(count);
        detail.setPercentage(total > 0 ? (count * 100.0 / total) : 0);
        detail.setChange(0); // TODO: 计算环比变化
        details.put(level, detail);
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

        // TODO: 更新学生的风险等级
        // 记录等级变更历史
        log.info("调整学生 {} 的风险等级为 {}", studentProfileId, adjustReqVO.getTargetLevel());
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

        // 创建危机事件
        CrisisInterventionDO event = BeanUtils.toBean(createReqVO, CrisisInterventionDO.class);
        event.setStatus(1); // 已上报
        event.setReporterUserId(SecurityFrameworkUtils.getLoginUserId());
        event.setReportedAt(LocalDateTime.now());
        event.setProgress(0);
        event.setAutoAssigned(false);

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
}