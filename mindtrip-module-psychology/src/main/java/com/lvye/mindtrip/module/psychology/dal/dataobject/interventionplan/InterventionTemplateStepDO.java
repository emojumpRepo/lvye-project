package com.lvye.mindtrip.module.psychology.dal.dataobject.interventionplan;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 干预模板步骤 DO
 *
 * @author 芋道源码
 */
@TableName(value = "lvye_intervention_template_step", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterventionTemplateStepDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 步骤标题
     */
    private String title;

}
