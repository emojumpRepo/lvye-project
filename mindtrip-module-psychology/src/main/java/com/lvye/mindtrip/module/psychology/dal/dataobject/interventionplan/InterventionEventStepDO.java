package com.lvye.mindtrip.module.psychology.dal.dataobject.interventionplan;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 干预事件步骤 DO
 *
 * @author 芋道源码
 */
@TableName(value = "lvye_intervention_event_step", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterventionEventStepDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 干预ID
     */
    private Long interventionId;

    /**
     * 干预模板ID
     */
    private Long templateId;

    /**
     * 步骤标题
     */
    private String title;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 步骤状态
     */
    private Integer status;

    /**
     * 教师笔记
     */
    private String notes;

    /**
     * 附件ID列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> attachmentIds;

}
