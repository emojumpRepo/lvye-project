package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.psychology.service.assessment.CombinedAssessmentResultGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组合测评结果生成服务实现
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@SuppressWarnings("unchecked")
public class CombinedAssessmentResultGenerationServiceImpl implements CombinedAssessmentResultGenerationService {

    @Override
    public CombinedAssessmentResult generateCombinedResult(Long assessmentId, Long studentProfileId, 
                                                          List<QuestionnaireResultDO> questionnaireResults) {
        log.info("生成组合测评结果，测评ID: {}, 学生档案ID: {}, 问卷数量: {}", 
                assessmentId, studentProfileId, questionnaireResults.size());
        
        try {
            CombinedAssessmentResult result = new CombinedAssessmentResult();
            result.setAssessmentId(assessmentId);
            result.setStudentProfileId(studentProfileId);
            
            // 1. 聚合多问卷结果
            AggregatedQuestionnaireResult aggregatedResult = aggregateQuestionnaireResults(questionnaireResults);
            result.setAggregatedResult(aggregatedResult);
            
            // 2. 综合分析
            ComprehensiveAnalysisResult analysisResult = performComprehensiveAnalysis(aggregatedResult);
            result.setAnalysisResult(analysisResult);
            
            // 3. 风险评估
            RiskAssessmentResult riskAssessment = assessRisk(analysisResult);
            result.setRiskAssessment(riskAssessment);
            
            // 4. 生成干预建议
            InterventionRecommendations interventionRecommendations = 
                    generateInterventionRecommendations(riskAssessment, analysisResult);
            result.setInterventionRecommendations(interventionRecommendations);
            
            // 5. 设置整体评分和风险等级
            result.setOverallScore(aggregatedResult.getWeightedAverageScore());
            result.setOverallRiskLevel(riskAssessment.getOverallRiskLevel());
            result.setDimensionScores(aggregatedResult.getDimensionAverages());
            
            // 6. 生成综合报告
            String comprehensiveReport = generateComprehensiveReport(result);
            result.setComprehensiveReport(comprehensiveReport);
            
            // 7. 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("generationTime", System.currentTimeMillis());
            metadata.put("questionnaireCount", questionnaireResults.size());
            metadata.put("algorithmVersion", "1.0");
            result.setMetadata(metadata);
            
            log.info("组合测评结果生成完成，整体评分: {}, 风险等级: {}", 
                    result.getOverallScore(), result.getOverallRiskLevel());
            
            return result;
            
        } catch (Exception e) {
            log.error("生成组合测评结果失败，测评ID: {}, 学生档案ID: {}", assessmentId, studentProfileId, e);
            throw new RuntimeException("生成组合测评结果失败: " + e.getMessage());
        }
    }

    @Override
    public AggregatedQuestionnaireResult aggregateQuestionnaireResults(List<QuestionnaireResultDO> questionnaireResults) {
        log.debug("聚合多问卷结果，问卷数量: {}", questionnaireResults.size());
        
        AggregatedQuestionnaireResult aggregated = new AggregatedQuestionnaireResult();
        
        if (questionnaireResults.isEmpty()) {
            return aggregated;
        }
        
        // 基本统计
        aggregated.setTotalQuestionnaires(questionnaireResults.size());
        
        // 计算加权平均分（使用标准分 standardScore）
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        for (QuestionnaireResultDO result : questionnaireResults) {
            double weight = getQuestionnaireWeight(result.getQuestionnaireId());
            double standardScore = result.getStandardScore() != null ? result.getStandardScore().doubleValue() : 0.0;
            totalWeightedScore += standardScore * weight;
            totalWeight += weight;
        }
        aggregated.setWeightedAverageScore(totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0);
        
        // 聚合维度得分
        Map<String, List<Double>> dimensionScoresList = new HashMap<>();
        for (QuestionnaireResultDO result : questionnaireResults) {
            Map<String, Double> dimensionScores = parseDimensionScores(result.getDimensionScores());
            for (Map.Entry<String, Double> entry : dimensionScores.entrySet()) {
                dimensionScoresList.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .add(entry.getValue());
            }
        }
        
        Map<String, Double> dimensionAverages = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : dimensionScoresList.entrySet()) {
            double average = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            dimensionAverages.put(entry.getKey(), average);
        }
        aggregated.setDimensionAverages(dimensionAverages);
        
        // 统计风险等级分布
        Map<String, Integer> riskLevelCounts = questionnaireResults.stream()
                .collect(Collectors.groupingBy(
                        result -> "Level" + result.getRiskLevel(),
                        Collectors.collectingAndThen(Collectors.counting(), count -> count.intValue())
                ));
        aggregated.setRiskLevelCounts(riskLevelCounts);
        
        // 识别显著发现
        List<String> significantFindings = identifySignificantFindings(questionnaireResults, dimensionAverages);
        aggregated.setSignificantFindings(significantFindings);
        
        // 统计摘要
        Map<String, Object> statisticalSummary = new HashMap<>();
        statisticalSummary.put("scoreStandardDeviation", calculateScoreStandardDeviation(questionnaireResults));
        statisticalSummary.put("consistencyIndex", calculateConsistencyIndex(questionnaireResults));
        statisticalSummary.put("completionRate", calculateCompletionRate(questionnaireResults));
        aggregated.setStatisticalSummary(statisticalSummary);
        
        return aggregated;
    }

    @Override
    public ComprehensiveAnalysisResult performComprehensiveAnalysis(AggregatedQuestionnaireResult aggregatedResult) {
        log.debug("执行综合分析");
        
        ComprehensiveAnalysisResult analysis = new ComprehensiveAnalysisResult();
        
        // 计算相关性矩阵
        Map<String, Double> correlationMatrix = calculateCorrelationMatrix(aggregatedResult.getDimensionAverages());
        analysis.setCorrelationMatrix(correlationMatrix);
        
        // 模式分析
        List<String> patternAnalysis = performPatternAnalysis(aggregatedResult);
        analysis.setPatternAnalysis(patternAnalysis);
        
        // 心理档案
        Map<String, Object> psychologicalProfile = buildPsychologicalProfile(aggregatedResult);
        analysis.setPsychologicalProfile(psychologicalProfile);
        
        // 识别优势
        List<String> strengths = identifyStrengths(aggregatedResult.getDimensionAverages());
        analysis.setStrengthsIdentified(strengths);
        
        // 识别关注领域
        List<String> areasOfConcern = identifyAreasOfConcern(aggregatedResult.getDimensionAverages());
        analysis.setAreasOfConcern(areasOfConcern);
        
        // 一致性评分
        Double consistencyScore = (Double) aggregatedResult.getStatisticalSummary().get("consistencyIndex");
        analysis.setConsistencyScore(consistencyScore != null ? consistencyScore : 0.0);
        
        return analysis;
    }

    @Override
    public RiskAssessmentResult assessRisk(ComprehensiveAnalysisResult analysisResult) {
        log.debug("执行风险评估");
        
        RiskAssessmentResult riskAssessment = new RiskAssessmentResult();
        
        // 计算整体风险等级
        Integer overallRiskLevel = calculateOverallRiskLevel(analysisResult);
        riskAssessment.setOverallRiskLevel(overallRiskLevel);
        
        // 各维度风险等级
        Map<String, Integer> dimensionRiskLevels = calculateDimensionRiskLevels(analysisResult);
        riskAssessment.setDimensionRiskLevels(dimensionRiskLevels);
        
        // 风险因素
        List<String> riskFactors = identifyRiskFactors(analysisResult);
        riskAssessment.setRiskFactors(riskFactors);
        
        // 保护因素
        List<String> protectiveFactors = identifyProtectiveFactors(analysisResult);
        riskAssessment.setProtectiveFactors(protectiveFactors);
        
        // 风险摘要
        String riskSummary = generateRiskSummary(overallRiskLevel, riskFactors, protectiveFactors);
        riskAssessment.setRiskSummary(riskSummary);
        
        // 是否需要立即关注
        Boolean requiresImmediateAttention = overallRiskLevel >= 4 || 
                riskFactors.stream().anyMatch(factor -> factor.contains("严重") || factor.contains("高风险"));
        riskAssessment.setRequiresImmediateAttention(requiresImmediateAttention);
        
        return riskAssessment;
    }

    @Override
    public InterventionRecommendations generateInterventionRecommendations(RiskAssessmentResult riskAssessment, 
                                                                          ComprehensiveAnalysisResult analysisResult) {
        log.debug("生成干预建议");
        
        InterventionRecommendations recommendations = new InterventionRecommendations();
        
        // 立即行动建议
        List<String> immediateActions = generateImmediateActions(riskAssessment);
        recommendations.setImmediateActions(immediateActions);
        
        // 短期目标
        List<String> shortTermGoals = generateShortTermGoals(riskAssessment, analysisResult);
        recommendations.setShortTermGoals(shortTermGoals);
        
        // 长期目标
        List<String> longTermGoals = generateLongTermGoals(analysisResult);
        recommendations.setLongTermGoals(longTermGoals);
        
        // 维度特定建议
        Map<String, List<String>> dimensionSpecificRecommendations = 
                generateDimensionSpecificRecommendations(riskAssessment, analysisResult);
        recommendations.setDimensionSpecificRecommendations(dimensionSpecificRecommendations);
        
        // 资源推荐
        List<String> resourceRecommendations = generateResourceRecommendations(riskAssessment);
        recommendations.setResourceRecommendations(resourceRecommendations);
        
        // 随访计划
        String followUpPlan = generateFollowUpPlan(riskAssessment);
        recommendations.setFollowUpPlan(followUpPlan);
        
        return recommendations;
    }

    @Override
    public String generateComprehensiveReport(CombinedAssessmentResult combinedResult) {
        log.debug("生成综合报告");
        
        StringBuilder report = new StringBuilder();
        
        // 报告标题
        report.append("# 心理健康综合测评报告\n\n");
        
        // 基本信息
        report.append("## 基本信息\n");
        report.append(String.format("- 测评ID: %d\n", combinedResult.getAssessmentId()));
        report.append(String.format("- 学生档案ID: %d\n", combinedResult.getStudentProfileId()));
        report.append(String.format("- 问卷数量: %d\n", combinedResult.getAggregatedResult().getTotalQuestionnaires()));
        report.append(String.format("- 整体评分: %.1f\n", combinedResult.getOverallScore()));
        report.append(String.format("- 风险等级: %d\n\n", combinedResult.getOverallRiskLevel()));
        
        // 维度得分
        report.append("## 各维度得分\n");
        for (Map.Entry<String, Double> entry : combinedResult.getDimensionScores().entrySet()) {
            report.append(String.format("- %s: %.1f\n", entry.getKey(), entry.getValue()));
        }
        report.append("\n");
        
        // 风险评估
        report.append("## 风险评估\n");
        report.append(String.format("- 整体风险等级: %d\n", combinedResult.getRiskAssessment().getOverallRiskLevel()));
        report.append(String.format("- 风险摘要: %s\n", combinedResult.getRiskAssessment().getRiskSummary()));
        
        if (!combinedResult.getRiskAssessment().getRiskFactors().isEmpty()) {
            report.append("- 风险因素:\n");
            for (String factor : combinedResult.getRiskAssessment().getRiskFactors()) {
                report.append(String.format("  * %s\n", factor));
            }
        }
        
        if (!combinedResult.getRiskAssessment().getProtectiveFactors().isEmpty()) {
            report.append("- 保护因素:\n");
            for (String factor : combinedResult.getRiskAssessment().getProtectiveFactors()) {
                report.append(String.format("  * %s\n", factor));
            }
        }
        report.append("\n");
        
        // 干预建议
        report.append("## 干预建议\n");
        
        if (!combinedResult.getInterventionRecommendations().getImmediateActions().isEmpty()) {
            report.append("### 立即行动建议\n");
            for (String action : combinedResult.getInterventionRecommendations().getImmediateActions()) {
                report.append(String.format("- %s\n", action));
            }
            report.append("\n");
        }
        
        if (!combinedResult.getInterventionRecommendations().getShortTermGoals().isEmpty()) {
            report.append("### 短期目标\n");
            for (String goal : combinedResult.getInterventionRecommendations().getShortTermGoals()) {
                report.append(String.format("- %s\n", goal));
            }
            report.append("\n");
        }
        
        if (!combinedResult.getInterventionRecommendations().getLongTermGoals().isEmpty()) {
            report.append("### 长期目标\n");
            for (String goal : combinedResult.getInterventionRecommendations().getLongTermGoals()) {
                report.append(String.format("- %s\n", goal));
            }
            report.append("\n");
        }
        
        // 随访计划
        if (combinedResult.getInterventionRecommendations().getFollowUpPlan() != null) {
            report.append("## 随访计划\n");
            report.append(combinedResult.getInterventionRecommendations().getFollowUpPlan());
            report.append("\n\n");
        }
        
        // 报告生成信息
        report.append("---\n");
        report.append("*本报告由心理健康测评系统自动生成*\n");
        
        return report.toString();
    }

    // 私有辅助方法

    private double getQuestionnaireWeight(Long questionnaireId) {
        // TODO: 根据问卷类型返回权重，这里返回默认权重
        return 1.0;
    }

    private Map<String, Double> parseDimensionScores(String dimensionScoresJson) {
        Map<String, Double> scores = new HashMap<>();
        if (dimensionScoresJson == null || dimensionScoresJson.trim().isEmpty()) {
            return scores;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = JsonUtils.parseObject(dimensionScoresJson, Map.class);
            if (raw != null) {
                for (Map.Entry<String, Object> entry : raw.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Number) {
                        scores.put(entry.getKey(), ((Number) value).doubleValue());
                    } else if (value != null) {
                        try {
                            scores.put(entry.getKey(), Double.parseDouble(value.toString()));
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return scores;
    }

    private List<String> identifySignificantFindings(List<QuestionnaireResultDO> results, Map<String, Double> dimensionAverages) {
        List<String> findings = new ArrayList<>();
        
        // 检查高分维度
        for (Map.Entry<String, Double> entry : dimensionAverages.entrySet()) {
            if (entry.getValue() > 85) {
                findings.add(String.format("%s维度得分较高(%.1f)", entry.getKey(), entry.getValue()));
            } else if (entry.getValue() < 60) {
                findings.add(String.format("%s维度得分较低(%.1f)", entry.getKey(), entry.getValue()));
            }
        }
        
        return findings;
    }

    private double calculateScoreStandardDeviation(List<QuestionnaireResultDO> results) {
        if (results.size() < 2) return 0.0;
        
        double mean = results.stream()
                .mapToDouble(r -> r.getStandardScore() != null ? r.getStandardScore().doubleValue() : 0.0)
                .average().orElse(0.0);
        double variance = results.stream()
                .mapToDouble(result -> Math.pow((result.getStandardScore() != null ? result.getStandardScore().doubleValue() : 0.0) - mean, 2))
                .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }

    private double calculateConsistencyIndex(List<QuestionnaireResultDO> results) {
        // 简化的一致性指数计算
        double standardDeviation = calculateScoreStandardDeviation(results);
        double mean = results.stream()
                .mapToDouble(r -> r.getStandardScore() != null ? r.getStandardScore().doubleValue() : 0.0)
                .average().orElse(0.0);
        
        if (mean == 0) return 0.0;
        
        // 变异系数的倒数作为一致性指数
        double coefficientOfVariation = standardDeviation / mean;
        return Math.max(0, 1 - coefficientOfVariation);
    }

    private double calculateCompletionRate(List<QuestionnaireResultDO> results) {
        long completedCount = results.stream()
                .filter(result -> result.getGenerationStatus() != null && result.getGenerationStatus() == 3)
                .count();
        
        return results.isEmpty() ? 0.0 : (double) completedCount / results.size();
    }

    private Map<String, Double> calculateCorrelationMatrix(Map<String, Double> dimensionAverages) {
        // 简化的相关性计算，实际应该基于原始数据
        Map<String, Double> correlations = new HashMap<>();
        
        List<String> dimensions = new ArrayList<>(dimensionAverages.keySet());
        for (int i = 0; i < dimensions.size(); i++) {
            for (int j = i + 1; j < dimensions.size(); j++) {
                String key = dimensions.get(i) + "_" + dimensions.get(j);
                // 模拟相关性系数
                correlations.put(key, Math.random() * 0.8 + 0.1);
            }
        }
        
        return correlations;
    }

    private List<String> performPatternAnalysis(AggregatedQuestionnaireResult aggregatedResult) {
        List<String> patterns = new ArrayList<>();
        
        // 分析得分模式
        double averageScore = aggregatedResult.getWeightedAverageScore();
        if (averageScore > 80) {
            patterns.add("整体心理健康状况良好");
        } else if (averageScore < 60) {
            patterns.add("存在心理健康风险，需要关注");
        } else {
            patterns.add("心理健康状况中等，有改善空间");
        }
        
        // 分析维度差异
        Map<String, Double> dimensions = aggregatedResult.getDimensionAverages();
        if (!dimensions.isEmpty()) {
            double max = Collections.max(dimensions.values());
            double min = Collections.min(dimensions.values());
            if (max - min > 20) {
                patterns.add("各维度得分差异较大，存在不平衡");
            }
        }
        
        return patterns;
    }

    private Map<String, Object> buildPsychologicalProfile(AggregatedQuestionnaireResult aggregatedResult) {
        Map<String, Object> profile = new HashMap<>();
        
        profile.put("overallWellbeing", aggregatedResult.getWeightedAverageScore());
        profile.put("dimensionBalance", calculateDimensionBalance(aggregatedResult.getDimensionAverages()));
        profile.put("consistencyLevel", aggregatedResult.getStatisticalSummary().get("consistencyIndex"));
        profile.put("riskIndicators", identifyRiskIndicators(aggregatedResult));
        
        return profile;
    }

    private double calculateDimensionBalance(Map<String, Double> dimensionAverages) {
        if (dimensionAverages.isEmpty()) return 0.0;
        
        double max = Collections.max(dimensionAverages.values());
        double min = Collections.min(dimensionAverages.values());
        double range = max - min;
        
        // 平衡度 = 1 - (范围 / 最大可能范围)
        return Math.max(0, 1 - (range / 100.0));
    }

    private List<String> identifyRiskIndicators(AggregatedQuestionnaireResult aggregatedResult) {
        List<String> indicators = new ArrayList<>();
        
        if (aggregatedResult.getWeightedAverageScore() < 50) {
            indicators.add("整体得分偏低");
        }
        
        for (Map.Entry<String, Double> entry : aggregatedResult.getDimensionAverages().entrySet()) {
            if (entry.getValue() < 40) {
                indicators.add(entry.getKey() + "维度得分过低");
            }
        }
        
        return indicators;
    }

    private List<String> identifyStrengths(Map<String, Double> dimensionAverages) {
        return dimensionAverages.entrySet().stream()
                .filter(entry -> entry.getValue() > 80)
                .map(entry -> entry.getKey() + "维度表现优秀")
                .collect(Collectors.toList());
    }

    private List<String> identifyAreasOfConcern(Map<String, Double> dimensionAverages) {
        return dimensionAverages.entrySet().stream()
                .filter(entry -> entry.getValue() < 60)
                .map(entry -> entry.getKey() + "维度需要关注")
                .collect(Collectors.toList());
    }

    private Integer calculateOverallRiskLevel(ComprehensiveAnalysisResult analysisResult) {
        // 基于心理档案计算整体风险等级
        Double overallWellbeing = (Double) analysisResult.getPsychologicalProfile().get("overallWellbeing");
        
        if (overallWellbeing == null) return 3;
        
        if (overallWellbeing >= 80) return 1; // 低风险
        else if (overallWellbeing >= 60) return 2; // 中低风险
        else if (overallWellbeing >= 40) return 3; // 中等风险
        else if (overallWellbeing >= 20) return 4; // 高风险
        else return 5; // 极高风险
    }

    private Map<String, Integer> calculateDimensionRiskLevels(ComprehensiveAnalysisResult analysisResult) {
        Map<String, Integer> riskLevels = new HashMap<>();
        
        // 模拟各维度风险等级计算
        riskLevels.put("anxiety", 2);
        riskLevels.put("depression", 3);
        riskLevels.put("stress", 2);
        
        return riskLevels;
    }

    private List<String> identifyRiskFactors(ComprehensiveAnalysisResult analysisResult) {
        List<String> riskFactors = new ArrayList<>();
        
        if (!analysisResult.getAreasOfConcern().isEmpty()) {
            riskFactors.addAll(analysisResult.getAreasOfConcern());
        }
        
        if (analysisResult.getConsistencyScore() < 0.5) {
            riskFactors.add("测评结果一致性较低");
        }
        
        return riskFactors;
    }

    private List<String> identifyProtectiveFactors(ComprehensiveAnalysisResult analysisResult) {
        List<String> protectiveFactors = new ArrayList<>();
        
        if (!analysisResult.getStrengthsIdentified().isEmpty()) {
            protectiveFactors.addAll(analysisResult.getStrengthsIdentified());
        }
        
        if (analysisResult.getConsistencyScore() > 0.8) {
            protectiveFactors.add("测评结果一致性良好");
        }
        
        return protectiveFactors;
    }

    private String generateRiskSummary(Integer overallRiskLevel, List<String> riskFactors, List<String> protectiveFactors) {
        StringBuilder summary = new StringBuilder();
        
        switch (overallRiskLevel) {
            case 1:
                summary.append("整体风险较低，心理健康状况良好");
                break;
            case 2:
                summary.append("存在轻微风险，建议持续关注");
                break;
            case 3:
                summary.append("存在中等风险，建议采取干预措施");
                break;
            case 4:
                summary.append("存在较高风险，需要及时干预");
                break;
            case 5:
                summary.append("存在极高风险，需要立即干预");
                break;
            default:
                summary.append("风险等级未知");
        }
        
        if (!riskFactors.isEmpty()) {
            summary.append("，主要风险因素包括：").append(String.join("、", riskFactors));
        }
        
        if (!protectiveFactors.isEmpty()) {
            summary.append("，保护因素包括：").append(String.join("、", protectiveFactors));
        }
        
        return summary.toString();
    }

    private List<String> generateImmediateActions(RiskAssessmentResult riskAssessment) {
        List<String> actions = new ArrayList<>();
        
        if (riskAssessment.getRequiresImmediateAttention()) {
            actions.add("联系专业心理咨询师");
            actions.add("建立支持网络");
            actions.add("制定安全计划");
        } else if (riskAssessment.getOverallRiskLevel() >= 3) {
            actions.add("安排心理健康评估");
            actions.add("开始自我监测");
        }
        
        return actions;
    }

    private List<String> generateShortTermGoals(RiskAssessmentResult riskAssessment, ComprehensiveAnalysisResult analysisResult) {
        List<String> goals = new ArrayList<>();
        
        goals.add("建立规律的作息时间");
        goals.add("学习压力管理技巧");
        goals.add("增加社交活动");
        
        return goals;
    }

    private List<String> generateLongTermGoals(ComprehensiveAnalysisResult analysisResult) {
        List<String> goals = new ArrayList<>();
        
        goals.add("提升整体心理韧性");
        goals.add("建立健康的生活方式");
        goals.add("发展积极的应对策略");
        
        return goals;
    }

    private Map<String, List<String>> generateDimensionSpecificRecommendations(RiskAssessmentResult riskAssessment, 
                                                                              ComprehensiveAnalysisResult analysisResult) {
        Map<String, List<String>> recommendations = new HashMap<>();
        
        recommendations.put("anxiety", Arrays.asList("练习深呼吸", "学习放松技巧"));
        recommendations.put("depression", Arrays.asList("增加户外活动", "培养兴趣爱好"));
        recommendations.put("stress", Arrays.asList("时间管理", "设定合理目标"));
        
        return recommendations;
    }

    private List<String> generateResourceRecommendations(RiskAssessmentResult riskAssessment) {
        List<String> resources = new ArrayList<>();
        
        resources.add("心理健康手册");
        resources.add("冥想应用程序");
        resources.add("心理咨询热线");
        
        if (riskAssessment.getRequiresImmediateAttention()) {
            resources.add("紧急心理危机干预热线");
            resources.add("专业心理治疗机构");
        }
        
        return resources;
    }

    private String generateFollowUpPlan(RiskAssessmentResult riskAssessment) {
        StringBuilder plan = new StringBuilder();
        
        if (riskAssessment.getRequiresImmediateAttention()) {
            plan.append("建议1周内进行复评，");
        } else if (riskAssessment.getOverallRiskLevel() >= 3) {
            plan.append("建议2-4周内进行复评，");
        } else {
            plan.append("建议3个月内进行复评，");
        }
        
        plan.append("持续监测心理健康状况变化，根据情况调整干预策略。");
        
        return plan.toString();
    }

}