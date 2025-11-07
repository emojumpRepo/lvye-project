package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测评任务 - 问卷关联 DO
 */
@TableName(value = "lvye_assessment_task_questionnaire", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentTaskQuestionnaireDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 问卷 ID
     */
    private Long questionnaireId;

    /**
     * 槽位标识（可空）
     */
    private String slotKey;

    /**
     * 槽位内顺序（可空）
     */
    private Integer slotOrder;
}


