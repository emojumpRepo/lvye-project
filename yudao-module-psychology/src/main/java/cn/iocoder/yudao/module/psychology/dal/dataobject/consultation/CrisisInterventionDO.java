package cn.iocoder.yudao.module.psychology.dal.dataobject.consultation;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
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

    /** 学生档案编号 */
    private Long studentProfileId;

    /** 事件标题 */
    private String title;

    /** 事件描述 */
    private String description;

    /** 风险等级（字典：risk_level） */
    private Integer riskLevel;

    /** 状态（字典：intervention_status） */
    private Integer status;

    /** 处理人管理员编号 */
    private Long handlerUserId;

    /** 来源类型（枚举：CrisisSourceTypeEnum） */
    private Integer sourceType;

    /** 上报人管理员编号（任课老师/系统等），当来源为 QUICK_REPORT 时必填 */
    private Long reporterUserId;

    /** 上报时间 */
    private java.time.LocalDateTime reportedAt;

    /** 紧急程度（快速上报特有），与 riskLevel 区分：前者为主观紧急，后者为评估风险 */
    private Integer urgencyLevel;
}



