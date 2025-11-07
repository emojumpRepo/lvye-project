package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-11
 * @Description:测评模块
 * @Version: 1.0
 */
@TableName("lvye_exam_template")
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentTemplateDO extends TenantBaseDO {

    /**
     * 测试模板id
     */
    @TableId
    private Long templateId;

    /**
     * 试题名称
     */
    private String name;

    /**
     * 试题链接
     */
    private String link;

    /**
     * 试题描述
     */
    private String description;

}
