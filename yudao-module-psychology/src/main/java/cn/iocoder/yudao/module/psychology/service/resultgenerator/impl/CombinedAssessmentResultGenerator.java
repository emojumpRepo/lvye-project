package cn.iocoder.yudao.module.psychology.service.resultgenerator.impl;

import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.enums.RiskLevelEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorStrategy;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组合测评结果生成器
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class CombinedAssessmentResultGenerator implements ResultGeneratorStrategy {

    @Override
    public ResultGeneratorTypeEnum getGeneratorType() {
        return ResultGeneratorTypeEnum.COMBINED_ASSESSMENT;
    }

    @Override
    public boolean supports(Long assessmentId, ResultGeneratorTypeEnum type) {
        return ResultGeneratorTypeEnum.COMBINED_ASSESSMENT.equals(type) 
               && isAssessmentSupported(assessmentId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T generateResult(ResultGenerationContext context) {
        log.info("开始生成组合测评结果，测评ID: {}", context.getAssessmentId());
        
        // 1. 获取所有关联的问卷结果
        List<QuestionnaireResultVO> questionnaireResults = context.getQuestionnaireResults();
        
        // 2. 获取组合测评配置
        AssessmentConfigVO config = getAssessmentConfig(context.getAssessmentId());
        
        // 3. 计算加权综合得分
        BigDecimal combinedScore = calculateCombinedScore(questionnaireResults, config.getWeightConfig());
        
        // 4. 确定综合风险等级
        RiskLevelEnum combinedRiskLevel = determineCombinedRiskLevel(questionnaireResults, config);
        
        // 5. 分析风险因素
        List<RiskFactorVO> riskFactors = analyzeRiskFactors(questionnaireResults, config);
        
        // 6. 生成干预建议
        List<InterventionSuggestionVO> interventionSuggestions = 
                generateInterventionSuggestions(combinedRiskLevel, riskFactors, config);
        
        // 7. 生成综合报告
        String comprehensiveReport = generateComprehensiveReport(
                questionnaireResults, combinedScore, combinedRiskLevel, riskFactors, config);
        
        AssessmentResultVO result = AssessmentResultVO.builder()
                .assessmentId(context.getAssessmentId())
                .combinedScore(combinedScore)
                .combinedRiskLevel(combinedRiskLevel.getLevel())
                .riskFactors(riskFactors)
                .interventionSuggestions(interventionSuggestions)
                .comprehensiveReport(comprehensiveReport)
                .questionnaireResults(questionnaireResults)
                .build();
        
        log.info("组合测评结果生成完成，综合风险等级: {}", combinedRiskLevel.getName());
        return (T) result;
    }

    @Override
    public void validateGenerationParams(ResultGenerationContext context) {
        if (context.getAssessmentId() == null) {
            throw new IllegalArgumentException("测评ID不能为空");
        }
        if (context.getQuestionnaireResults() == null || context.getQuestionnaireResults().isEmpty()) {
            throw new IllegalArgumentException("问卷结果数据不能为空");
        }
        if (context.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
    }

    /**
     * 检查测评是否支持结果生成
     */
    private boolean isAssessmentSupported(Long assessmentId) {
        // TODO: 实现测评支持性检查逻辑
        return assessmentId != null && assessmentId > 0;
    }

    /**
     * 获取测评配置
     */
    private AssessmentConfigVO getAssessmentConfig(Long assessmentId) {
        // TODO: 从数据库或缓存中获取测评配置
        // 这里返回一个模拟的配置
        AssessmentConfigVO config = new AssessmentConfigVO();
        config.setAssessmentId(assessmentId);
        
        // 设置默认的权重配置
        WeightConfigVO weightConfig = new WeightConfigVO();
        Map<Long, BigDecimal> questionnaireWeights = new HashMap<>();
        questionnaireWeights.put(1L, new BigDecimal("0.4"));
        questionnaireWeights.put(2L, new BigDecimal("0.6"));
        weightConfig.setQuestionnaireWeights(questionnaireWeights);
        config.setWeightConfig(weightConfig);
        
        // 设置综合风险等级规则
        CombinedRiskLevelRulesVO riskRules = new CombinedRiskLevelRulesVO();
        riskRules.setAlgorithmType("MAX_RISK");
        config.setCombinedRiskLevelRules(riskRules);
        
        return config;
    }

    /**
     * 计算加权综合得分
     */
    private BigDecimal calculateCombinedScore(List<QuestionnaireResultVO> results, 
                                            WeightConfigVO weightConfig) {
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (QuestionnaireResultVO result : results) {
            BigDecimal weight = weightConfig.getWeight(result.getQuestionnaireId());
            if (result.getStandardScore() != null) {
                totalScore = totalScore.add(result.getStandardScore().multiply(weight));
                totalWeight = totalWeight.add(weight);
            }
        }
        
        return totalWeight.compareTo(BigDecimal.ZERO) > 0 ? 
               totalScore.divide(totalWeight, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * 确定综合风险等级
     */
    private RiskLevelEnum determineCombinedRiskLevel(List<QuestionnaireResultVO> results, 
                                                   AssessmentConfigVO config) {
        String algorithmType = config.getCombinedRiskLevelRules().getAlgorithmType();
        
        switch (algorithmType) {
            case "MAX_RISK":
                // 采用最高风险等级
                return results.stream()
                        .map(r -> RiskLevelEnum.fromLevel(r.getRiskLevel()))
                        .filter(Objects::nonNull)
                        .max(Comparator.comparing(RiskLevelEnum::getLevel))
                        .orElse(RiskLevelEnum.NORMAL);
                        
            case "WEIGHTED_AVERAGE":
                // 采用加权平均
                return calculateWeightedAverageRiskLevel(results, config.getWeightConfig());
                
            default:
                return RiskLevelEnum.NORMAL;
        }
    }

    /**
     * 计算加权平均风险等级
     */
    private RiskLevelEnum calculateWeightedAverageRiskLevel(List<QuestionnaireResultVO> results, 
                                                          WeightConfigVO weightConfig) {
        BigDecimal totalWeightedRisk = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (QuestionnaireResultVO result : results) {
            BigDecimal weight = weightConfig.getWeight(result.getQuestionnaireId());
            BigDecimal riskValue = new BigDecimal(result.getRiskLevel());
            totalWeightedRisk = totalWeightedRisk.add(riskValue.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }
        
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal averageRisk = totalWeightedRisk.divide(totalWeight, 0, RoundingMode.HALF_UP);
            return RiskLevelEnum.fromLevel(averageRisk.intValue());
        }
        
        return RiskLevelEnum.NORMAL;
    }

    /**
     * 分析风险因素
     */
    private List<RiskFactorVO> analyzeRiskFactors(List<QuestionnaireResultVO> results, 
                                                AssessmentConfigVO config) {
        List<RiskFactorVO> riskFactors = new ArrayList<>();
        
        for (QuestionnaireResultVO result : results) {
            if (result.getRiskLevel() >= 3) { // 预警及以上等级
                RiskFactorVO riskFactor = RiskFactorVO.builder()
                        .factorCode("QUESTIONNAIRE_RISK_" + result.getQuestionnaireId())
                        .factorName("问卷" + result.getQuestionnaireId() + "风险因素")
                        .riskLevel(result.getRiskLevel())
                        .description("该问卷显示存在" + RiskLevelEnum.fromLevel(result.getRiskLevel()).getName() + "风险")
                        .sourceQuestionnaireId(result.getQuestionnaireId())
                        .build();
                riskFactors.add(riskFactor);
            }
            
            // 分析维度风险
            if (result.getDimensionScores() != null) {
                for (Map.Entry<String, BigDecimal> entry : result.getDimensionScores().entrySet()) {
                    if (entry.getValue().compareTo(new BigDecimal("40")) < 0) { // 维度得分过低
                        RiskFactorVO dimensionRisk = RiskFactorVO.builder()
                                .factorCode("DIMENSION_RISK_" + entry.getKey())
                                .factorName(entry.getKey() + "维度风险")
                                .riskLevel(3)
                                .description(entry.getKey() + "维度得分偏低，需要关注")
                                .sourceQuestionnaireId(result.getQuestionnaireId())
                                .sourceDimension(entry.getKey())
                                .build();
                        riskFactors.add(dimensionRisk);
                    }
                }
            }
        }
        
        return riskFactors;
    }

    /**
     * 生成干预建议
     */
    private List<InterventionSuggestionVO> generateInterventionSuggestions(RiskLevelEnum combinedRiskLevel, 
                                                                          List<RiskFactorVO> riskFactors, 
                                                                          AssessmentConfigVO config) {
        List<InterventionSuggestionVO> suggestions = new ArrayList<>();
        
        // 根据综合风险等级生成基础建议
        switch (combinedRiskLevel) {
            case NORMAL:
                suggestions.add(InterventionSuggestionVO.builder()
                        .suggestionType("MAINTENANCE")
                        .title("保持良好状态")
                        .content("您的心理状态良好，请继续保持健康的生活方式。")
                        .priority(1)
                        .timeframe("持续")
                        .build());
                break;
                
            case ATTENTION:
                suggestions.add(InterventionSuggestionVO.builder()
                        .suggestionType("PREVENTION")
                        .title("预防性干预")
                        .content("建议适当关注心理健康，可以通过运动、阅读等方式调节。")
                        .priority(2)
                        .timeframe("1-2周")
                        .build());
                break;
                
            case WARNING:
                suggestions.add(InterventionSuggestionVO.builder()
                        .suggestionType("INTERVENTION")
                        .title("及时干预")
                        .content("建议及时寻求专业心理咨询师的帮助。")
                        .priority(3)
                        .timeframe("立即")
                        .build());
                break;
                
            case HIGH_RISK:
                suggestions.add(InterventionSuggestionVO.builder()
                        .suggestionType("CRISIS_INTERVENTION")
                        .title("危机干预")
                        .content("强烈建议立即寻求专业心理医生的帮助。")
                        .priority(4)
                        .timeframe("立即")
                        .build());
                break;
        }
        
        // 根据具体风险因素生成针对性建议
        for (RiskFactorVO riskFactor : riskFactors) {
            if (riskFactor.getSourceDimension() != null) {
                InterventionSuggestionVO suggestion = InterventionSuggestionVO.builder()
                        .suggestionType("TARGETED")
                        .title("针对" + riskFactor.getSourceDimension() + "的建议")
                        .content("针对" + riskFactor.getSourceDimension() + "维度的专项改善建议")
                        .priority(2)
                        .targetRiskFactor(riskFactor.getFactorCode())
                        .timeframe("2-4周")
                        .build();
                suggestions.add(suggestion);
            }
        }
        
        return suggestions.stream()
                .sorted(Comparator.comparing(InterventionSuggestionVO::getPriority).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 生成综合报告
     */
    private String generateComprehensiveReport(List<QuestionnaireResultVO> questionnaireResults, 
                                             BigDecimal combinedScore, 
                                             RiskLevelEnum combinedRiskLevel, 
                                             List<RiskFactorVO> riskFactors, 
                                             AssessmentConfigVO config) {
        StringBuilder report = new StringBuilder();
        
        report.append("综合测评结果报告\n");
        report.append("===================\n\n");
        
        report.append("综合得分: ").append(combinedScore).append("\n");
        report.append("综合风险等级: ").append(combinedRiskLevel.getName()).append("\n\n");
        
        report.append("各问卷结果汇总:\n");
        for (QuestionnaireResultVO result : questionnaireResults) {
            report.append("- 问卷").append(result.getQuestionnaireId())
                  .append(": 标准分 ").append(result.getStandardScore())
                  .append(", 风险等级 ").append(RiskLevelEnum.fromLevel(result.getRiskLevel()).getName())
                  .append("\n");
        }
        
        if (!riskFactors.isEmpty()) {
            report.append("\n识别的风险因素:\n");
            for (RiskFactorVO riskFactor : riskFactors) {
                report.append("- ").append(riskFactor.getFactorName())
                      .append(": ").append(riskFactor.getDescription()).append("\n");
            }
        }
        
        return report.toString();
    }

}