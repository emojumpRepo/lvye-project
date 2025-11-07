package com.lvye.mindtrip.module.psychology.dal.dataobject.consultation;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 危机干预事件 DO
 */
@TableName(value = "lvye_crisis_intervention", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrisisInterventionDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 事件编号 */
    private String eventId;

    /** 学生档案编号 */
    private Long studentProfileId;

    /** 事件标题 */
    private String title;

    /** 事件描述 */
    private String description;

    /** 风险等级（字典：risk_level） */
    private Integer riskLevel;

    /** 状态（字典：intervention_status） 
     * 1-已上报、2-已分配、3-处理中、4-已结案、5-持续关注
     */
    private Integer status;

    /** 处理人管理员编号 */
    private Long handlerUserId;

    /** 来源类型（枚举：CrisisSourceTypeEnum） */
    private Integer sourceType;

    /** 上报人管理员编号（任课老师/系统等），当来源为 QUICK_REPORT 时必填 */
    private Long reporterUserId;

    /** 上报时间 */
    private java.time.LocalDateTime reportedAt;
    
    /** 优先级（字典：priority_level）
     * 1-高、2-中、3-低
     */
    private Integer priority;
    
    /** 事发地点 */
    private String location;
    
    /** 处理方式（字典：process_method）
     * 1-心理访谈、2-量表评估、3-持续关注、4-直接解决
     */
    private Integer processMethod;
    
    /** 处理原因说明 */
    private String processReason;
    
    /** 结案总结 */
    private String closureSummary;
    
    /** 处理进度百分比 */
    private Integer progress;

    /** 处理状态 */
    private Integer processStatus;

    /** 是否自动分配 */
    private Boolean autoAssigned;

    /** 是否关闭 */
    private Boolean closed;

    /** 处理时间 */
    private java.time.LocalDateTime handleAt;
}



