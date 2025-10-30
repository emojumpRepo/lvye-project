package cn.iocoder.yudao.module.psychology.service.consultation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAppointmentDO;

import jakarta.validation.Valid;
import java.util.List;

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
     * 获取咨询预约统计数据
     *
     * @return 统计数据
     */
    ConsultationStatisticsRespVO getStatistics();

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

    /**
     * 校验预约时间是否冲突
     *
     * @param reqVO 时间冲突校验请求
     * @return 冲突校验结果
     */
    ConsultationAppointmentCheckTimeConflictRespVO checkTimeConflict(ConsultationAppointmentCheckTimeConflictReqVO reqVO);

    /**
     * 获取周预约数据
     *
     * @param reqVO 周查询请求
     * @return 周预约数据
     */
    ConsultationAppointmentWeeklyRespVO getWeeklyAppointments(ConsultationAppointmentWeeklyReqVO reqVO);

    /**
     * 获取时间范围内的咨询数据
     *
     * @param reqVO 时间范围查询请求
     * @return 时间范围内的咨询数据
     */
    ConsultationAppointmentTimeRangeRespVO getTimeRangeData(ConsultationAppointmentTimeRangeReqVO reqVO);

    /**
     * 根据日期查询咨询预约数据
     *
     * @param reqVO 日期查询请求
     * @return 咨询预约数据
     */
    ConsultationAppointmentDateQueryRespVO getAppointmentsByDate(ConsultationAppointmentDateQueryReqVO reqVO);

    /**
     * 根据学生档案ID查询咨询记录列表
     *
     * @param studentProfileId 学生档案ID
     * @return 咨询记录列表
     */
    List<ConsultationAppointmentRespVO> getAppointmentsByStudentProfileId(Long studentProfileId);

    /**
     * 保存咨询纪要
     *
     * @param id 预约ID
     * @param reqVO 咨询纪要信息
     */
    void saveSummary(Long id, ConsultationAppointmentSaveSummaryReqVO reqVO);

    /**
     * 获取今日咨询预约分页
     *
     * @param pageReqVO 分页查询
     * @return 今日咨询预约分页
     */
    PageResult<ConsultationAppointmentRespVO> getTodayAppointmentPage(ConsultationAppointmentTodayPageReqVO pageReqVO);
}