package cn.iocoder.yudao.module.psychology.service.assessment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 测评结果统计分析服务接口
 *
 * @author 芋道源码
 */
public interface AssessmentResultStatisticsService {

    /**
     * 获取测评结果趋势分析
     *
     * @param assessmentId 测评ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势分析结果
     */
    TrendAnalysisResult getAssessmentTrendAnalysis(Long assessmentId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取风险等级分布统计
     *
     * @param assessmentId 测评ID
     * @param timeRange 时间范围（天数）
     * @return 风险等级分布
     */
    RiskLevelDistribution getRiskLevelDistribution(Long assessmentId, Integer timeRange);

    /**
     * 获取测评结果对比分析
     *
     * @param assessmentIds 测评ID列表
     * @param timeRange 时间范围（天数）
     * @return 对比分析结果
     */
    ComparisonAnalysisResult getAssessmentComparisonAnalysis(List<Long> assessmentIds, Integer timeRange);

    /**
     * 获取测评效果评估
     *
     * @param assessmentId 测评ID
     * @param studentProfileId 学生档案ID
     * @param evaluationPeriod 评估周期（天数）
     * @return 效果评估结果
     */
    EffectivenessEvaluation getAssessmentEffectivenessEvaluation(Long assessmentId, Long studentProfileId, Integer evaluationPeriod);

    /**
     * 获取测评数据可视化支持
     *
     * @param visualizationReqVO 可视化请求
     * @return 可视化数据
     */
    VisualizationData getAssessmentVisualizationData(Object visualizationReqVO);

    /**
     * 生成统计报告
     *
     * @param reportReqVO 报告请求
     * @return 统计报告
     */
    StatisticalReport generateStatisticalReport(Object reportReqVO);

    /**
     * 趋势分析结果
     */
    class TrendAnalysisResult {
        private List<Map<String, Object>> trendData;
        private Map<String, Object> trendSummary;
        private List<String> keyInsights;
        private Double overallTrendSlope;
        private String trendDirection;
        private Map<String, Double> dimensionTrends;
        private List<Map<String, Object>> seasonalPatterns;

        // Getters and Setters
        public List<Map<String, Object>> getTrendData() {
            return trendData;
        }

        public void setTrendData(List<Map<String, Object>> trendData) {
            this.trendData = trendData;
        }

        public Map<String, Object> getTrendSummary() {
            return trendSummary;
        }

        public void setTrendSummary(Map<String, Object> trendSummary) {
            this.trendSummary = trendSummary;
        }

        public List<String> getKeyInsights() {
            return keyInsights;
        }

        public void setKeyInsights(List<String> keyInsights) {
            this.keyInsights = keyInsights;
        }

        public Double getOverallTrendSlope() {
            return overallTrendSlope;
        }

        public void setOverallTrendSlope(Double overallTrendSlope) {
            this.overallTrendSlope = overallTrendSlope;
        }

        public String getTrendDirection() {
            return trendDirection;
        }

        public void setTrendDirection(String trendDirection) {
            this.trendDirection = trendDirection;
        }

        public Map<String, Double> getDimensionTrends() {
            return dimensionTrends;
        }

        public void setDimensionTrends(Map<String, Double> dimensionTrends) {
            this.dimensionTrends = dimensionTrends;
        }

        public List<Map<String, Object>> getSeasonalPatterns() {
            return seasonalPatterns;
        }

        public void setSeasonalPatterns(List<Map<String, Object>> seasonalPatterns) {
            this.seasonalPatterns = seasonalPatterns;
        }
    }

    /**
     * 风险等级分布
     */
    class RiskLevelDistribution {
        private Map<Integer, Long> distribution;
        private Map<Integer, Double> percentages;
        private Integer mostCommonRiskLevel;
        private Double averageRiskLevel;
        private List<Map<String, Object>> timeSeriesDistribution;
        private Map<String, Object> comparisonWithPrevious;

        // Getters and Setters
        public Map<Integer, Long> getDistribution() {
            return distribution;
        }

        public void setDistribution(Map<Integer, Long> distribution) {
            this.distribution = distribution;
        }

        public Map<Integer, Double> getPercentages() {
            return percentages;
        }

        public void setPercentages(Map<Integer, Double> percentages) {
            this.percentages = percentages;
        }

        public Integer getMostCommonRiskLevel() {
            return mostCommonRiskLevel;
        }

        public void setMostCommonRiskLevel(Integer mostCommonRiskLevel) {
            this.mostCommonRiskLevel = mostCommonRiskLevel;
        }

        public Double getAverageRiskLevel() {
            return averageRiskLevel;
        }

        public void setAverageRiskLevel(Double averageRiskLevel) {
            this.averageRiskLevel = averageRiskLevel;
        }

        public List<Map<String, Object>> getTimeSeriesDistribution() {
            return timeSeriesDistribution;
        }

        public void setTimeSeriesDistribution(List<Map<String, Object>> timeSeriesDistribution) {
            this.timeSeriesDistribution = timeSeriesDistribution;
        }

        public Map<String, Object> getComparisonWithPrevious() {
            return comparisonWithPrevious;
        }

        public void setComparisonWithPrevious(Map<String, Object> comparisonWithPrevious) {
            this.comparisonWithPrevious = comparisonWithPrevious;
        }
    }

    /**
     * 对比分析结果
     */
    class ComparisonAnalysisResult {
        private Map<Long, Map<String, Object>> assessmentSummaries;
        private Map<String, List<Double>> dimensionComparisons;
        private Map<String, Object> statisticalComparison;
        private List<String> significantDifferences;
        private Map<String, Double> correlationMatrix;
        private String comparisonSummary;

        // Getters and Setters
        public Map<Long, Map<String, Object>> getAssessmentSummaries() {
            return assessmentSummaries;
        }

        public void setAssessmentSummaries(Map<Long, Map<String, Object>> assessmentSummaries) {
            this.assessmentSummaries = assessmentSummaries;
        }

        public Map<String, List<Double>> getDimensionComparisons() {
            return dimensionComparisons;
        }

        public void setDimensionComparisons(Map<String, List<Double>> dimensionComparisons) {
            this.dimensionComparisons = dimensionComparisons;
        }

        public Map<String, Object> getStatisticalComparison() {
            return statisticalComparison;
        }

        public void setStatisticalComparison(Map<String, Object> statisticalComparison) {
            this.statisticalComparison = statisticalComparison;
        }

        public List<String> getSignificantDifferences() {
            return significantDifferences;
        }

        public void setSignificantDifferences(List<String> significantDifferences) {
            this.significantDifferences = significantDifferences;
        }

        public Map<String, Double> getCorrelationMatrix() {
            return correlationMatrix;
        }

        public void setCorrelationMatrix(Map<String, Double> correlationMatrix) {
            this.correlationMatrix = correlationMatrix;
        }

        public String getComparisonSummary() {
            return comparisonSummary;
        }

        public void setComparisonSummary(String comparisonSummary) {
            this.comparisonSummary = comparisonSummary;
        }
    }

    /**
     * 效果评估
     */
    class EffectivenessEvaluation {
        private Double improvementScore;
        private String improvementLevel;
        private Map<String, Double> dimensionImprovements;
        private List<String> positiveChanges;
        private List<String> areasNeedingAttention;
        private Map<String, Object> progressMetrics;
        private String evaluationSummary;
        private List<String> recommendations;

        // Getters and Setters
        public Double getImprovementScore() {
            return improvementScore;
        }

        public void setImprovementScore(Double improvementScore) {
            this.improvementScore = improvementScore;
        }

        public String getImprovementLevel() {
            return improvementLevel;
        }

        public void setImprovementLevel(String improvementLevel) {
            this.improvementLevel = improvementLevel;
        }

        public Map<String, Double> getDimensionImprovements() {
            return dimensionImprovements;
        }

        public void setDimensionImprovements(Map<String, Double> dimensionImprovements) {
            this.dimensionImprovements = dimensionImprovements;
        }

        public List<String> getPositiveChanges() {
            return positiveChanges;
        }

        public void setPositiveChanges(List<String> positiveChanges) {
            this.positiveChanges = positiveChanges;
        }

        public List<String> getAreasNeedingAttention() {
            return areasNeedingAttention;
        }

        public void setAreasNeedingAttention(List<String> areasNeedingAttention) {
            this.areasNeedingAttention = areasNeedingAttention;
        }

        public Map<String, Object> getProgressMetrics() {
            return progressMetrics;
        }

        public void setProgressMetrics(Map<String, Object> progressMetrics) {
            this.progressMetrics = progressMetrics;
        }

        public String getEvaluationSummary() {
            return evaluationSummary;
        }

        public void setEvaluationSummary(String evaluationSummary) {
            this.evaluationSummary = evaluationSummary;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }
    }

    /**
     * 可视化数据
     */
    class VisualizationData {
        private Map<String, Object> chartData;
        private Map<String, String> chartConfigs;
        private List<Map<String, Object>> dashboardWidgets;
        private Map<String, Object> interactiveElements;

        // Getters and Setters
        public Map<String, Object> getChartData() {
            return chartData;
        }

        public void setChartData(Map<String, Object> chartData) {
            this.chartData = chartData;
        }

        public Map<String, String> getChartConfigs() {
            return chartConfigs;
        }

        public void setChartConfigs(Map<String, String> chartConfigs) {
            this.chartConfigs = chartConfigs;
        }

        public List<Map<String, Object>> getDashboardWidgets() {
            return dashboardWidgets;
        }

        public void setDashboardWidgets(List<Map<String, Object>> dashboardWidgets) {
            this.dashboardWidgets = dashboardWidgets;
        }

        public Map<String, Object> getInteractiveElements() {
            return interactiveElements;
        }

        public void setInteractiveElements(Map<String, Object> interactiveElements) {
            this.interactiveElements = interactiveElements;
        }
    }

    /**
     * 统计报告
     */
    class StatisticalReport {
        private String reportTitle;
        private LocalDateTime generationTime;
        private Map<String, Object> executiveSummary;
        private List<Map<String, Object>> detailedAnalysis;
        private List<String> keyFindings;
        private List<String> recommendations;
        private Map<String, Object> appendices;
        private String reportContent;

        // Getters and Setters
        public String getReportTitle() {
            return reportTitle;
        }

        public void setReportTitle(String reportTitle) {
            this.reportTitle = reportTitle;
        }

        public LocalDateTime getGenerationTime() {
            return generationTime;
        }

        public void setGenerationTime(LocalDateTime generationTime) {
            this.generationTime = generationTime;
        }

        public Map<String, Object> getExecutiveSummary() {
            return executiveSummary;
        }

        public void setExecutiveSummary(Map<String, Object> executiveSummary) {
            this.executiveSummary = executiveSummary;
        }

        public List<Map<String, Object>> getDetailedAnalysis() {
            return detailedAnalysis;
        }

        public void setDetailedAnalysis(List<Map<String, Object>> detailedAnalysis) {
            this.detailedAnalysis = detailedAnalysis;
        }

        public List<String> getKeyFindings() {
            return keyFindings;
        }

        public void setKeyFindings(List<String> keyFindings) {
            this.keyFindings = keyFindings;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }

        public Map<String, Object> getAppendices() {
            return appendices;
        }

        public void setAppendices(Map<String, Object> appendices) {
            this.appendices = appendices;
        }

        public String getReportContent() {
            return reportContent;
        }

        public void setReportContent(String reportContent) {
            this.reportContent = reportContent;
        }
    }

}