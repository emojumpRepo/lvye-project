package cn.iocoder.yudao.module.psychology.service.assessment.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultStatisticsService;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultStatisticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 测评结果统计分析服务测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class AssessmentResultStatisticsServiceImplTest {

    @Mock
    private QuestionnaireResultMapper questionnaireResultMapper;

    @InjectMocks
    private AssessmentResultStatisticsServiceImpl statisticsService;

    @Test
    void testGetAssessmentTrendAnalysis_Success() {
        // 准备测试数据
        Long assessmentId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        
        List<QuestionnaireResultDO> results = Arrays.asList(
                createTestResult(1L, 75.0, 2, LocalDateTime.now().minusDays(20)),
                createTestResult(2L, 80.0, 2, LocalDateTime.now().minusDays(10)),
                createTestResult(3L, 85.0, 1, LocalDateTime.now().minusDays(5))
        );
        
        when(questionnaireResultMapper.selectResultsByTimeRange(null, assessmentId, startTime, endTime))
                .thenReturn(results);
        
        // 执行测试
        AssessmentResultStatisticsService.TrendAnalysisResult result = 
                statisticsService.getAssessmentTrendAnalysis(assessmentId, startTime, endTime);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getTrendData());
        assertEquals(3, result.getTrendData().size());
        assertNotNull(result.getTrendSummary());
        assertNotNull(result.getKeyInsights());
        assertFalse(result.getKeyInsights().isEmpty());
        assertNotNull(result.getOverallTrendSlope());
        assertNotNull(result.getTrendDirection());
        assertNotNull(result.getDimensionTrends());
        assertNotNull(result.getSeasonalPatterns());
        
        // 验证趋势方向（85-75=10，应该是上升趋势）
        assertTrue(result.getOverallTrendSlope() > 0);
        assertTrue(result.getTrendDirection().contains("上升") || result.getTrendDirection().contains("改善"));
        
        verify(questionnaireResultMapper).selectResultsByTimeRange(null, assessmentId, startTime, endTime);
    }

    @Test
    void testGetAssessmentTrendAnalysis_NoData() {
        // 准备测试数据
        Long assessmentId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        
        when(questionnaireResultMapper.selectResultsByTimeRange(null, assessmentId, startTime, endTime))
                .thenReturn(Arrays.asList());
        
        // 执行测试
        AssessmentResultStatisticsService.TrendAnalysisResult result = 
                statisticsService.getAssessmentTrendAnalysis(assessmentId, startTime, endTime);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getTrendData().isEmpty());
        assertTrue(result.getKeyInsights().contains("暂无数据"));
        assertEquals("无数据", result.getTrendDirection());
        
        verify(questionnaireResultMapper).selectResultsByTimeRange(null, assessmentId, startTime, endTime);
    }

    @Test
    void testGetRiskLevelDistribution_Success() {
        // 准备测试数据
        Long assessmentId = 1L;
        Integer timeRange = 30;
        
        List<QuestionnaireResultDO> results = Arrays.asList(
                createTestResult(1L, 85.0, 1, LocalDateTime.now()),
                createTestResult(2L, 75.0, 2, LocalDateTime.now()),
                createTestResult(3L, 65.0, 3, LocalDateTime.now()),
                createTestResult(4L, 80.0, 2, LocalDateTime.now())
        );
        
        when(questionnaireResultMapper.selectResultsByTimeRange(any(), eq(assessmentId), any(), any()))
                .thenReturn(results);
        
        // 执行测试
        AssessmentResultStatisticsService.RiskLevelDistribution result = 
                statisticsService.getRiskLevelDistribution(assessmentId, timeRange);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getDistribution());
        assertNotNull(result.getPercentages());
        assertNotNull(result.getMostCommonRiskLevel());
        assertNotNull(result.getAverageRiskLevel());
        assertNotNull(result.getTimeSeriesDistribution());
        assertNotNull(result.getComparisonWithPrevious());
        
        // 验证分布数据
        assertEquals(1L, result.getDistribution().get(1)); // 1个风险等级1
        assertEquals(2L, result.getDistribution().get(2)); // 2个风险等级2
        assertEquals(1L, result.getDistribution().get(3)); // 1个风险等级3
        
        // 验证最常见风险等级
        assertEquals(2, result.getMostCommonRiskLevel()); // 风险等级2最多
        
        // 验证平均风险等级
        assertEquals(2.0, result.getAverageRiskLevel(), 0.1); // (1+2+3+2)/4 = 2.0
        
        verify(questionnaireResultMapper).selectResultsByTimeRange(any(), eq(assessmentId), any(), any());
    }

    @Test
    void testGetAssessmentComparisonAnalysis_Success() {
        // 准备测试数据
        List<Long> assessmentIds = Arrays.asList(1L, 2L, 3L);
        Integer timeRange = 30;
        
        // 执行测试
        AssessmentResultStatisticsService.ComparisonAnalysisResult result = 
                statisticsService.getAssessmentComparisonAnalysis(assessmentIds, timeRange);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getAssessmentSummaries());
        assertEquals(3, result.getAssessmentSummaries().size());
        assertNotNull(result.getDimensionComparisons());
        assertNotNull(result.getStatisticalComparison());
        assertNotNull(result.getSignificantDifferences());
        assertNotNull(result.getCorrelationMatrix());
        assertNotNull(result.getComparisonSummary());
        assertFalse(result.getComparisonSummary().isEmpty());
        
        // 验证维度对比包含常见维度
        assertTrue(result.getDimensionComparisons().containsKey("anxiety"));
        assertTrue(result.getDimensionComparisons().containsKey("depression"));
        assertTrue(result.getDimensionComparisons().containsKey("stress"));
    }

    @Test
    void testGetAssessmentEffectivenessEvaluation_Success() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        Integer evaluationPeriod = 30;
        
        List<QuestionnaireResultDO> results = Arrays.asList(
                createTestResult(1L, 70.0, 3, LocalDateTime.now().minusDays(25)),
                createTestResult(2L, 75.0, 2, LocalDateTime.now().minusDays(15)),
                createTestResult(3L, 80.0, 2, LocalDateTime.now().minusDays(5))
        );
        
        when(questionnaireResultMapper.selectResultsByTimeRange(eq(studentProfileId), eq(assessmentId), any(), any()))
                .thenReturn(results);
        
        // 执行测试
        AssessmentResultStatisticsService.EffectivenessEvaluation result = 
                statisticsService.getAssessmentEffectivenessEvaluation(assessmentId, studentProfileId, evaluationPeriod);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getImprovementScore());
        assertNotNull(result.getImprovementLevel());
        assertNotNull(result.getDimensionImprovements());
        assertNotNull(result.getPositiveChanges());
        assertNotNull(result.getAreasNeedingAttention());
        assertNotNull(result.getProgressMetrics());
        assertNotNull(result.getEvaluationSummary());
        assertNotNull(result.getRecommendations());
        
        // 验证改善分数（80-70=10，应该是正值）
        assertTrue(result.getImprovementScore() > 0);
        assertTrue(result.getImprovementLevel().contains("改善"));
        assertFalse(result.getEvaluationSummary().isEmpty());
        
        verify(questionnaireResultMapper).selectResultsByTimeRange(eq(studentProfileId), eq(assessmentId), any(), any());
    }

    @Test
    void testGetAssessmentEffectivenessEvaluation_InsufficientData() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        Integer evaluationPeriod = 30;
        
        // 只有一个结果，数据不足
        List<QuestionnaireResultDO> results = Arrays.asList(
                createTestResult(1L, 75.0, 2, LocalDateTime.now())
        );
        
        when(questionnaireResultMapper.selectResultsByTimeRange(eq(studentProfileId), eq(assessmentId), any(), any()))
                .thenReturn(results);
        
        // 执行测试
        AssessmentResultStatisticsService.EffectivenessEvaluation result = 
                statisticsService.getAssessmentEffectivenessEvaluation(assessmentId, studentProfileId, evaluationPeriod);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0.0, result.getImprovementScore());
        assertEquals("数据不足", result.getImprovementLevel());
        assertTrue(result.getEvaluationSummary().contains("数据不足"));
        
        verify(questionnaireResultMapper).selectResultsByTimeRange(eq(studentProfileId), eq(assessmentId), any(), any());
    }

    @Test
    void testGetAssessmentVisualizationData_Success() {
        // 准备测试数据
        Object visualizationReqVO = new Object();
        
        // 执行测试
        AssessmentResultStatisticsService.VisualizationData result = 
                statisticsService.getAssessmentVisualizationData(visualizationReqVO);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getChartData());
        assertNotNull(result.getChartConfigs());
        assertNotNull(result.getDashboardWidgets());
        assertNotNull(result.getInteractiveElements());
        
        // 验证图表数据包含各种图表类型
        assertTrue(result.getChartData().containsKey("lineChart"));
        assertTrue(result.getChartData().containsKey("barChart"));
        assertTrue(result.getChartData().containsKey("pieChart"));
        assertTrue(result.getChartData().containsKey("heatmap"));
        
        // 验证仪表板组件
        assertFalse(result.getDashboardWidgets().isEmpty());
        
        // 验证交互元素
        assertTrue(result.getInteractiveElements().containsKey("filters"));
        assertTrue(result.getInteractiveElements().containsKey("drillDown"));
        assertTrue(result.getInteractiveElements().containsKey("export"));
    }

    @Test
    void testGenerateStatisticalReport_Success() {
        // 准备测试数据
        Object reportReqVO = new Object();
        
        // 执行测试
        AssessmentResultStatisticsService.StatisticalReport result = 
                statisticsService.generateStatisticalReport(reportReqVO);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getReportTitle());
        assertNotNull(result.getGenerationTime());
        assertNotNull(result.getExecutiveSummary());
        assertNotNull(result.getDetailedAnalysis());
        assertNotNull(result.getKeyFindings());
        assertNotNull(result.getRecommendations());
        assertNotNull(result.getAppendices());
        assertNotNull(result.getReportContent());
        
        // 验证报告标题
        assertEquals("心理健康测评统计分析报告", result.getReportTitle());
        
        // 验证执行摘要包含关键指标
        assertTrue(result.getExecutiveSummary().containsKey("totalAssessments"));
        assertTrue(result.getExecutiveSummary().containsKey("averageScore"));
        assertTrue(result.getExecutiveSummary().containsKey("improvementRate"));
        
        // 验证详细分析不为空
        assertFalse(result.getDetailedAnalysis().isEmpty());
        
        // 验证关键发现和建议不为空
        assertFalse(result.getKeyFindings().isEmpty());
        assertFalse(result.getRecommendations().isEmpty());
        
        // 验证报告内容包含标题
        assertTrue(result.getReportContent().contains(result.getReportTitle()));
        assertTrue(result.getReportContent().contains("执行摘要"));
        assertTrue(result.getReportContent().contains("关键发现"));
        assertTrue(result.getReportContent().contains("建议"));
    }

    private QuestionnaireResultDO createTestResult(Long id, Double score, Integer riskLevel, LocalDateTime time) {
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
        result.setSubmitTime(time);
        result.setGenerationTime(time);
        return result;
    }

}