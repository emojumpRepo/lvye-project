package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import com.lvye.mindtrip.framework.mybatis.core.dataobject.BaseDO;
import com.lvye.mindtrip.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测评结果配置 DO
 *
 * @author MinGoo
 */
@TableName(value = "lvye_assessment_result_config", autoResultMap = true)
@TenantIgnore
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentResultConfigDO extends BaseDO {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 场景ID
     */
    private Long scenarioId;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 规则类型：0-等级方面规则，1-评语方面规则
     */
    private Integer ruleType;

    /**
     * 计算公式（JSON规则）
     */
    private String calculateFormula;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 评价等级（可选，字典或自定义文本）
     */
    private String level;

    /**
     * 建议文本
     */
    private String suggestions;

    /**
     * 评语文本
     */
    private String comment;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

}