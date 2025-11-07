package com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 维度结果 DO
 *
 * @author MinGoo
 */
@TableName(value = "lvye_dimension_result", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class DimensionResultDO extends TenantBaseDO {

    /**
     * 结果ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 问卷结果ID
     */
    private Long questionnaireResultId;

    /**
     * 维度ID
     */
    private Long dimensionId;

    /**
     * 维度编码
     */
    private String dimensionCode;

    /**
     * 分数
     */
    private BigDecimal score;

    /**
     * 是否异常（0：正常，1：异常）
     */
    private Integer isAbnormal;

    /**
     * 风险等级（1-无/低风险，2-轻度风险，3-中度风险，4-重度风险）
     */
    @TableField("risk_level")
    private Integer riskLevel;

    /**
     * 等级
     */
    private String level;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 学生评语（从配置中随机选择的单条评语）
     */
    private String studentComment;

    /**
     * 描述（扩展数据，JSON格式）
     */
    private String description;

}
