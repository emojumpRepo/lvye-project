package com.lvye.mindtrip.module.psychology.dal.dataobject.consultation;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 心理咨询预约 DO
 * 
 * @author 芋道源码
 */
@TableName(value = "lvye_consultation_appointment", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationAppointmentDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 学生档案编号
     */
    private Long studentProfileId;

    /**
     * 主责咨询师（心理老师）管理员编号
     */
    private Long counselorUserId;

    /**
     * 预约咨询的开始时间
     */
    private LocalDateTime appointmentStartTime;

    /**
     * 预约咨询的结束时间
     */
    private LocalDateTime appointmentEndTime;

    /**
     * 咨询时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 咨询类型（字典：consultation_type）
     * 初次咨询、复诊咨询、紧急咨询、家长咨询
     */
    private String consultationType;

    /**
     * 咨询地点
     */
    private String location;

    /**
     * 预约时的备注信息
     */
    private String notes;

    /**
     * 状态（字典：appointment_status）
     * 1-已预约、2-已完成、3-已闭环、4-已取消
     */
    private Integer status;

    /**
     * 取消原因
     */
    private String cancellationReason;

    /**
     * 实际咨询时间（用于补录）
     */
    private LocalDateTime actualTime;

    /**
     * 是否逾期
     */
    private Boolean overdue;

    /**
     * 是否通知学生
     */
    private Boolean notifyStudent;

    /**
     * 是否提醒自己
     */
    private Boolean remindSelf;

    /**
     * 提前提醒时间（分钟）
     */
    private Integer remindTime;

    /**
     * 当前进度
     */
    private Integer currentStep;

    /**
     * 咨询纪要
     */
    private String summary;

    /**
     * 附件ID列表
     */
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private java.util.List<Long> attachmentIds;
}