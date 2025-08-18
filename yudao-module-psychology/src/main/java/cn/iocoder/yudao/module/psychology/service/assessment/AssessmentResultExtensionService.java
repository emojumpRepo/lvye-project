package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 测评结果扩展服务接口
 *
 * @author 芋道源码
 */
public interface AssessmentResultExtensionService {

    /**
     * 生成组合测评结果
     *
     * @param assessmentId 测评ID
     * @param studentProfileId 学生档案ID
     * @param questionnaireResultIds 问卷结果ID列表
     * @return 组合测评结果ID
     */
    Long generateCombinedAssessmentResult(Long assessmentId, Long studentProfileId, List<Long> questionnaireResultIds);

    /**
     * 检查测评完成状态
     *
     * @param assessmentId 测评ID
     * @param studentProfileId 学生档案ID
     * @return 完成状态信息
     */
    AssessmentCompletionStatus checkAssessmentCompletionStatus(Long assessmentId, Long studentProfileId);

    /**
     * 获取用户测评记录
     *
     * @param studentProfileId 学生档案ID
     * @param pageReqVO 分页查询条件
     * @return 测评记录分页
     */
    PageResult<Object> getUserAssessmentRecords(Long studentProfileId, Object pageReqVO);

    /**
     * 获取历史测评结果对比
     *
     * @param assessmentId 测评ID
     * @param studentProfileId 学生档案ID
     * @param limit 限制数量
     * @return 历史对比数据
     */
    List<AssessmentResultComparison> getHistoryAssessmentComparison(Long assessmentId, Long studentProfileId, Integer limit);

    /**
     * 获取测评结果趋势分析
     *
     * @param assessmentId 测评ID
     * @param studentProfileId 学生档案ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势分析数据
     */
    AssessmentTrendAnalysis getAssessmentTrendAnalysis(Long assessmentId, Long studentProfileId, 
                                                      LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 批量生成测评结果
     *
     * @param batchGenerationReqVO 批量生成请求
     * @return 批量生成结果
     */
    BatchAssessmentGenerationResult batchGenerateAssessmentResults(Object batchGenerationReqVO);

    /**
     * 获取测评结果统计
     *
     * @param assessmentId 测评ID
     * @param timeRange 时间范围
     * @return 统计数据
     */
    AssessmentResultStatistics getAssessmentResultStatistics(Long assessmentId, Integer timeRange);

    /**
     * 测评完成状态
     */
    class AssessmentCompletionStatus {
        private boolean completed;
        private Integer totalQuestionnaires;
        private Integer completedQuestionnaires;
        private Double completionRate;
        private List<String> pendingQuestionnaires;
        private LocalDateTime lastCompletionTime;
        private String statusMessage;

        // Getters and Setters
        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public Integer getTotalQuestionnaires() {
            return totalQuestionnaires;
        }

        public void setTotalQuestionnaires(Integer totalQuestionnaires) {
            this.totalQuestionnaires = totalQuestionnaires;
        }

        public Integer getCompletedQuestionnaires() {
            return completedQuestionnaires;
        }

        public void setCompletedQuestionnaires(Integer completedQuestionnaires) {
            this.completedQuestionnaires = completedQuestionnaires;
        }

        public Double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(Double completionRate) {
            this.completionRate = completionRate;
        }

        public List<String> getPendingQuestionnaires() {
            return pendingQuestionnaires;
        }

        public void setPendingQuestionnaires(List<String> pendingQuestionnaires) {
            this.pendingQuestionnaires = pendingQuestionnaires;
        }

        public LocalDateTime getLastCompletionTime() {
            return lastCompletionTime;
        }

        public void setLastCompletionTime(LocalDateTime lastCompletionTime) {
            this.lastCompletionTime = lastCompletionTime;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }
    }

    /**
     * 测评结果对比
     */
    class AssessmentResultComparison {
        private Long resultId;
        private LocalDateTime assessmentTime;
        private Double totalScore;
        private Integer riskLevel;
        private Map<String, Double> dimensionScores;
        private String changeDescription;
        private Double scoreChange;
        private Integer riskLevelChange;

        // Getters and Setters
        public Long getResultId() {
            return resultId;
        }

        public void setResultId(Long resultId) {
            this.resultId = resultId;
        }

        public LocalDateTime getAssessmentTime() {
            return assessmentTime;
        }

        public void setAssessmentTime(LocalDateTime assessmentTime) {
            this.assessmentTime = assessmentTime;
        }

        public Double getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(Double totalScore) {
            this.totalScore = totalScore;
        }

        public Integer getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(Integer riskLevel) {
            this.riskLevel = riskLevel;
        }

        public Map<String, Double> getDimensionScores() {
            return dimensionScores;
        }

        public void setDimensionScores(Map<String, Double> dimensionScores) {
            this.dimensionScores = dimensionScores;
        }

        public String getChangeDescription() {
            return changeDescription;
        }

        public void setChangeDescription(String changeDescription) {
            this.changeDescription = changeDescription;
        }

        public Double getScoreChange() {
            return scoreChange;
        }

        public void setScoreChange(Double scoreChange) {
            this.scoreChange = scoreChange;
        }

        public Integer getRiskLevelChange() {
            return riskLevelChange;
        }

        public void setRiskLevelChange(Integer riskLevelChange) {
            this.riskLevelChange = riskLevelChange;
        }
    }

    /**
     * 测评趋势分析
     */
    class AssessmentTrendAnalysis {
        private List<Map<String, Object>> trendData;
        private Map<String, Object> trendSummary;
        private List<String> insights;
        private Double overallTrend; // 正数表示上升趋势，负数表示下降趋势
        private String trendDescription;

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

        public List<String> getInsights() {
            return insights;
        }

        public void setInsights(List<String> insights) {
            this.insights = insights;
        }

        public Double getOverallTrend() {
            return overallTrend;
        }

        public void setOverallTrend(Double overallTrend) {
            this.overallTrend = overallTrend;
        }

        public String getTrendDescription() {
            return trendDescription;
        }

        public void setTrendDescription(String trendDescription) {
            this.trendDescription = trendDescription;
        }
    }

    /**
     * 批量测评生成结果
     */
    class BatchAssessmentGenerationResult {
        private Integer totalCount;
        private Integer successCount;
        private Integer failureCount;
        private List<String> errorMessages;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String summary;

        // Getters and Setters
        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Integer successCount) {
            this.successCount = successCount;
        }

        public Integer getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(Integer failureCount) {
            this.failureCount = failureCount;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }

        public void setErrorMessages(List<String> errorMessages) {
            this.errorMessages = errorMessages;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }

    /**
     * 测评结果统计
     */
    class AssessmentResultStatistics {
        private Long totalAssessments;
        private Long completedAssessments;
        private Double completionRate;
        private Double averageScore;
        private Map<Integer, Long> riskLevelDistribution;
        private Map<String, Double> dimensionAverages;
        private List<Map<String, Object>> timeSeriesData;

        // Getters and Setters
        public Long getTotalAssessments() {
            return totalAssessments;
        }

        public void setTotalAssessments(Long totalAssessments) {
            this.totalAssessments = totalAssessments;
        }

        public Long getCompletedAssessments() {
            return completedAssessments;
        }

        public void setCompletedAssessments(Long completedAssessments) {
            this.completedAssessments = completedAssessments;
        }

        public Double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(Double completionRate) {
            this.completionRate = completionRate;
        }

        public Double getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(Double averageScore) {
            this.averageScore = averageScore;
        }

        public Map<Integer, Long> getRiskLevelDistribution() {
            return riskLevelDistribution;
        }

        public void setRiskLevelDistribution(Map<Integer, Long> riskLevelDistribution) {
            this.riskLevelDistribution = riskLevelDistribution;
        }

        public Map<String, Double> getDimensionAverages() {
            return dimensionAverages;
        }

        public void setDimensionAverages(Map<String, Double> dimensionAverages) {
            this.dimensionAverages = dimensionAverages;
        }

        public List<Map<String, Object>> getTimeSeriesData() {
            return timeSeriesData;
        }

        public void setTimeSeriesData(List<Map<String, Object>> timeSeriesData) {
            this.timeSeriesData = timeSeriesData;
        }
    }

}