package cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 测评结果VO
 *
 * @author 芋道源码
 */
@Data
@Builder
public class AssessmentResultVO {

    /**
     * 测评ID
     */
    private Long assessmentId;

    /**
     * 综合得分
     */
    private BigDecimal combinedScore;

    /**
     * 综合风险等级
     */
    private Integer combinedRiskLevel;

    /**
     * 风险因素列表
     */
    private List<RiskFactorVO> riskFactors;

    /**
     * 干预建议列表
     */
    private List<InterventionSuggestionVO> interventionSuggestions;

    /**
     * 综合报告内容
     */
    private String comprehensiveReport;

    /**
     * 关联的问卷结果
     */
    private List<QuestionnaireResultVO> questionnaireResults;

}