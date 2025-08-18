package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;

import java.util.List;
import java.util.Map;

/**
 * 组合测评结果生成服务接口
 *
 * @author 芋道源码
 */
public interface CombinedAssessmentResultGenerationService {

    /**
     * 生成组合测评结果
     *
     * @param assessmentId 测评ID
     * @param studentProfileId 学生档案ID
     * @param questionnaireResults 问卷结果列表
     * @return 组合测评结果
     */
    CombinedAssessmentResult generateCombinedResult(Long assessmentId, Long studentProfileId, 
                                                   List<QuestionnaireResultDO> questionnaireResults);

    /**
     * 聚合多问卷结果
     *
     * @param questionnaireResults 问卷结果列表
     * @return 聚合结果
     */
    AggregatedQuestionnaireResult aggregateQuestionnaireResults(List<QuestionnaireResultDO> questionnaireResults);

    /**
     * 综合分析算法
     *
     * @param aggregatedResult 聚合结果
     * @return 综合分析结果
     */
    ComprehensiveAnalysisResult performComprehensiveAnalysis(AggregatedQuestionnaireResult aggregatedResult);

    /**
     * 风险评估
     *
     * @param analysisResult 综合分析结果
     * @return 风险评估结果
     */
    RiskAssessmentResult assessRisk(ComprehensiveAnalysisResult analysisResult);

    /**
     * 生成干预建议
     *
     * @param riskAssessment 风险评估结果
     * @param analysisResult 综合分析结果
     * @return 干预建议
     */
    InterventionRecommendations generateInterventionRecommendations(RiskAssessmentResult riskAssessment, 
                                                                   ComprehensiveAnalysisResult analysisResult);

    /**
     * 生成综合报告
     *
     * @param combinedResult 组合测评结果
     * @return 综合报告内容
     */
    String generateComprehensiveReport(CombinedAssessmentResult combinedResult);

    /**
     * 组合测评结果
     */
    class CombinedAssessmentResult {
        private Long assessmentId;
        private Long studentProfileId;
        private Double overallScore;
        private Integer overallRiskLevel;
        private Map<String, Double> dimensionScores;
        private AggregatedQuestionnaireResult aggregatedResult;
        private ComprehensiveAnalysisResult analysisResult;
        private RiskAssessmentResult riskAssessment;
        private InterventionRecommendations interventionRecommendations;
        private String comprehensiveReport;
        private Map<String, Object> metadata;

        // Getters and Setters
        public Long getAssessmentId() {
            return assessmentId;
        }

        public void setAssessmentId(Long assessmentId) {
            this.assessmentId = assessmentId;
        }

        public Long getStudentProfileId() {
            return studentProfileId;
        }

        public void setStudentProfileId(Long studentProfileId) {
            this.studentProfileId = studentProfileId;
        }

        public Double getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(Double overallScore) {
            this.overallScore = overallScore;
        }

        public Integer getOverallRiskLevel() {
            return overallRiskLevel;
        }

        public void setOverallRiskLevel(Integer overallRiskLevel) {
            this.overallRiskLevel = overallRiskLevel;
        }

        public Map<String, Double> getDimensionScores() {
            return dimensionScores;
        }

        public void setDimensionScores(Map<String, Double> dimensionScores) {
            this.dimensionScores = dimensionScores;
        }

        public AggregatedQuestionnaireResult getAggregatedResult() {
            return aggregatedResult;
        }

        public void setAggregatedResult(AggregatedQuestionnaireResult aggregatedResult) {
            this.aggregatedResult = aggregatedResult;
        }

        public ComprehensiveAnalysisResult getAnalysisResult() {
            return analysisResult;
        }

        public void setAnalysisResult(ComprehensiveAnalysisResult analysisResult) {
            this.analysisResult = analysisResult;
        }

        public RiskAssessmentResult getRiskAssessment() {
            return riskAssessment;
        }

        public void setRiskAssessment(RiskAssessmentResult riskAssessment) {
            this.riskAssessment = riskAssessment;
        }

        public InterventionRecommendations getInterventionRecommendations() {
            return interventionRecommendations;
        }

        public void setInterventionRecommendations(InterventionRecommendations interventionRecommendations) {
            this.interventionRecommendations = interventionRecommendations;
        }

        public String getComprehensiveReport() {
            return comprehensiveReport;
        }

        public void setComprehensiveReport(String comprehensiveReport) {
            this.comprehensiveReport = comprehensiveReport;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * 聚合问卷结果
     */
    class AggregatedQuestionnaireResult {
        private Integer totalQuestionnaires;
        private Double weightedAverageScore;
        private Map<String, Double> dimensionAverages;
        private Map<String, Integer> riskLevelCounts;
        private List<String> significantFindings;
        private Map<String, Object> statisticalSummary;

        // Getters and Setters
        public Integer getTotalQuestionnaires() {
            return totalQuestionnaires;
        }

        public void setTotalQuestionnaires(Integer totalQuestionnaires) {
            this.totalQuestionnaires = totalQuestionnaires;
        }

        public Double getWeightedAverageScore() {
            return weightedAverageScore;
        }

        public void setWeightedAverageScore(Double weightedAverageScore) {
            this.weightedAverageScore = weightedAverageScore;
        }

        public Map<String, Double> getDimensionAverages() {
            return dimensionAverages;
        }

        public void setDimensionAverages(Map<String, Double> dimensionAverages) {
            this.dimensionAverages = dimensionAverages;
        }

        public Map<String, Integer> getRiskLevelCounts() {
            return riskLevelCounts;
        }

        public void setRiskLevelCounts(Map<String, Integer> riskLevelCounts) {
            this.riskLevelCounts = riskLevelCounts;
        }

        public List<String> getSignificantFindings() {
            return significantFindings;
        }

        public void setSignificantFindings(List<String> significantFindings) {
            this.significantFindings = significantFindings;
        }

        public Map<String, Object> getStatisticalSummary() {
            return statisticalSummary;
        }

        public void setStatisticalSummary(Map<String, Object> statisticalSummary) {
            this.statisticalSummary = statisticalSummary;
        }
    }

    /**
     * 综合分析结果
     */
    class ComprehensiveAnalysisResult {
        private Map<String, Double> correlationMatrix;
        private List<String> patternAnalysis;
        private Map<String, Object> psychologicalProfile;
        private List<String> strengthsIdentified;
        private List<String> areasOfConcern;
        private Double consistencyScore;

        // Getters and Setters
        public Map<String, Double> getCorrelationMatrix() {
            return correlationMatrix;
        }

        public void setCorrelationMatrix(Map<String, Double> correlationMatrix) {
            this.correlationMatrix = correlationMatrix;
        }

        public List<String> getPatternAnalysis() {
            return patternAnalysis;
        }

        public void setPatternAnalysis(List<String> patternAnalysis) {
            this.patternAnalysis = patternAnalysis;
        }

        public Map<String, Object> getPsychologicalProfile() {
            return psychologicalProfile;
        }

        public void setPsychologicalProfile(Map<String, Object> psychologicalProfile) {
            this.psychologicalProfile = psychologicalProfile;
        }

        public List<String> getStrengthsIdentified() {
            return strengthsIdentified;
        }

        public void setStrengthsIdentified(List<String> strengthsIdentified) {
            this.strengthsIdentified = strengthsIdentified;
        }

        public List<String> getAreasOfConcern() {
            return areasOfConcern;
        }

        public void setAreasOfConcern(List<String> areasOfConcern) {
            this.areasOfConcern = areasOfConcern;
        }

        public Double getConsistencyScore() {
            return consistencyScore;
        }

        public void setConsistencyScore(Double consistencyScore) {
            this.consistencyScore = consistencyScore;
        }
    }

    /**
     * 风险评估结果
     */
    class RiskAssessmentResult {
        private Integer overallRiskLevel;
        private Map<String, Integer> dimensionRiskLevels;
        private List<String> riskFactors;
        private List<String> protectiveFactors;
        private String riskSummary;
        private Boolean requiresImmediateAttention;

        // Getters and Setters
        public Integer getOverallRiskLevel() {
            return overallRiskLevel;
        }

        public void setOverallRiskLevel(Integer overallRiskLevel) {
            this.overallRiskLevel = overallRiskLevel;
        }

        public Map<String, Integer> getDimensionRiskLevels() {
            return dimensionRiskLevels;
        }

        public void setDimensionRiskLevels(Map<String, Integer> dimensionRiskLevels) {
            this.dimensionRiskLevels = dimensionRiskLevels;
        }

        public List<String> getRiskFactors() {
            return riskFactors;
        }

        public void setRiskFactors(List<String> riskFactors) {
            this.riskFactors = riskFactors;
        }

        public List<String> getProtectiveFactors() {
            return protectiveFactors;
        }

        public void setProtectiveFactors(List<String> protectiveFactors) {
            this.protectiveFactors = protectiveFactors;
        }

        public String getRiskSummary() {
            return riskSummary;
        }

        public void setRiskSummary(String riskSummary) {
            this.riskSummary = riskSummary;
        }

        public Boolean getRequiresImmediateAttention() {
            return requiresImmediateAttention;
        }

        public void setRequiresImmediateAttention(Boolean requiresImmediateAttention) {
            this.requiresImmediateAttention = requiresImmediateAttention;
        }
    }

    /**
     * 干预建议
     */
    class InterventionRecommendations {
        private List<String> immediateActions;
        private List<String> shortTermGoals;
        private List<String> longTermGoals;
        private Map<String, List<String>> dimensionSpecificRecommendations;
        private List<String> resourceRecommendations;
        private String followUpPlan;

        // Getters and Setters
        public List<String> getImmediateActions() {
            return immediateActions;
        }

        public void setImmediateActions(List<String> immediateActions) {
            this.immediateActions = immediateActions;
        }

        public List<String> getShortTermGoals() {
            return shortTermGoals;
        }

        public void setShortTermGoals(List<String> shortTermGoals) {
            this.shortTermGoals = shortTermGoals;
        }

        public List<String> getLongTermGoals() {
            return longTermGoals;
        }

        public void setLongTermGoals(List<String> longTermGoals) {
            this.longTermGoals = longTermGoals;
        }

        public Map<String, List<String>> getDimensionSpecificRecommendations() {
            return dimensionSpecificRecommendations;
        }

        public void setDimensionSpecificRecommendations(Map<String, List<String>> dimensionSpecificRecommendations) {
            this.dimensionSpecificRecommendations = dimensionSpecificRecommendations;
        }

        public List<String> getResourceRecommendations() {
            return resourceRecommendations;
        }

        public void setResourceRecommendations(List<String> resourceRecommendations) {
            this.resourceRecommendations = resourceRecommendations;
        }

        public String getFollowUpPlan() {
            return followUpPlan;
        }

        public void setFollowUpPlan(String followUpPlan) {
            this.followUpPlan = followUpPlan;
        }
    }

}