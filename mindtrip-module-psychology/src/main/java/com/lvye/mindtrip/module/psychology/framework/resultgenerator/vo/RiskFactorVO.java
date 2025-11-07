package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 风险因素VO
 *
 * @author 芋道源码
 */
@Data
@Builder
public class RiskFactorVO {

    /**
     * 风险因素代码
     */
    private String factorCode;

    /**
     * 风险因素名称
     */
    private String factorName;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 风险描述
     */
    private String description;

    /**
     * 来源问卷ID
     */
    private Long sourceQuestionnaireId;

    /**
     * 来源维度
     */
    private String sourceDimension;

}