package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 测评答题记录 DO（题目级别）
 */
@TableName(value = "lvye_assessment_answer", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentAnswerDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 任务参与者编号 */
    private Long participantId;

    /** 题目序号（从1开始） */
    private Integer questionIndex;

    /** 选项编码/答案内容（固定问卷编码或简答内容） */
    private String answer;

    /** 题目得分（若适用） */
    private Integer score;
}



