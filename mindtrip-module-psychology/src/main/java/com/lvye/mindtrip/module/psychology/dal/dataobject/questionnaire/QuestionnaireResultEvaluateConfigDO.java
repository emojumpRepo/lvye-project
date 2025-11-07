package com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果评价参数表
 * @Version: 1.0
 */
@TableName(value = "lvye_questionnaire_result_evaluate_config", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireResultEvaluateConfigDO extends TenantBaseDO {

    /**
     * 问卷ID
     */
    @TableId
    private Long id;

    /**
     * 问卷id
     */
    private Long questionnaireId;

    /**
     * 异常因子数量
     */
    private String abnormalCount;

    /**
     * 评价
     */
    private String evaluate;

    /**
     * 建议
     */
    private String suggestions;

    /**
     * 风险等级
     */
    private Integer riskLevel;


}
