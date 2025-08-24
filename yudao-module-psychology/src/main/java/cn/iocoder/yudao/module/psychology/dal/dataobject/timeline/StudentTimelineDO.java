package cn.iocoder.yudao.module.psychology.dal.dataobject.timeline;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 学生综合时间线事件 DO
 */
@TableName(value = "lvye_student_timeline", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTimelineDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 学生档案编号 */
    private Long studentProfileId;

    /** 事件类型（枚举：TimelineEventTypeEnum） */
    private Integer eventType;

    /** 事件摘要 */
    private String title;

    /** 事件详情（JSON 或文本） */
    private String content;

    /** 关联业务编号（如任务、咨询、上报等） */
    private Long bizId;
}



