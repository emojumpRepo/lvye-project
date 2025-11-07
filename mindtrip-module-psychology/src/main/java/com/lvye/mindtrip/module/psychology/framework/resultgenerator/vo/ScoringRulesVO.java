package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

import java.util.Map;

/**
 * 评分规则VO
 *
 * @author 芋道源码
 */
@Data
public class ScoringRulesVO {

    /**
     * 评分算法类型
     */
    private String algorithmType;

    /**
     * 题目权重配置
     */
    private Map<String, Double> questionWeights;

    /**
     * 维度权重配置
     */
    private Map<String, Double> dimensionWeights;

    /**
     * 标准分转换参数
     */
    private StandardScoreParamsVO standardScoreParams;

    /**
     * 百分位转换参数
     */
    private PercentileParamsVO percentileParams;

}