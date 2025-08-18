package cn.iocoder.yudao.module.psychology.service.assessment.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.service.assessment.CombinedAssessmentResultGenerationService;
import cn.iocoder.yudao.module.psychology.service.assessment.CombinedAssessmentResultGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 组合测评结果生成服务测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class CombinedAssessmentResultGenerationServiceImplTest {

    @InjectMocks
    private CombinedAssessmentResultGenerationServiceImpl generationService;

    @Test
    void testGenerateCombinedResult_Success() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        List<QuestionnaireResultDO> questionnaireResults = Arrays.asList(
                createTestQuestionnaireResult(1L, 85.0, 2),
                createTestQuestionnaireResult(2L, 78.0, 3),
                createTestQuestionnaireResult(3L, 92.0, 1)
        );
        
        // 执行测试
        CombinedAssessmentResultGenerationService.CombinedAssessmentResult result = 
                generationService.generateCombinedResult(assessmentId, studentProfileId, questionnaireResults);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(assessmentId, result.getAssessmentId());
        assertEquals(studentProfileId, result.getStudentProfileId());
        assertNotNull(result.getOverallScore());
        assertNotNull(result.getOverallRiskLevel());
        assertNotNull(result.getDimensionScores());
        assertNotNull(result.getAggregatedResult());
        assertNotNull(result.getAnalysisResult());
        assertNotNull(result.getRiskAssessment());
        assertNotNull(result.getInterventionRecommendations());
        assertNotNull(result.getComprehensiveReport());
        assertNotNull(result.getMetadata());
        
        // 验证聚合结果
        assertEquals(3, result.getAggregatedResult().getTotalQuestionnaires());
        assertTrue(result.getAggregatedResult().getWeightedAverageScore() > 0);
        
        // 验证综合报告包含关键信息
        String report = result.getComprehensiveReport();
        assertTrue(report.contains("心理健康综合测评报告"));
        assertTrue(report.contains("基本信息"));
        assertTrue(report.contains("各维度得分"));
        assertTrue(report.contains("风险评估"));
        assertTrue(report.contains("干预建议"));
    }

    @Test
    void testGenerateCombinedResult_EmptyResults() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        List<QuestionnaireResultDO> questionnaireResults = Arrays.asList();
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            generationService.generateCombinedResult(assessmentId, studentProfileId, questionnaireResults);
        });
    }

    @Test
    void testAggregateQuestionnaireResults_Success() {
        // 准备测试数据
        List<QuestionnaireResultDO> questionnaireResults = Arrays.asList(
                createTestQuestionnaireResult(1L, 85.0, 2),
                createTestQuestionnaireResult(2L, 78.0, 3),
                createTestQuestionnaireResult(3L, 92.0, 1)
        );
        
        // 执行测试
        CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult result = 
                generationService.aggregateQuestionnaireResults(questionnaireResults);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.getTotalQuestionnaires());
        assertNotNull(result.getWeightedAverageScore());
        assertTrue(result.getWeightedAverageScore() > 0);
        assertNotNull(result.getDimensionAverages());
        assertFalse(result.getDimensionAverages().isEmpty());
        assertNotNull(result.getRiskLevelCounts());
        assertNotNull(result.getSignificantFindings());
        assertNotNull(result.getStatisticalSummary());
        
        // 验证统计摘要包含必要字段
        assertTrue(result.getStatisticalSummary().containsKey("scoreStandardDeviation"));
        assertTrue(result.getStatisticalSummary().containsKey("consistencyIndex"));
        assertTrue(result.getStatisticalSummary().containsKey("completionRate"));
    }

    @Test
    void testAggregateQuestionnaireResults_EmptyList() {
        // 准备测试数据
        List<QuestionnaireResultDO> questionnaireResults = Arrays.asList();
        
        // 执行测试
        CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult result = 
                generationService.aggregateQuestionnaireResults(questionnaireResults);
        
        // 验证结果
        assertNotNull(result);
        assertNull(result.getTotalQuestionnaires());
    }

    @Test
    void testPerformComprehensiveAnalysis_Success() {
        // 准备测试数据
        CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult aggregatedResult = 
                createTestAggregatedResult();
        
        // 执行测试
        CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult result = 
                generationService.performComprehensiveAnalysis(aggregatedResult);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getCorrelationMatrix());
        assertNotNull(result.getPatternAnalysis());
        assertFalse(result.getPatternAnalysis().isEmpty());
        assertNotNull(result.getPsychologicalProfile());
        assertNotNull(result.getStrengthsIdentified());
        assertNotNull(result.getAreasOfConcern());
        assertNotNull(result.getConsistencyScore());
        assertTrue(result.getConsistencyScore() >= 0 && result.getConsistencyScore() <= 1);
    }

    @Test
    void testAssessRisk_Success() {
        // 准备测试数据
        CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult analysisResult = 
                createTestAnalysisResult();
        
        // 执行测试
        CombinedAssessmentResultGenerationService.RiskAssessmentResult result = 
                generationService.assessRisk(analysisResult);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOverallRiskLevel());
        assertTrue(result.getOverallRiskLevel() >= 1 && result.getOverallRiskLevel() <= 5);
        assertNotNull(result.getDimensionRiskLevels());
        assertNotNull(result.getRiskFactors());
        assertNotNull(result.getProtectiveFactors());
        assertNotNull(result.getRiskSummary());
        assertFalse(result.getRiskSummary().isEmpty());
        assertNotNull(result.getRequiresImmediateAttention());
    }

    @Test
    void testGenerateInterventionRecommendations_Success() {
        // 准备测试数据
        CombinedAssessmentResultGenerationService.RiskAssessmentResult riskAssessment = 
                createTestRiskAssessment();
        CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult analysisResult = 
                createTestAnalysisResult();
        
        // 执行测试
        CombinedAssessmentResultGenerationService.InterventionRecommendations result = 
                generationService.generateInterventionRecommendations(riskAssessment, analysisResult);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getImmediateActions());
        assertNotNull(result.getShortTermGoals());
        assertNotNull(result.getLongTermGoals());
        assertNotNull(result.getDimensionSpecificRecommendations());
        assertNotNull(result.getResourceRecommendations());
        assertNotNull(result.getFollowUpPlan());
        assertFalse(result.getFollowUpPlan().isEmpty());
        
        // 验证维度特定建议包含常见维度
        assertTrue(result.getDimensionSpecificRecommendations().containsKey("anxiety"));
        assertTrue(result.getDimensionSpecificRecommendations().containsKey("depression"));
        assertTrue(result.getDimensionSpecificRecommendations().containsKey("stress"));
    }

    @Test
    void testGenerateInterventionRecommendations_HighRisk() {
        // 准备测试数据 - 高风险情况
        CombinedAssessmentResultGenerationService.RiskAssessmentResult riskAssessment = 
                createHighRiskAssessment();
        CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult analysisResult = 
                createTestAnalysisResult();
        
        // 执行测试
        CombinedAssessmentResultGenerationService.InterventionRecommendations result = 
                generationService.generateInterventionRecommendations(riskAssessment, analysisResult);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.getImmediateActions().isEmpty());
        
        // 高风险情况应该包含紧急行动建议
        assertTrue(result.getImmediateActions().stream()
                .anyMatch(action -> action.contains("专业心理咨询师") || action.contains("支持网络")));
        
        // 资源推荐应该包含紧急资源
        assertTrue(result.getResourceRecommendations().stream()
                .anyMatch(resource -> resource.contains("紧急") || resource.contains("危机")));
    }

    @Test
    void testGenerateComprehensiveReport_Success() {
        // 准备测试数据
        CombinedAssessmentResultGenerationService.CombinedAssessmentResult combinedResult = 
                createTestCombinedResult();
        
        // 执行测试
        String report = generationService.generateComprehensiveReport(combinedResult);
        
        // 验证结果
        assertNotNull(report);
        assertFalse(report.isEmpty());
        
        // 验证报告包含必要的章节
        assertTrue(report.contains("# 心理健康综合测评报告"));
        assertTrue(report.contains("## 基本信息"));
        assertTrue(report.contains("## 各维度得分"));
        assertTrue(report.contains("## 风险评估"));
        assertTrue(report.contains("## 干预建议"));
        assertTrue(report.contains("### 立即行动建议"));
        assertTrue(report.contains("### 短期目标"));
        assertTrue(report.contains("### 长期目标"));
        assertTrue(report.contains("## 随访计划"));
        
        // 验证报告包含具体数据
        assertTrue(report.contains("测评ID: 1"));
        assertTrue(report.contains("学生档案ID: 100"));
        assertTrue(report.contains("整体评分: 85.0"));
        assertTrue(report.contains("风险等级: 2"));
    }

    private QuestionnaireResultDO createTestQuestionnaireResult(Long id, Double score, Integer riskLevel) {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(id);
        result.setQuestionnaireId(id);
        result.setStudentProfileId(100L);
        result.setAnswerData("{\"q1\":\"A\",\"q2\":\"B\"}");
        result.setTotalScore(score);
        result.setDimensionScores("{\"anxiety\":75,\"depression\":80,\"stress\":77.5}");
        result.setRiskLevel(riskLevel);
        result.setResultContent("测试结果内容");
        result.setSuggestions("测试建议");
        result.setGenerationStatus(3);
        result.setSubmitTime(LocalDateTime.now());
        result.setGenerationTime(LocalDateTime.now());
        return result;
    }

    private CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult createTestAggregatedResult() {
        CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult result = 
                new CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult();
        result.setTotalQuestionnaires(3);
        result.setWeightedAverageScore(85.0);
        
        // 设置统计摘要
        result.setStatisticalSummary(new java.util.HashMap<String, Object>() {{
            put("consistencyIndex", 0.8);
        }});
        
        return result;
    }

    private CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult createTestAnalysisResult() {
        CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult result = 
                new CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult();
        result.setConsistencyScore(0.8);
        result.setStrengthsIdentified(Arrays.asList("anxiety维度表现优秀"));
        result.setAreasOfConcern(Arrays.asList("stress维度需要关注"));
        
        // 设置心理档案
        result.setPsychologicalProfile(new java.util.HashMap<String, Object>() {{
            put("overallWellbeing", 75.0);
        }});
        
        return result;
    }

    private CombinedAssessmentResultGenerationService.RiskAssessmentResult createTestRiskAssessment() {
        CombinedAssessmentResultGenerationService.RiskAssessmentResult result = 
                new CombinedAssessmentResultGenerationService.RiskAssessmentResult();
        result.setOverallRiskLevel(2);
        result.setRequiresImmediateAttention(false);
        result.setRiskFactors(Arrays.asList("stress维度需要关注"));
        result.setProtectiveFactors(Arrays.asList("anxiety维度表现优秀"));
        result.setRiskSummary("存在轻微风险，建议持续关注");
        return result;
    }

    private CombinedAssessmentResultGenerationService.RiskAssessmentResult createHighRiskAssessment() {
        CombinedAssessmentResultGenerationService.RiskAssessmentResult result = 
                new CombinedAssessmentResultGenerationService.RiskAssessmentResult();
        result.setOverallRiskLevel(4);
        result.setRequiresImmediateAttention(true);
        result.setRiskFactors(Arrays.asList("严重抑郁倾向", "高风险行为"));
        result.setProtectiveFactors(Arrays.asList());
        result.setRiskSummary("存在较高风险，需要及时干预");
        return result;
    }

    private CombinedAssessmentResultGenerationService.CombinedAssessmentResult createTestCombinedResult() {
        CombinedAssessmentResultGenerationService.CombinedAssessmentResult result = 
                new CombinedAssessmentResultGenerationService.CombinedAssessmentResult();
        result.setAssessmentId(1L);
        result.setStudentProfileId(100L);
        result.setOverallScore(85.0);
        result.setOverallRiskLevel(2);
        
        // 设置维度得分
        result.setDimensionScores(new java.util.HashMap<String, Double>() {{
            put("anxiety", 75.0);
            put("depression", 80.0);
            put("stress", 77.5);
        }});
        
        // 设置聚合结果
        result.setAggregatedResult(createTestAggregatedResult());
        
        // 设置风险评估
        result.setRiskAssessment(createTestRiskAssessment());
        
        // 设置干预建议
        CombinedAssessmentResultGenerationService.InterventionRecommendations recommendations = 
                new CombinedAssessmentResultGenerationService.InterventionRecommendations();
        recommendations.setImmediateActions(Arrays.asList("开始自我监测"));
        recommendations.setShortTermGoals(Arrays.asList("建立规律的作息时间"));
        recommendations.setLongTermGoals(Arrays.asList("提升整体心理韧性"));
        recommendations.setFollowUpPlan("建议2-4周内进行复评");
        result.setInterventionRecommendations(recommendations);
        
        return result;
    }

}