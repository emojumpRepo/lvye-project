package cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo;

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
     * 结果报告内容
     */
    private String reportContent;

    /**
     * 建议内容
     */
    private String suggestions;

}