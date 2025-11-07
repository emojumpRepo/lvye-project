package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import com.lvye.mindtrip.framework.mybatis.core.dataobject.BaseDO;
import com.lvye.mindtrip.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模块结果计算配置 DO
 */
@TableName(value = "lvye_module_result_config", autoResultMap = true)
@TenantIgnore  // 模块结果配置为全局配置表，不进行租户隔离
@Data
@EqualsAndHashCode(callSuper = true)
public class ModuleResultConfigDO extends BaseDO {

    @TableId
    private Long id;

    private Long scenarioSlotId;

    private String configName;

    /**
     * 规则类型：0-等级方面规则，1-评语方面规则
     */
    private Integer ruleType;

    private String calculateFormula;

    private String description;

    private String level;

    private String suggestions;

    private String comments; // JSON 字符串数组

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;
}
