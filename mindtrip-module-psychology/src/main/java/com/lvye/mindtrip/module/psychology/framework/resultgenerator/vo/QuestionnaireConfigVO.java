package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

/**
 * 问卷配置VO
 *
 * @author 芋道源码
 */
@Data
public class QuestionnaireConfigVO {

    /**
     * 问卷ID
     */
    private Long questionnaireId;

    /**
     * 评分规则
     */
    private ScoringRulesVO scoringRules;

    /**
     * 风险等级规则
     */
    private RiskLevelRulesVO riskLevelRules;

    /**
     * 报告模板
     */
    private ReportTemplateVO reportTemplate;

    /**
     * 维度配置
     */
    private DimensionConfigVO dimensionConfig;

}