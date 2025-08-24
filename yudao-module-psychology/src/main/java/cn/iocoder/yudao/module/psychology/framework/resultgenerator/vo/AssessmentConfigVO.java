package cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

/**
 * 测评配置VO
 *
 * @author 芋道源码
 */
@Data
public class AssessmentConfigVO {

    /**
     * 测评ID
     */
    private Long assessmentId;

    /**
     * 权重配置
     */
    private WeightConfigVO weightConfig;

    /**
     * 综合风险等级规则
     */
    private CombinedRiskLevelRulesVO combinedRiskLevelRules;

    /**
     * 综合报告模板
     */
    private CombinedReportTemplateVO combinedReportTemplate;

    /**
     * 干预建议配置
     */
    private InterventionConfigVO interventionConfig;

}