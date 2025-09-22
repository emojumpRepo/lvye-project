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

    private RiskLevelEnum safeRiskLevel(Integer level) {
        if (level == null) {
            return null;
        }
        RiskLevelEnum e = RiskLevelEnum.fromLevel(level);
        return e != null ? e : RiskLevelEnum.NORMAL;
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
        // 检查是否包含PHCSS问卷（ID=12），如果有则应用PHCSS规则
        Optional<QuestionnaireResultVO> phcssResult = results.stream()
                .filter(r -> Long.valueOf(12L).equals(r.getQuestionnaireId()))
                .findFirst();

        if (phcssResult.isPresent()) {
            return applyPhcssCountRule(phcssResult.get());
        }

        // 兜底：使用原有逻辑
        String algorithmType = config.getCombinedRiskLevelRules().getAlgorithmType();

        switch (algorithmType) {
            case "MAX_RISK":
                // 采用最高风险等级（兼容空值）
                return results.stream()
                        .map(r -> safeRiskLevel(r.getRiskLevel()))
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
            if (result.getRiskLevel() == null) {
                continue;
            }
            BigDecimal weight = weightConfig.getWeight(result.getQuestionnaireId());
            if (weight == null) {
                continue;
            }
            BigDecimal riskValue = new BigDecimal(result.getRiskLevel());
            totalWeightedRisk = totalWeightedRisk.add(riskValue.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal averageRisk = totalWeightedRisk.divide(totalWeight, 0, RoundingMode.HALF_UP);
            return safeRiskLevel(averageRisk.intValue());
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
            Integer rLevel = result.getRiskLevel();
            if (rLevel != null && rLevel >= 3) { // 预警及以上等级
                RiskLevelEnum rle = safeRiskLevel(rLevel);
                RiskFactorVO riskFactor = RiskFactorVO.builder()
                        .factorCode("QUESTIONNAIRE_RISK_" + result.getQuestionnaireId())
                        .factorName("问卷" + result.getQuestionnaireId() + "风险因素")
                        .riskLevel(rLevel)
                        .description("该问卷显示存在" + (rle != null ? rle.getName() : "异常") + "风险")
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
                  .append(", 风险等级 ");
            RiskLevelEnum rle = safeRiskLevel(result.getRiskLevel());
            report.append(rle != null ? rle.getName() : "未知");
            // 来自 evaluate_config 的单卷评价/建议（在问卷结果阶段已计算），这里统一拼接到综合报告
            if (result.getLevelDescription() != null && !result.getLevelDescription().isEmpty()) {
                report.append("，评价：").append(result.getLevelDescription());
            }
            if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
                report.append("，建议：").append(result.getSuggestions());
            }
            report.append("\n");
        }

        if (!riskFactors.isEmpty()) {
            report.append("\n识别的风险因素:\n");
            for (RiskFactorVO riskFactor : riskFactors) {
                report.append("- ").append(riskFactor.getFactorName())
                      .append(": ").append(riskFactor.getDescription()).append("\n");
            }
        }

        // 检查是否应用了PHCSS规则，如果是则添加PHCSS专用的评价和建议
        Optional<QuestionnaireResultVO> phcssResult = questionnaireResults.stream()
                .filter(r -> Long.valueOf(12L).equals(r.getQuestionnaireId()))
                .findFirst();

        if (phcssResult.isPresent()) {
            String phcssEvaluation = getPhcssEvaluation(combinedRiskLevel);
            String phcssSuggestion = getPhcssSuggestion(combinedRiskLevel);

            report.append("\n=== PHCSS测评结果解读 ===\n");
            report.append("评价：").append(phcssEvaluation).append("\n");
            report.append("建议：").append(phcssSuggestion).append("\n");
        }

        return report.toString();
    }

    /**
     * 应用PHCSS维度计数规则
     * 根据PHCSS问卷6个维度低于界值的数量确定风险等级
     */
    private RiskLevelEnum applyPhcssCountRule(QuestionnaireResultVO phcssResult) {
        if (phcssResult.getDimensionScores() == null || phcssResult.getDimensionScores().isEmpty()) {
            log.warn("PHCSS问卷结果缺少维度分数，使用兜底风险等级");
            return RiskLevelEnum.NORMAL;
        }

        // PHCSS 6个维度的阈值配置（硬编码，后续可配置化）
        Map<String, BigDecimal> phcssThresholds = getPhcssThresholds();

        // 统计低于界值的维度数量
        int belowThresholdCount = 0;
        Map<String, BigDecimal> dimensionScores = phcssResult.getDimensionScores();
        
        log.info("PHCSS维度分析开始，共{}个维度", dimensionScores.size());

        for (Map.Entry<String, BigDecimal> entry : dimensionScores.entrySet()) {
            String dimensionKey = entry.getKey();
            BigDecimal score = entry.getValue();
            BigDecimal threshold = phcssThresholds.get(dimensionKey);

            if (threshold != null && score != null) {
                if (score.compareTo(threshold) < 0) {
                    belowThresholdCount++;
                    log.info("PHCSS维度 [{}] 得分={} < 临界值={} (低于临界值)", 
                        dimensionKey, score, threshold);
                } else {
                    log.info("PHCSS维度 [{}] 得分={} >= 临界值={} (正常)", 
                        dimensionKey, score, threshold);
                }
            } else {
                if (threshold == null) {
                    log.warn("PHCSS维度 [{}] 未找到对应的临界值配置，得分={}", dimensionKey, score);
                } else {
                    log.warn("PHCSS维度 [{}] 得分为空", dimensionKey);
                }
            }
        }

        log.info("PHCSS规则判定：共{}个维度低于临界值", belowThresholdCount);

        // 根据低于界值的维度数量映射风险等级
        return mapPhcssCountToRiskLevel(belowThresholdCount);
    }

    /**
     * 获取PHCSS 6个维度的阈值配置
     * TODO: 后续可从数据库配置表读取
     */
    private Map<String, BigDecimal> getPhcssThresholds() {
        Map<String, BigDecimal> thresholds = new HashMap<>();
        // PHCSS量表6个维度的临界值配置
        // 注：这里的维度名称需要与实际问卷返回的维度名称保持一致
        // 如果实际维度名称不同，需要根据实际情况调整
        thresholds.put("躯体化", new BigDecimal("2.0"));         // 躯体化维度临界值
        thresholds.put("强迫", new BigDecimal("2.0"));          // 强迫维度临界值
        thresholds.put("人际关系敏感", new BigDecimal("2.0"));   // 人际关系敏感维度临界值
        thresholds.put("抑郁", new BigDecimal("2.0"));          // 抑郁维度临界值
        thresholds.put("焦虑", new BigDecimal("2.0"));          // 焦虑维度临界值
        thresholds.put("敌对", new BigDecimal("2.0"));          // 敌对维度临界值
        
        // 如果维度名称是英文或其他格式，添加映射
        // 例如：
        thresholds.put("somatization", new BigDecimal("2.0"));       // 躯体化英文
        thresholds.put("obsession", new BigDecimal("2.0"));          // 强迫英文
        thresholds.put("interpersonal", new BigDecimal("2.0"));      // 人际关系敏感英文
        thresholds.put("depression", new BigDecimal("2.0"));         // 抑郁英文
        thresholds.put("anxiety", new BigDecimal("2.0"));            // 焦虑英文
        thresholds.put("hostility", new BigDecimal("2.0"));          // 敌对英文
        
        return thresholds;
    }

    /**
     * 将PHCSS低于界值的维度数量映射到风险等级
     */
    private RiskLevelEnum mapPhcssCountToRiskLevel(int belowThresholdCount) {
        if (belowThresholdCount == 0) {
            return RiskLevelEnum.NORMAL; // 无/低风险
        } else if (belowThresholdCount <= 2) {
            return RiskLevelEnum.ATTENTION; // 轻度风险
        } else if (belowThresholdCount == 3) {
            return RiskLevelEnum.WARNING; // 中度风险
        } else { // >= 4
            return RiskLevelEnum.HIGH_RISK; // 重度风险
        }
    }

    /**
     * 获取PHCSS风险等级对应的评价文本
     */
    private String getPhcssEvaluation(RiskLevelEnum riskLevel) {
        switch (riskLevel) {
            case NORMAL:
                return "目前的心理状况良好。";
            case ATTENTION:
                return "目前为心理健康问题的低风险人群。";
            case WARNING:
                return "目前为心理健康问题的中风险人群。";
            case HIGH_RISK:
                return "目前为心理健康问题的高风险人群。";
            default:
                return "心理状况评估结果未知。";
        }
    }

    /**
     * 获取PHCSS风险等级对应的建议文本
     */
    private String getPhcssSuggestion(RiskLevelEnum riskLevel) {
        switch (riskLevel) {
            case NORMAL:
                return "无需关注，可建议请继续保持良好的心境。";
            case ATTENTION:
                return "日常关注，可建议多做运动，参加社交活动，丰富日常生活。";
            case WARNING:
                return "建议多与家长、老师、同伴交流自己的感受、想法，寻求他们的帮助和情感支持；多参与户外活动，释放压力、调节情绪；关注学校心理咨询中心的活动，必要时寻求心理咨询帮助【补充说明：鉴于小学生正处于身心快速发展时期，此结果仅作部分参考，建议同时关注家长端评估结果】。";
            case HIGH_RISK:
                return "建议积极寻求家长、学校老师及学校心理咨询中心的帮助，必要时在家长陪同下至专科门诊寻求专业心理支持【补充说明：鉴于小学生正处于身心快速发展时期，此结果仅作部分参考，建议同时关注家长端评估结果】。";
            default:
                return "请咨询专业心理健康工作者获取进一步建议。";
        }
    }

}