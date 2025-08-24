package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问卷结果服务接口
 *
 * @author 芋道源码
 */
public interface QuestionnaireResultService {

    /**
     * 删除问卷结果
     *
     * @param id 编号
     */
    void deleteQuestionnaireResult(Long id);

    /**
     * 批量删除问卷结果
     *
     * @param ids 编号列表
     */
    void deleteQuestionnaireResults(List<Long> ids);

    /**
     * 获得问卷结果
     *
     * @param id 编号
     * @return 问卷结果
     */
    QuestionnaireResultDO getQuestionnaireResult(Long id);

    /**
     * 获得问卷结果分页
     *
     * @param pageReqVO 分页查询
     * @return 问卷结果分页
     */
    PageResult<QuestionnaireResultDO> getQuestionnaireResultPage(Object pageReqVO);

    /**
     * 获取用户问卷结果列表
     *
     * @param userId 用户ID
     * @param questionnaireId 问卷ID（可选）
     * @return 问卷结果列表
     */
    List<QuestionnaireResultDO> getUserQuestionnaireResults(Long userId, Long questionnaireId);

    /**
     * 获取问卷的所有结果
     *
     * @param questionnaireId 问卷ID
     * @return 问卷结果列表
     */
    List<QuestionnaireResultDO> getQuestionnaireResults(Long questionnaireId);

    /**
     * 检查用户是否已完成问卷
     *
     * @param questionnaireId 问卷ID
     * @param userId 用户ID
     * @return 是否已完成
     */
    boolean hasUserCompletedQuestionnaire(Long questionnaireId, Long userId);

    /**
     * 获取问卷完成统计
     *
     * @param questionnaireId 问卷ID
     * @return 完成统计信息
     */
    QuestionnaireCompletionStats getQuestionnaireCompletionStats(Long questionnaireId);

    /**
     * 获取用户最新的问卷结果
     *
     * @param questionnaireId 问卷ID
     * @param userId 用户ID
     * @return 最新问卷结果
     */
    QuestionnaireResultDO getLatestUserQuestionnaireResult(Long questionnaireId, Long userId);

    /**
     * 提交问卷答案并生成结果
     *
     * @param submitReqVO 提交答案请求
     * @return 问卷结果ID
     */
    Long submitQuestionnaireAnswers(Object submitReqVO);

    /**
     * 异步生成问卷结果
     *
     * @param resultId 问卷结果ID
     * @return 是否成功启动生成任务
     */
    boolean generateResultAsync(Long resultId);

    /**
     * 获取结果生成状态
     *
     * @param resultId 问卷结果ID
     * @return 生成状态信息
     */
    ResultGenerationStatus getResultGenerationStatus(Long resultId);

    /**
     * 重新生成问卷结果
     *
     * @param resultId 问卷结果ID
     * @return 是否成功
     */
    boolean regenerateResult(Long resultId);

    /**
     * 批量生成问卷结果
     *
     * @param resultIds 问卷结果ID列表
     * @return 批量生成结果
     */
    BatchGenerationResult batchGenerateResults(List<Long> resultIds);

    /**
     * 多维度查询问卷结果
     *
     * @param queryReqVO 查询条件
     * @return 问卷结果分页
     */
    PageResult<QuestionnaireResultDO> queryQuestionnaireResults(Object queryReqVO);

    /**
     * 导出问卷结果到Excel
     *
     * @param exportReqVO 导出条件
     * @return Excel文件字节数组
     */
    byte[] exportQuestionnaireResultsToExcel(Object exportReqVO);

    /**
     * 获取问卷结果统计分析
     *
     * @param analysisReqVO 分析条件
     * @return 统计分析结果
     */
    QuestionnaireResultAnalysis getQuestionnaireResultAnalysis(Object analysisReqVO);

    /**
     * 检查问卷支持性
     *
     * @param questionnaireId 问卷ID
     * @return 支持性检查结果
     */
    QuestionnaireSupportabilityResult checkQuestionnaireSupportability(Long questionnaireId);

    /**
     * 优化结果数据关联查询
     *
     * @param resultIds 结果ID列表
     * @return 优化后的结果列表
     */
    List<QuestionnaireResultDO> getOptimizedQuestionnaireResults(List<Long> resultIds);

    /**
     * 问卷完成统计信息
     */
    class QuestionnaireCompletionStats {
        private Long totalResults;
        private Long completedResults;
        private Long pendingResults;
        private Double completionRate;
        private Double averageScore;
        private Long todayCompleted;
        private Long weekCompleted;
        private Long monthCompleted;

        // Getters and Setters
        public Long getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(Long totalResults) {
            this.totalResults = totalResults;
        }

        public Long getCompletedResults() {
            return completedResults;
        }

        public void setCompletedResults(Long completedResults) {
            this.completedResults = completedResults;
        }

        public Long getPendingResults() {
            return pendingResults;
        }

        public void setPendingResults(Long pendingResults) {
            this.pendingResults = pendingResults;
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

        public Long getTodayCompleted() {
            return todayCompleted;
        }

        public void setTodayCompleted(Long todayCompleted) {
            this.todayCompleted = todayCompleted;
        }

        public Long getWeekCompleted() {
            return weekCompleted;
        }

        public void setWeekCompleted(Long weekCompleted) {
            this.weekCompleted = weekCompleted;
        }

        public Long getMonthCompleted() {
            return monthCompleted;
        }

        public void setMonthCompleted(Long monthCompleted) {
            this.monthCompleted = monthCompleted;
        }
    }

    /**
     * 结果生成状态
     */
    class ResultGenerationStatus {
        private Integer status; // 1-待处理, 2-生成中, 3-已完成, 4-失败
        private String message;
        private Integer progress; // 进度百分比
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String errorMessage;

        // Getters and Setters
        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
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

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 批量生成结果
     */
    class BatchGenerationResult {
        private Integer totalCount;
        private Integer successCount;
        private Integer failureCount;
        private List<String> errorMessages;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public BatchGenerationResult() {
            this.errorMessages = new ArrayList<>();
            this.startTime = LocalDateTime.now();
        }

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
    }

    /**
     * 问卷结果统计分析
     */
    class QuestionnaireResultAnalysis {
        private Long totalResults;
        private Double averageScore;
        private Double maxScore;
        private Double minScore;
        private Map<Integer, Long> riskLevelDistribution;
        private Map<String, Double> dimensionAverages;
        private List<Map<String, Object>> trendData;
        private Map<String, Object> correlationAnalysis;

        public QuestionnaireResultAnalysis() {
            this.riskLevelDistribution = new HashMap<>();
            this.dimensionAverages = new HashMap<>();
            this.trendData = new ArrayList<>();
            this.correlationAnalysis = new HashMap<>();
        }

        // Getters and Setters
        public Long getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(Long totalResults) {
            this.totalResults = totalResults;
        }

        public Double getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(Double averageScore) {
            this.averageScore = averageScore;
        }

        public Double getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(Double maxScore) {
            this.maxScore = maxScore;
        }

        public Double getMinScore() {
            return minScore;
        }

        public void setMinScore(Double minScore) {
            this.minScore = minScore;
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

        public List<Map<String, Object>> getTrendData() {
            return trendData;
        }

        public void setTrendData(List<Map<String, Object>> trendData) {
            this.trendData = trendData;
        }

        public Map<String, Object> getCorrelationAnalysis() {
            return correlationAnalysis;
        }

        public void setCorrelationAnalysis(Map<String, Object> correlationAnalysis) {
            this.correlationAnalysis = correlationAnalysis;
        }
    }

    /**
     * 问卷支持性检查结果
     */
    class QuestionnaireSupportabilityResult {
        private boolean supportable;
        private String reason;
        private Integer supportabilityScore; // 支持性评分 0-100
        private List<String> issues;
        private List<String> recommendations;

        public QuestionnaireSupportabilityResult() {
            this.issues = new ArrayList<>();
            this.recommendations = new ArrayList<>();
        }

        // Getters and Setters
        public boolean isSupportable() {
            return supportable;
        }

        public void setSupportable(boolean supportable) {
            this.supportable = supportable;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Integer getSupportabilityScore() {
            return supportabilityScore;
        }

        public void setSupportabilityScore(Integer supportabilityScore) {
            this.supportabilityScore = supportabilityScore;
        }

        public List<String> getIssues() {
            return issues;
        }

        public void setIssues(List<String> issues) {
            this.issues = issues;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }
    }

}