package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 问卷结果VO
 *
 * @author 芋道源码
 */
@Data
@Builder
public class QuestionnaireResultVO {

    /**
     * 问卷ID
     */
    private Long questionnaireId;

    /**
     * 原始得分
     */
    private BigDecimal rawScore;

    /**
     * 标准分
     */
    private BigDecimal standardScore;

    /**
     * 百分位排名
     */
    private BigDecimal percentileRank;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 等级描述
     */
    private String levelDescription;

    /**
     * 各维度得分
     */
    private Map<String, BigDecimal> dimensionScores;

    /**
     * 各维度异常状态
     * key: 维度名称
     * value: 是否异常 (true=异常, false=正常)
     */
    private Map<String, Boolean> dimensionAbnormalStatus;

    /**
     * 结果报告内容
     */
    private String reportContent;

    /**
     * 建议内容
     */
    private String suggestions;

    /**
     * 问卷整体是否异常：0-正常，1-异常
     * 某些问卷（如PHCSS）会根据维度异常数量综合判定
     */
    private Integer isAbnormal;

}