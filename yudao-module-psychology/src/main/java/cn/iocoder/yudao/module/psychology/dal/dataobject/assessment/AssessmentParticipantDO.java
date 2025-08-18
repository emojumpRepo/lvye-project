package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 测评任务参与者 DO
 */
@TableName(value = "lvye_assessment_participant", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentParticipantDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 任务编号 */
    private Long taskId;

    /** 学生档案编号 */
    private Long studentProfileId;

    /** 家长参与标识：true 家长；false 学生 */
    private Boolean isParent;

    /** 完成状态（枚举：ParticipantCompletionStatusEnum） */
    private Integer completionStatus;

    /** 开始作答时间 */
    private LocalDateTime startTime;

    /** 提交时间 */
    private LocalDateTime submitTime;
}



