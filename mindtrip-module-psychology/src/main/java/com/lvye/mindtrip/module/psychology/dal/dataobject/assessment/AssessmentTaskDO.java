package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.util.Date;
import java.util.List;

/**
 * 测评任务 DO
 */
@TableName(value = "lvye_assessment_task", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentTaskDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 任务编号（唯一）
     */
    private String taskNo;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 关联问卷 ID 列表（非持久化字段）
     */
    @TableField(exist = false)
    private List<Long> questionnaireIds;

    /**
     *目标对象（字典：target_audience）
     */
    private Integer targetAudience;

    /**
     * 状态（枚举：AssessmentTaskStatusEnum）
     */
    private Integer status;

    /**
     * 发布人管理员编号
     */
    private Long publishUserId;

    /**
     * 开始时间
     */
    private Date startline;

    /**
     * 截止时间
     */
    private Date deadline;

    /**
     * 场景ID
     */
    private Long scenarioId;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 危机事件ID
     */
    private Long eventId;

    /**
     * 发布人管理员
     */
    @TableField(exist = false)
    private String publishUser;

    /**
     * 完成人数
     */
    @TableField(exist = false)
    private Long finishNum;

    /**
     * 总人数
     */
    @TableField(exist = false)
    private Long totalNum;

    /**
     * 场景编号(非持久化字段)
     */
    @TableField(exist = false)
    private String scenarioCode;

    /**
     * 场景名称(非持久化字段)
     */
    @TableField(exist = false)
    private String scenarioName;

}


