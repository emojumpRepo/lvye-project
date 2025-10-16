package cn.iocoder.yudao.module.psychology.service.consultation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAppointmentDO;

import jakarta.validation.Valid;

/**
 * 咨询预约 Service 接口
 *
 * @author 芋道源码
 */
public interface ConsultationAppointmentService {

    /**
     * 创建咨询预约
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAppointment(@Valid ConsultationAppointmentCreateReqVO createReqVO);

    /**
     * 更新咨询预约
     *
     * @param updateReqVO 更新信息
     */
    void updateAppointment(@Valid ConsultationAppointmentUpdateReqVO updateReqVO);

    /**
     * 删除咨询预约
     *
     * @param id 编号
     */
    void deleteAppointment(Long id);

    /**
     * 获得咨询预约
     *
     * @param id 编号
     * @return 咨询预约
     */
    ConsultationAppointmentRespVO getAppointment(Long id);

    /**
     * 获得咨询预约分页
     *
     * @param pageReqVO 分页查询
     * @return 咨询预约分页
     */
    PageResult<ConsultationAppointmentRespVO> getAppointmentPage(ConsultationAppointmentPageReqVO pageReqVO);

    /**
     * 获取今日咨询列表和统计
     *
     * @return 今日咨询数据
     */
    TodayConsultationRespVO getTodayConsultations();

    /**
     * 完成咨询
     *
     * @param id 预约ID
     * @param fillAssessmentNow 是否立即填写评估
     */
    void completeAppointment(Long id, Boolean fillAssessmentNow);

    /**
     * 调整预约时间
     *
     * @param id 预约ID
     * @param adjustReqVO 调整信息
     */
    void adjustAppointmentTime(Long id, ConsultationAppointmentAdjustTimeReqVO adjustReqVO);

    /**
     * 取消预约
     *
     * @param id 预约ID
     * @param cancelReqVO 取消信息
     */
    void cancelAppointment(Long id, ConsultationAppointmentCancelReqVO cancelReqVO);

    /**
     * 补录咨询记录
     *
     * @param id 预约ID
     * @param supplementReqVO 补录信息
     */
    void supplementAppointment(Long id, ConsultationAppointmentSupplementReqVO supplementReqVO);

    /**
     * 发送催办提醒
     *
     * @param id 预约ID
     */
    void sendReminder(Long id);
}