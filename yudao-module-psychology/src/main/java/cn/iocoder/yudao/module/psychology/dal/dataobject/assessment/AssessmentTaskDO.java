package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 测评任务 DO
 */
@TableName(value = "psy_assessment_task", autoResultMap = true)
@KeySequence("psy_assessment_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentTaskDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 任务编号（唯一） */
    private String taskNo;

    /** 任务名称 */
    private String name;

    /** 量表编号，固定问卷 A/B */
    private String scaleCode;

    /** 目标对象（字典：target_audience） */
    private Integer targetAudience;

    /** 状态（枚举：AssessmentTaskStatusEnum） */
    private Integer status;

    /** 发布人管理员编号 */
    private Long publishUserId;

    /** 截止时间 */
    private LocalDateTime deadline;

}



