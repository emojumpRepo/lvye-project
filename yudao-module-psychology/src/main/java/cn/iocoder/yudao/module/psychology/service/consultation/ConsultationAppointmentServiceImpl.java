package cn.iocoder.yudao.module.psychology.service.consultation;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfilePageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAppointmentDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAssessmentDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.ConsultationAppointmentMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.ConsultationAssessmentMapper;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.*;

/**
 * 咨询预约 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ConsultationAppointmentServiceImpl implements ConsultationAppointmentService {

    @Resource
    private ConsultationAppointmentMapper appointmentMapper;
    
    @Resource
    private ConsultationAssessmentMapper assessmentMapper;
    
    @Resource
    private StudentProfileService studentProfileService;
    
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAppointment(ConsultationAppointmentCreateReqVO createReqVO) {
        // 验证学生是否存在
        StudentProfileVO student = studentProfileService.getStudentProfile(createReqVO.getStudentProfileId());
        if (student == null) {
            throw ServiceExceptionUtil.exception(STUDENT_PROFILE_NOT_EXISTS);
        }

        // 使用入参指定的咨询师用户ID
        Long counselorUserId = createReqVO.getCounselorUserId();
        
        // 验证时间逻辑：结束时间必须晚于开始时间
        if (createReqVO.getAppointmentEndTime().isBefore(createReqVO.getAppointmentStartTime()) ||
            createReqVO.getAppointmentEndTime().isEqual(createReqVO.getAppointmentStartTime())) {
            throw ServiceExceptionUtil.exception(CONSULTATION_TIME_INVALID);
        }
        
        // 检查时间冲突
        if (appointmentMapper.hasTimeConflict(counselorUserId, createReqVO.getAppointmentStartTime(), 
            createReqVO.getAppointmentEndTime(), null)) {
            throw ServiceExceptionUtil.exception(CONSULTATION_TIME_CONFLICT);
        }

        // 创建预约
        ConsultationAppointmentDO appointment = BeanUtils.toBean(createReqVO, ConsultationAppointmentDO.class);
        appointment.setCounselorUserId(counselorUserId);
        appointment.setStatus(1); // 已预约
        appointment.setCurrentStep(1);
        appointment.setOverdue(false);
        
        // 如果没有传递durationMinutes，则根据开始时间和结束时间计算
        if (appointment.getDurationMinutes() == null) {
            long minutes = java.time.Duration.between(createReqVO.getAppointmentStartTime(), 
                createReqVO.getAppointmentEndTime()).toMinutes();
            appointment.setDurationMinutes((int) minutes);
        }
        
        appointmentMapper.insert(appointment);
        
        // TODO: 发送通知给学生
        if (createReqVO.getNotifyStudent()) {
            sendNotificationToStudent(appointment.getId(), student.getId());
        }
        
        return appointment.getId();
    }

    @Override
    public void updateAppointment(ConsultationAppointmentUpdateReqVO updateReqVO) {
        // 校验存在
        ConsultationAppointmentDO appointment = validateAppointmentExists(updateReqVO.getId());
        
        // 使用入参指定的咨询师用户ID
        Long counselorUserId = updateReqVO.getCounselorUserId();
        
        // 确定要使用的开始时间和结束时间
        LocalDateTime startTime = updateReqVO.getAppointmentStartTime() != null ? 
            updateReqVO.getAppointmentStartTime() : appointment.getAppointmentStartTime();
        LocalDateTime endTime = updateReqVO.getAppointmentEndTime() != null ? 
            updateReqVO.getAppointmentEndTime() : appointment.getAppointmentEndTime();
        
        // 如果修改了开始时间或结束时间，需要验证逻辑和冲突
        if ((updateReqVO.getAppointmentStartTime() != null && 
             !updateReqVO.getAppointmentStartTime().equals(appointment.getAppointmentStartTime())) ||
            (updateReqVO.getAppointmentEndTime() != null && 
             !updateReqVO.getAppointmentEndTime().equals(appointment.getAppointmentEndTime()))) {
            
            // 验证时间逻辑：结束时间必须晚于开始时间
            if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                throw ServiceExceptionUtil.exception(CONSULTATION_TIME_INVALID);
            }
            
            // 检查时间冲突（使用新的咨询师ID）
            if (appointmentMapper.hasTimeConflict(counselorUserId, 
                startTime, endTime, updateReqVO.getId())) {
                throw ServiceExceptionUtil.exception(CONSULTATION_TIME_CONFLICT);
            }
        }
        
        // 更新
        ConsultationAppointmentDO updateObj = BeanUtils.toBean(updateReqVO, ConsultationAppointmentDO.class);
        updateObj.setCounselorUserId(counselorUserId);
        
        // 如果没有传递durationMinutes但修改了时间，则重新计算时长
        if (updateObj.getDurationMinutes() == null && 
            (updateReqVO.getAppointmentStartTime() != null || updateReqVO.getAppointmentEndTime() != null)) {
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            updateObj.setDurationMinutes((int) minutes);
        }
        
        appointmentMapper.updateById(updateObj);
    }

    @Override
    public void deleteAppointment(Long id) {
        // 校验存在
        validateAppointmentExists(id);
        // 删除
        appointmentMapper.deleteById(id);
    }

    @Override
    public ConsultationAppointmentRespVO getAppointment(Long id) {
        ConsultationAppointmentDO appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            return null;
        }
        List<ConsultationAppointmentRespVO> voList = convertToRespVOList(Collections.singletonList(appointment));
        return voList.isEmpty() ? null : voList.get(0);
    }

    @Override
    public PageResult<ConsultationAppointmentRespVO> getAppointmentPage(ConsultationAppointmentPageReqVO pageReqVO) {
        // 查询分页数据
        PageResult<ConsultationAppointmentDO> pageResult = appointmentMapper.selectPage(pageReqVO);
        
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }
        
        // 转换为VO并填充关联信息
        List<ConsultationAppointmentRespVO> voList = convertToRespVOList(pageResult.getList());
        
        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public ConsultationStatisticsRespVO getStatistics() {
        LocalDateTime now = LocalDateTime.now();

        ConsultationStatisticsRespVO result = new ConsultationStatisticsRespVO();

        // 统计今天的咨询数(基于开始时间)
        result.setTodayCount((int) appointmentMapper.countTodayConsultations());

        // 统计已完成数(状态为3-已闭环)
        result.setCompletedCount((int) appointmentMapper.countCompletedConsultations());

        // 统计待完成数(状态为1或2且未逾期)
        result.setPendingCount((int) appointmentMapper.countPendingConsultations(now));

        // 统计逾期数(结束时间小于当前时间且未完成)
        result.setOverdueCount((int) appointmentMapper.countOverdueConsultations(now));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeAppointment(Long id, Boolean fillAssessmentNow) {
        // 校验存在
        ConsultationAppointmentDO appointment = validateAppointmentExists(id);
        
        // 校验状态
        if (appointment.getStatus() != 1) {
            throw ServiceExceptionUtil.exception(CONSULTATION_STATUS_ERROR);
        }
        
        // 更新状态为已完成
        appointment.setStatus(2);
        appointment.setCurrentStep(2);
        appointment.setActualTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        
        // 如果不立即填写评估，可以记录一个待办事项
        if (!Boolean.TRUE.equals(fillAssessmentNow)) {
            // TODO: 创建待办事项提醒填写评估
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustAppointmentTime(Long id, ConsultationAppointmentAdjustTimeReqVO adjustReqVO) {
        // 校验存在
        ConsultationAppointmentDO appointment = validateAppointmentExists(id);
        
        // 校验状态（只有已预约状态可以调整时间）
        if (appointment.getStatus() != 1) {
            throw ServiceExceptionUtil.exception(CONSULTATION_STATUS_ERROR);
        }
        
        // 验证时间逻辑：结束时间必须晚于开始时间
        if (adjustReqVO.getNewAppointmentEndTime().isBefore(adjustReqVO.getNewAppointmentStartTime()) ||
            adjustReqVO.getNewAppointmentEndTime().isEqual(adjustReqVO.getNewAppointmentStartTime())) {
            throw ServiceExceptionUtil.exception(CONSULTATION_TIME_INVALID);
        }
        
        // 检查时间冲突
        if (appointmentMapper.hasTimeConflict(appointment.getCounselorUserId(), 
            adjustReqVO.getNewAppointmentStartTime(), adjustReqVO.getNewAppointmentEndTime(), id)) {
            throw ServiceExceptionUtil.exception(CONSULTATION_TIME_CONFLICT);
        }
        
        // 计算新的时长
        long newDurationMinutes = java.time.Duration.between(
            adjustReqVO.getNewAppointmentStartTime(), 
            adjustReqVO.getNewAppointmentEndTime()
        ).toMinutes();
        
        // 更新开始时间、结束时间和时长
        appointment.setAppointmentStartTime(adjustReqVO.getNewAppointmentStartTime());
        appointment.setAppointmentEndTime(adjustReqVO.getNewAppointmentEndTime());
        appointment.setDurationMinutes((int) newDurationMinutes);
        appointmentMapper.updateById(appointment);
        
        // TODO: 发送通知给学生
        if (appointment.getNotifyStudent()) {
            sendNotificationToStudent(id, appointment.getStudentProfileId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long id, ConsultationAppointmentCancelReqVO cancelReqVO) {
        // 校验存在
        ConsultationAppointmentDO appointment = validateAppointmentExists(id);
        
        // 校验状态（只有已预约状态可以取消）
        if (appointment.getStatus() != 1) {
            throw ServiceExceptionUtil.exception(CONSULTATION_STATUS_ERROR);
        }
        
        // 更新状态为已取消
        appointment.setStatus(4);
        String cancellationReason = cancelReqVO.getReason();
        if (cancelReqVO.getCustomReason() != null) {
            cancellationReason += "：" + cancelReqVO.getCustomReason();
        }
        appointment.setCancellationReason(cancellationReason);
        appointmentMapper.updateById(appointment);
        
        // TODO: 发送通知给学生
        if (appointment.getNotifyStudent()) {
            sendNotificationToStudent(id, appointment.getStudentProfileId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void supplementAppointment(Long id, ConsultationAppointmentSupplementReqVO supplementReqVO) {
        // 校验存在
        ConsultationAppointmentDO appointment = validateAppointmentExists(id);
        
        // 校验状态（只有已预约或逾期状态可以补录）
        if (appointment.getStatus() != 1) {
            throw ServiceExceptionUtil.exception(CONSULTATION_STATUS_ERROR);
        }
        
        // 更新为已完成并记录实际时间
        appointment.setStatus(2);
        appointment.setCurrentStep(2);
        appointment.setActualTime(supplementReqVO.getActualTime());
        if (supplementReqVO.getNotes() != null && !supplementReqVO.getNotes().trim().isEmpty()) {
            String notes = appointment.getNotes() != null ? appointment.getNotes() + "\n" : "";
            appointment.setNotes(notes + "补录说明：" + supplementReqVO.getNotes());
        }
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void sendReminder(Long id) {
        // 校验存在
        ConsultationAppointmentDO appointment = validateAppointmentExists(id);

        // TODO: 发送催办提醒
        log.info("发送催办提醒，预约ID：{}", id);
    }

    @Override
    public ConsultationAppointmentCheckTimeConflictRespVO checkTimeConflict(ConsultationAppointmentCheckTimeConflictReqVO reqVO) {
        // 验证时间逻辑：结束时间必须晚于开始时间
        if (reqVO.getAppointmentEndTime() <= reqVO.getAppointmentStartTime()) {
            return new ConsultationAppointmentCheckTimeConflictRespVO(true, "预约结束时间必须晚于开始时间");
        }

        // 将时间戳转换为LocalDateTime
        LocalDateTime startTime = Instant.ofEpochMilli(reqVO.getAppointmentStartTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime endTime = Instant.ofEpochMilli(reqVO.getAppointmentEndTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // 检查时间冲突
        boolean hasConflict = appointmentMapper.hasTimeConflict(
                reqVO.getCounselorUserId(),
                startTime,
                endTime,
                reqVO.getExcludeId()
        );

        if (hasConflict) {
            return new ConsultationAppointmentCheckTimeConflictRespVO(true, "该时间段与已有预约冲突");
        } else {
            return new ConsultationAppointmentCheckTimeConflictRespVO(false, "该时间段可以预约");
        }
    }

    @Override
    public ConsultationAppointmentWeeklyRespVO getWeeklyAppointments(ConsultationAppointmentWeeklyReqVO reqVO) {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 计算目标周的周一（一周的开始）
        LocalDate weekStart = today
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(reqVO.getWeekOffset() != null ? reqVO.getWeekOffset() : 0);

        // 计算目标周的周日（一周的结束）
        LocalDate weekEnd = weekStart.plusDays(6);

        // 转换为LocalDateTime用于数据库查询
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);

        // 查询该周的所有预约数据
        List<ConsultationAppointmentDO> appointments = appointmentMapper.selectByDateRange(startDateTime, endDateTime);

        // 转换为RespVO（包含学生、咨询师等关联信息）
        List<ConsultationAppointmentRespVO> appointmentVOs = convertToRespVOList(appointments);

        // 按日期分组
        Map<LocalDate, List<ConsultationAppointmentRespVO>> appointmentsByDate = appointmentVOs.stream()
                .collect(Collectors.groupingBy(vo -> vo.getAppointmentStartTime().toLocalDate()));

        // 构建返回的每日数据列表（确保包含完整的7天）
        List<DailyAppointmentVO> dailyAppointments = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1-7，星期一是1，星期日是7

            DailyAppointmentVO dailyVO = new DailyAppointmentVO();
            dailyVO.setKey(dayOfWeek);
            dailyVO.setDate(date);

            List<ConsultationAppointmentRespVO> dayAppointments = appointmentsByDate.getOrDefault(date, new ArrayList<>());
            dailyVO.setAppointments(dayAppointments);
            dailyVO.setCount(dayAppointments.size());

            dailyAppointments.add(dailyVO);
        }

        // 构建响应VO
        ConsultationAppointmentWeeklyRespVO result = new ConsultationAppointmentWeeklyRespVO();
        result.setWeekStart(weekStart);
        result.setWeekEnd(weekEnd);
        result.setDailyAppointments(dailyAppointments);

        return result;
    }

    @Override
    public ConsultationAppointmentTimeRangeRespVO getTimeRangeData(ConsultationAppointmentTimeRangeReqVO reqVO) {
        // 1. 计算时间范围
        LocalDate referenceDate = reqVO.getReferenceDate() != null ? reqVO.getReferenceDate() : LocalDate.now();
        Integer offset = reqVO.getOffset() != null ? reqVO.getOffset() : 0;

        LocalDate startDate;
        LocalDate endDate;

        switch (reqVO.getTimeGranularity().toLowerCase()) {
            case "day":
                // 日粒度：计算某一天
                LocalDate targetDay = referenceDate.plusDays(offset);
                startDate = targetDay;
                endDate = targetDay;
                break;

            case "week":
                // 周粒度：计算某一周的周一到周日
                LocalDate targetWeek = referenceDate.plusWeeks(offset);
                startDate = targetWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = startDate.plusDays(6);
                break;

            case "month":
                // 月粒度：计算某一月的1号到月底
                LocalDate targetMonth = referenceDate.plusMonths(offset);
                startDate = targetMonth.with(TemporalAdjusters.firstDayOfMonth());
                endDate = targetMonth.with(TemporalAdjusters.lastDayOfMonth());
                break;

            default:
                throw ServiceExceptionUtil.exception(CONSULTATION_TIME_GRANULARITY_INVALID);
        }

        // 2. 查询时间范围内的预约数据
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<ConsultationAppointmentDO> appointments = appointmentMapper.selectByDateRange(startDateTime, endDateTime);

        // 3. 根据咨询师ID过滤（如果指定）
        if (reqVO.getCounselorUserId() != null) {
            appointments = appointments.stream()
                    .filter(a -> reqVO.getCounselorUserId().equals(a.getCounselorUserId()))
                    .collect(Collectors.toList());
        }

        // 4. 转换为RespVO（包含学生、咨询师等关联信息）
        List<ConsultationAppointmentRespVO> appointmentVOs = convertToRespVOList(appointments);

        // 5. 按日期分组（生成每日数据）
        Map<LocalDate, List<ConsultationAppointmentRespVO>> appointmentsByDate = appointmentVOs.stream()
                .collect(Collectors.groupingBy(vo -> vo.getAppointmentStartTime().toLocalDate()));

        List<DailyAppointmentVO> dailyData = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DailyAppointmentVO dailyVO = new DailyAppointmentVO();
            dailyVO.setKey(currentDate.getDayOfWeek().getValue());
            dailyVO.setDate(currentDate);

            List<ConsultationAppointmentRespVO> dayAppointments = appointmentsByDate.getOrDefault(currentDate, new ArrayList<>());
            dailyVO.setAppointments(dayAppointments);
            dailyVO.setCount(dayAppointments.size());

            dailyData.add(dailyVO);
            currentDate = currentDate.plusDays(1);
        }

        // 6. 按咨询师统计（仅当未指定咨询师且请求包含统计时）
        List<CounselorStatVO> counselorStats = null;
        if (reqVO.getCounselorUserId() == null && Boolean.TRUE.equals(reqVO.getIncludeCounselorStats())) {
            // 按咨询师分组
            Map<Long, List<ConsultationAppointmentDO>> appointmentsByCounselor = appointments.stream()
                    .collect(Collectors.groupingBy(ConsultationAppointmentDO::getCounselorUserId));

            // 批量获取咨询师信息
            List<Long> counselorIds = new ArrayList<>(appointmentsByCounselor.keySet());
            Map<Long, AdminUserRespDTO> counselorMap = adminUserApi.getUserMap(counselorIds);

            counselorStats = new ArrayList<>();
            for (Map.Entry<Long, List<ConsultationAppointmentDO>> entry : appointmentsByCounselor.entrySet()) {
                Long counselorId = entry.getKey();
                List<ConsultationAppointmentDO> counselorAppointments = entry.getValue();

                CounselorStatVO statVO = new CounselorStatVO();
                statVO.setCounselorUserId(counselorId);

                AdminUserRespDTO counselor = counselorMap.get(counselorId);
                if (counselor != null) {
                    statVO.setCounselorName(counselor.getNickname());
                }

                // 统计各状态数量
                statVO.setTotalCount(counselorAppointments.size());
                statVO.setScheduledCount((int) counselorAppointments.stream().filter(a -> a.getStatus() == 1).count());
                statVO.setCompletedCount((int) counselorAppointments.stream().filter(a -> a.getStatus() == 2).count());
                statVO.setClosedLoopCount((int) counselorAppointments.stream().filter(a -> a.getStatus() == 3).count());
                statVO.setCanceledCount((int) counselorAppointments.stream().filter(a -> a.getStatus() == 4).count());
                statVO.setOverdueCount((int) counselorAppointments.stream().filter(a -> Boolean.TRUE.equals(a.getOverdue())).count());

                // 统计总时长
                int totalDuration = counselorAppointments.stream()
                        .filter(a -> a.getDurationMinutes() != null)
                        .mapToInt(ConsultationAppointmentDO::getDurationMinutes)
                        .sum();
                statVO.setTotalDurationMinutes(totalDuration);

                counselorStats.add(statVO);
            }

            // 按总数降序排序
            counselorStats.sort((a, b) -> b.getTotalCount().compareTo(a.getTotalCount()));
        }

        // 7. 计算汇总统计（仅当请求包含统计时）
        TimePeriodSummaryVO summary = null;
        if (Boolean.TRUE.equals(reqVO.getIncludeSummary())) {
            summary = new TimePeriodSummaryVO();
            summary.setTotalCount(appointments.size());
            summary.setScheduledCount((int) appointments.stream().filter(a -> a.getStatus() == 1).count());
            summary.setCompletedCount((int) appointments.stream().filter(a -> a.getStatus() == 2).count());
            summary.setClosedLoopCount((int) appointments.stream().filter(a -> a.getStatus() == 3).count());
            summary.setCanceledCount((int) appointments.stream().filter(a -> a.getStatus() == 4).count());
            summary.setOverdueCount((int) appointments.stream().filter(a -> Boolean.TRUE.equals(a.getOverdue())).count());

            int totalDuration = appointments.stream()
                    .filter(a -> a.getDurationMinutes() != null)
                    .mapToInt(ConsultationAppointmentDO::getDurationMinutes)
                    .sum();
            summary.setTotalDurationMinutes(totalDuration);
            summary.setAvgDurationMinutes(appointments.isEmpty() ? 0 : totalDuration / appointments.size());
        }

        // 8. 构建响应VO
        ConsultationAppointmentTimeRangeRespVO result = new ConsultationAppointmentTimeRangeRespVO();
        result.setTimeGranularity(reqVO.getTimeGranularity());
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setTotalCount(appointments.size());
        result.setCounselorStats(counselorStats);
        result.setDailyData(dailyData);
        result.setSummary(summary);

        return result;
    }

    @Override
    public ConsultationAppointmentDateQueryRespVO getAppointmentsByDate(ConsultationAppointmentDateQueryReqVO reqVO) {
        // 1. 查询指定日期的预约数据（自动排除已取消的）
        List<ConsultationAppointmentDO> appointments = appointmentMapper.selectByDateAndCounselor(
                reqVO.getDate(),
                reqVO.getCounselorUserId()
        );

        // 2. 转换为RespVO（包含学生、咨询师等关联信息）
        List<ConsultationAppointmentRespVO> appointmentVOs = convertToRespVOList(appointments);

        // 3. 构建响应VO
        ConsultationAppointmentDateQueryRespVO result = new ConsultationAppointmentDateQueryRespVO();
        result.setDate(reqVO.getDate());
        result.setCounselorUserId(reqVO.getCounselorUserId());
        result.setTotalCount(appointmentVOs.size());
        result.setAppointments(appointmentVOs);

        return result;
    }

    private ConsultationAppointmentDO validateAppointmentExists(Long id) {
        ConsultationAppointmentDO appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw ServiceExceptionUtil.exception(CONSULTATION_APPOINTMENT_NOT_EXISTS);
        }
        return appointment;
    }

    private List<ConsultationAppointmentRespVO> convertToRespVOList(List<ConsultationAppointmentDO> appointments) {
        if (CollUtil.isEmpty(appointments)) {
            return new ArrayList<>();
        }
        
        // 批量获取学生信息
        List<Long> studentIds = appointments.stream()
            .map(ConsultationAppointmentDO::getStudentProfileId)
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
        List<Long> userIds = appointments.stream()
            .map(ConsultationAppointmentDO::getCounselorUserId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        
        // 批量获取评估信息  
        List<Long> appointmentIds = appointments.stream()
            .map(ConsultationAppointmentDO::getId)
            .collect(Collectors.toList());
        List<ConsultationAssessmentDO> assessments = assessmentMapper.selectList(
            ConsultationAssessmentDO::getAppointmentId, appointmentIds);
        Map<Long, ConsultationAssessmentDO> assessmentMap = assessments.stream()
            .collect(Collectors.toMap(ConsultationAssessmentDO::getAppointmentId, a -> a));
        
        // 转换为VO
        return appointments.stream().map(appointment -> {
            ConsultationAppointmentRespVO vo = BeanUtils.toBean(appointment, ConsultationAppointmentRespVO.class);
            
            // BeanUtils.toBean已经自动复制了appointmentStartTime和appointmentEndTime字段
            // 无需额外设置
            
            // 填充学生信息
            StudentProfileVO student = studentMap.get(appointment.getStudentProfileId());
            if (student != null) {
                vo.setStudentName(student.getName());
                vo.setStudentNumber(student.getStudentNo());
                vo.setClassName(student.getClassName());
            }
            
            // 填充咨询师信息
            AdminUserRespDTO counselor = userMap.get(appointment.getCounselorUserId());
            if (counselor != null) {
                vo.setCounselorName(counselor.getNickname());
            }
            
            // 填充是否已评估
            vo.setHasAssessment(assessmentMap.containsKey(appointment.getId()));
            
            return vo;
        }).collect(Collectors.toList());
    }

    private void sendNotificationToStudent(Long appointmentId, Long studentProfileId) {
        // TODO: 实现发送通知逻辑
        log.info("发送通知给学生，预约ID：{}，学生ID：{}", appointmentId, studentProfileId);
    }
}