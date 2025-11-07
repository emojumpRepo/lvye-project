package com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire;

import com.lvye.mindtrip.framework.mybatis.core.dataobject.BaseDO;
import com.lvye.mindtrip.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷结果配置 DO
 *
 * @author MinGoo
 */
@TableName(value = "lvye_questionnaire_result_config", autoResultMap = true)
@TenantIgnore  // 问卷结果配置表为全局配置表，不进行租户隔离
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireResultConfigDO extends BaseDO {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 维度ID（新架构）
     */
    private Long dimensionId;

    /**
     * 题目索引
     */
    private String questionIndex;

    /**
     * 计算类型
     */
    private Integer calculateType;

    /**
     * 计算公式
     */
    private String calculateFormula;

    /**
     * 规则匹配排序（升序）
     */
    private Integer matchOrder;

    /**
     * 教师端评语
     */
    private String teacherComment;

    /**
     * 学生端评语
     */
    private String studentComment;

    /**
     * 是否异常
     */
    private Integer isAbnormal;

    /**
     * 风险等级（1-无/低风险，2-轻度风险，3-中度风险，4-重度风险）
     */
    private Integer riskLevel;

    /**
     * 等级：优秀、良好、一般、较差、很差
     */
    private String level;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;

    /**
     * 是否可多命中（0：否，1：是）
     */
    private Integer isMultiHit;

}
