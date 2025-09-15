package cn.iocoder.yudao.module.psychology.service.consultation;

import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.assessment.ConsultationAssessmentRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.assessment.ConsultationAssessmentSaveReqVO;

import jakarta.validation.Valid;

/**
 * 咨询评估 Service 接口
 *
 * @author 芋道源码
 */
public interface ConsultationAssessmentService {

    /**
     * 获取咨询评估信息
     *
     * @param appointmentId 咨询预约ID
     * @return 评估信息
     */
    ConsultationAssessmentRespVO getAssessmentByAppointmentId(Long appointmentId);

    /**
     * 保存评估报告
     *
     * @param saveReqVO 评估信息
     * @return 评估ID
     */
    Long saveAssessment(@Valid ConsultationAssessmentSaveReqVO saveReqVO);

    /**
     * 保存评估草稿
     *
     * @param saveReqVO 评估信息
     * @return 评估ID
     */
    Long saveDraft(@Valid ConsultationAssessmentSaveReqVO saveReqVO);

    /**
     * 获取评估报告逾期时间阈值
     *
     * @return 逾期时间（小时）
     */
    Integer getAssessmentOverdueTime();

    /**
     * 设置评估报告逾期时间阈值
     *
     * @param hours 逾期时间（小时）
     */
    void setAssessmentOverdueTime(Integer hours);
}