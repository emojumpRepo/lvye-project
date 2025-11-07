package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 干预建议VO
 *
 * @author 芋道源码
 */
@Data
@Builder
public class InterventionSuggestionVO {

    /**
     * 建议类型
     */
    private String suggestionType;

    /**
     * 建议标题
     */
    private String title;

    /**
     * 建议内容
     */
    private String content;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 针对的风险因素
     */
    private String targetRiskFactor;

    /**
     * 建议的执行时间
     */
    private String timeframe;

}