package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

import java.util.Map;

/**
 * 报告模板VO
 *
 * @author 芋道源码
 */
@Data
public class ReportTemplateVO {

    /**
     * 报告标题模板
     */
    private String titleTemplate;

    /**
     * 报告内容模板
     */
    private String contentTemplate;

    /**
     * 建议内容模板
     */
    private String suggestionTemplate;

    /**
     * 风险等级对应的建议模板
     */
    private Map<Integer, String> riskLevelSuggestions;

}