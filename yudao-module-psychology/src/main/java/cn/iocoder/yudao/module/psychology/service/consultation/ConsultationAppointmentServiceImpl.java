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
            
            // 检查时间冲突
            if (appointmentMapper.hasTimeConflict(appointment.getCounselorUserId(), 
                startTime, endTime, updateReqVO.getId())) {
                throw ServiceExceptionUtil.exception(CONSULTATION_TIME_CONFLICT);
            }
        }
        
        // 更新
        ConsultationAppointmentDO updateObj = BeanUtils.toBean(updateReqVO, ConsultationAppointmentDO.class);
        
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
    public TodayConsultationRespVO getTodayConsultations() {
        // 获取当前用户的今日咨询列表
        Long counselorUserId = SecurityFrameworkUtils.getLoginUserId();
        List<ConsultationAppointmentDO> todayList = appointmentMapper.selectTodayList(counselorUserId);
        
        // 统计数据
        TodayConsultationRespVO result = new TodayConsultationRespVO();
        result.setTotalCount(todayList.size());
        result.setCompletedCount((int) todayList.stream().filter(a -> a.getStatus() == 2 || a.getStatus() == 3).count());
        result.setPendingCount((int) todayList.stream().filter(a -> a.getStatus() == 1).count());
        result.setCancelledCount((int) todayList.stream().filter(a -> a.getStatus() == 4).count());
        result.setOverdueCount((int) todayList.stream().filter(ConsultationAppointmentDO::getOverdue).count());
        
        // 转换列表
        result.setAppointments(convertToRespVOList(todayList));
        
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
        
        // 基于原时长计算新的结束时间
        LocalDateTime newEndTime = adjustReqVO.getNewAppointmentTime().plusMinutes(appointment.getDurationMinutes());
        
        // 检查时间冲突
        if (appointmentMapper.hasTimeConflict(appointment.getCounselorUserId(), 
            adjustReqVO.getNewAppointmentTime(), newEndTime, id)) {
            throw ServiceExceptionUtil.exception(CONSULTATION_TIME_CONFLICT);
        }
        
        // 更新开始和结束时间
        appointment.setAppointmentStartTime(adjustReqVO.getNewAppointmentTime());
        appointment.setAppointmentEndTime(newEndTime);
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
        appointment.setActualTime(supplementReqVO.getActualTime());
        if (supplementReqVO.getNotes() != null) {
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