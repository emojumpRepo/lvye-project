package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

/**
 * 综合报告模板VO
 *
 * @author 芋道源码
 */
@Data
public class CombinedReportTemplateVO {

    /**
     * 报告标题模板
     */
    private String titleTemplate;

    /**
     * 综合分析模板
     */
    private String analysisTemplate;

    /**
     * 风险因素总结模板
     */
    private String riskSummaryTemplate;

    /**
     * 干预建议模板
     */
    private String interventionTemplate;

}