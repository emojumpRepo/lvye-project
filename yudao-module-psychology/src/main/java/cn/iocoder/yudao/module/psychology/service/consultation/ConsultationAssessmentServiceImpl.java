package cn.iocoder.yudao.module.psychology.service.consultation;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.assessment.ConsultationAssessmentRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.assessment.ConsultationAssessmentSaveReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAppointmentDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAssessmentDO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
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

import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.*;

/**
 * 咨询评估 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ConsultationAssessmentServiceImpl implements ConsultationAssessmentService {

    @Resource
    private ConsultationAssessmentMapper assessmentMapper;

    @Resource
    private ConsultationAppointmentMapper appointmentMapper;

    @Resource
    private StudentProfileService studentProfileService;

    @Resource
    private AdminUserApi adminUserApi;

    // TODO: 从系统配置中读取
    private static final Integer DEFAULT_OVERDUE_HOURS = 48;

    @Override
    public ConsultationAssessmentRespVO getAssessmentByAppointmentId(Long appointmentId) {
        // 验证预约是否存在
        ConsultationAppointmentDO appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw ServiceExceptionUtil.exception(CONSULTATION_APPOINTMENT_NOT_EXISTS);
        }

        // 查询评估信息
        ConsultationAssessmentDO assessment = assessmentMapper.selectByAppointmentId(appointmentId);
        if (assessment == null) {
            // 如果没有评估，返回基本信息供创建
            ConsultationAssessmentRespVO vo = new ConsultationAssessmentRespVO();
            vo.setAppointmentId(appointmentId);
            vo.setStudentProfileId(appointment.getStudentProfileId());
            vo.setAppointmentTime(appointment.getAppointmentStartTime());
            
            // 填充学生信息
            StudentProfileVO student = studentProfileService.getStudentProfile(appointment.getStudentProfileId());
            if (student != null) {
                vo.setStudentName(student.getName());
                vo.setStudentNumber(student.getStudentNo());
                vo.setClassName(student.getClassName());
            }
            
            return vo;
        }

        // 转换为VO
        ConsultationAssessmentRespVO vo = BeanUtils.toBean(assessment, ConsultationAssessmentRespVO.class);
        
        // 填充学生信息
        StudentProfileVO student = studentProfileService.getStudentProfile(assessment.getStudentProfileId());
        if (student != null) {
            vo.setStudentName(student.getName());
            vo.setStudentNumber(student.getStudentNo());
            vo.setClassName(student.getClassName());
        }
        
        // 填充评估人信息
        AdminUserRespDTO counselor = adminUserApi.getUser(assessment.getCounselorUserId());
        if (counselor != null) {
            vo.setCounselorName(counselor.getNickname());
        }
        
        vo.setAppointmentTime(appointment.getAppointmentStartTime());
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAssessment(ConsultationAssessmentSaveReqVO saveReqVO) {
        // 验证预约是否存在
        ConsultationAppointmentDO appointment = validateAppointmentExists(saveReqVO.getAppointmentId());
        
        // 验证预约状态（必须是已完成状态）
        if (appointment.getStatus() != 2) {
            throw ServiceExceptionUtil.exception(CONSULTATION_STATUS_ERROR);
        }

        // 检查是否已存在非草稿评估
        ConsultationAssessmentDO existingAssessment = assessmentMapper.selectByAppointmentId(saveReqVO.getAppointmentId());
        if (existingAssessment != null && !existingAssessment.getDraft()) {
            throw ServiceExceptionUtil.exception(CONSULTATION_ASSESSMENT_ALREADY_EXISTS);
        }

        // 保存或更新评估
        ConsultationAssessmentDO assessment;
        if (existingAssessment != null) {
            // 更新现有草稿
            assessment = BeanUtils.toBean(saveReqVO, ConsultationAssessmentDO.class);
            assessment.setId(existingAssessment.getId());
            assessment.setDraft(false);
            assessment.setSubmittedAt(LocalDateTime.now());
            assessmentMapper.updateById(assessment);
        } else {
            // 创建新评估
            assessment = BeanUtils.toBean(saveReqVO, ConsultationAssessmentDO.class);
            assessment.setStudentProfileId(appointment.getStudentProfileId());
            assessment.setCounselorUserId(SecurityFrameworkUtils.getLoginUserId());
            assessment.setDraft(false);
            assessment.setSubmittedAt(LocalDateTime.now());
            assessmentMapper.insert(assessment);
        }

        // 更新预约状态为已闭环
        appointment.setStatus(3);
        appointmentMapper.updateById(appointment);

        return assessment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(ConsultationAssessmentSaveReqVO saveReqVO) {
        // 验证预约是否存在
        ConsultationAppointmentDO appointment = validateAppointmentExists(saveReqVO.getAppointmentId());

        // 检查是否已存在评估
        ConsultationAssessmentDO existingAssessment = assessmentMapper.selectByAppointmentId(saveReqVO.getAppointmentId());
        
        ConsultationAssessmentDO assessment;
        if (existingAssessment != null) {
            // 如果已经提交了正式评估，不能再保存草稿
            if (!existingAssessment.getDraft()) {
                throw ServiceExceptionUtil.exception(CONSULTATION_ASSESSMENT_ALREADY_EXISTS);
            }
            // 更新草稿
            assessment = BeanUtils.toBean(saveReqVO, ConsultationAssessmentDO.class);
            assessment.setId(existingAssessment.getId());
            assessment.setDraft(true);
            assessmentMapper.updateById(assessment);
        } else {
            // 创建新草稿
            assessment = BeanUtils.toBean(saveReqVO, ConsultationAssessmentDO.class);
            assessment.setStudentProfileId(appointment.getStudentProfileId());
            assessment.setCounselorUserId(SecurityFrameworkUtils.getLoginUserId());
            assessment.setDraft(true);
            assessmentMapper.insert(assessment);
        }

        return assessment.getId();
    }

    @Override
    public Integer getAssessmentOverdueTime() {
        // TODO: 从系统配置中读取
        return DEFAULT_OVERDUE_HOURS;
    }

    @Override
    public void setAssessmentOverdueTime(Integer hours) {
        // TODO: 保存到系统配置
        log.info("设置评估报告逾期时间为：{} 小时", hours);
    }

    private ConsultationAppointmentDO validateAppointmentExists(Long appointmentId) {
        ConsultationAppointmentDO appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw ServiceExceptionUtil.exception(CONSULTATION_APPOINTMENT_NOT_EXISTS);
        }
        return appointment;
    }
}