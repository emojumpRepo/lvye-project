package com.lvye.mindtrip.module.psychology.dal.dataobject.consultation;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 心理咨询记录 DO
 */
@TableName(value = "lvye_consultation_record", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRecordDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 学生档案编号 */
    private Long studentProfileId;

    /** 咨询师（心理老师）管理员编号 */
    private Long counselorUserId;

    /** 咨询类型（字典：consultation_type） */
    private String type;

    /** 咨询方式（字典：consultation_method） */
    private Integer method;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 时长（分钟） */
    private Integer durationMinutes;

    /** 咨询内容摘要 */
    private String content;

    /** 结论与建议 */
    private String suggestion;

    /** 状态（字典：intervention_status） */
    private Integer status;
}



