package cn.iocoder.yudao.module.psychology.controller.admin.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultExtensionService;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultStatisticsService;
import cn.iocoder.yudao.module.psychology.service.assessment.CombinedAssessmentResultGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测评结果管理API控制器扩展测试
 *
 * @author 芋道源码
 */
@Import(AssessmentResultExtensionController.class)
class AssessmentResultExtensionControllerTest extends BaseDbUnitTest {

    @Resource
    private AssessmentResultExtensionController assessmentResultExtensionController;

    @MockBean
    private AssessmentResultExtensionService assessmentResultExtensionService;

    @MockBean
    private AssessmentResultStatisticsService assessmentResultStatisticsService;

    @MockBean
    private CombinedAssessmentResultGenerationService combinedAssessmentResultGenerationService;

    private MockMvc mockMvc;

    @Test
    void testGenerateCombinedAssessmentResult_Success() throws Exception {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        Long resultId = 1001L;
        
        when(assessmentResultExtensionService.generateCombinedAssessmentResult(eq(assessmentId), eq(studentProfileId), any()))
                .thenReturn(resultId);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/assessment-result-extension/generate-combined-result")
                .param("assessmentId", assessmentId.toString())
                .param("studentProfileId", studentProfileId.toString())
                .contentType("application/json")
                .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(resultId));

        // 验证调用
        verify(assessmentResultExtensionService).generateCombinedAssessmentResult(eq(assessmentId), eq(studentProfileId), any());
    }

    @Test
    void testCheckCompletionStatus_Success() throws Exception {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        
        AssessmentResultExtensionService.AssessmentCompletionStatus status = 
                new AssessmentResultExtensionService.AssessmentCompletionStatus();
        status.setCompleted(true);
        status.setTotalQuestionnaires(5);
        status.setCompletedQuestionnaires(5);
        status.setCompletionRate(100.0);
        status.setStatusMessage("测评已完成");
        
        when(assessmentResultExtensionService.checkAssessmentCompletionStatus(assessmentId, studentProfileId))
                .thenReturn(status);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/completion-status")
                .param("assessmentId", assessmentId.toString())
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.totalQuestionnaires").value(5))
                .andExpect(jsonPath("$.data.completedQuestionnaires").value(5))
                .andExpect(jsonPath("$.data.completionRate").value(100.0))
                .andExpect(jsonPath("$.data.statusMessage").value("测评已完成"));

        // 验证调用
        verify(assessmentResultExtensionService).checkAssessmentCompletionStatus(assessmentId, studentProfileId);
    }

    @Test
    void testGetUserAssessmentRecords_Success() throws Exception {
        // 准备测试数据
        Long studentProfileId = 100L;
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(new Object()));
        pageResult.setTotal(1L);
        
        when(assessmentResultExtensionService.getUserAssessmentRecords(eq(studentProfileId), any()))
                .thenReturn(pageResult);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/user-records")
                .param("studentProfileId", studentProfileId.toString())
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));

        // 验证调用
        verify(assessmentResultExtensionService).getUserAssessmentRecords(eq(studentProfileId), any());
    }

    @Test
    void testGetHistoryComparison_Success() throws Exception {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        Integer limit = 5;
        
        List<AssessmentResultExtensionService.AssessmentResultComparison> comparisons = Arrays.asList(
                createTestComparison(1L, 85.0, 2),
                createTestComparison(2L, 80.0, 3)
        );
        
        when(assessmentResultExtensionService.getHistoryAssessmentComparison(assessmentId, studentProfileId, limit))
                .thenReturn(comparisons);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/history-comparison")
                .param("assessmentId", assessmentId.toString())
                .param("studentProfileId", studentProfileId.toString())
                .param("limit", limit.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].totalScore").value(85.0))
                .andExpect(jsonPath("$.data[0].riskLevel").value(2));

        // 验证调用
        verify(assessmentResultExtensionService).getHistoryAssessmentComparison(assessmentId, studentProfileId, limit);
    }

    @Test
    void testGetTrendAnalysis_Success() throws Exception {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        
        AssessmentResultExtensionService.AssessmentTrendAnalysis analysis = 
                new AssessmentResultExtensionService.AssessmentTrendAnalysis();
        analysis.setOverallTrend(5.0);
        analysis.setTrendDescription("呈现显著上升趋势");
        
        when(assessmentResultExtensionService.getAssessmentTrendAnalysis(eq(assessmentId), eq(studentProfileId), any(), any()))
                .thenReturn(analysis);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/trend-analysis")
                .param("assessmentId", assessmentId.toString())
                .param("studentProfileId", studentProfileId.toString())
                .param("startTime", "2024-01-01 00:00:00")
                .param("endTime", "2024-01-31 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.overallTrend").value(5.0))
                .andExpect(jsonPath("$.data.trendDescription").value("呈现显著上升趋势"));

        // 验证调用
        verify(assessmentResultExtensionService).getAssessmentTrendAnalysis(eq(assessmentId), eq(studentProfileId), any(), any());
    }

    @Test
    void testBatchGenerateResults_Success() throws Exception {
        // 准备测试数据
        AssessmentResultExtensionService.BatchAssessmentGenerationResult batchResult = 
                new AssessmentResultExtensionService.BatchAssessmentGenerationResult();
        batchResult.setTotalCount(10);
        batchResult.setSuccessCount(8);
        batchResult.setFailureCount(2);
        batchResult.setSummary("批量生成完成，成功: 8, 失败: 2");
        
        when(assessmentResultExtensionService.batchGenerateAssessmentResults(any()))
                .thenReturn(batchResult);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/assessment-result-extension/batch-generate")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.successCount").value(8))
                .andExpect(jsonPath("$.data.failureCount").value(2))
                .andExpect(jsonPath("$.data.summary").value("批量生成完成，成功: 8, 失败: 2"));

        // 验证调用
        verify(assessmentResultExtensionService).batchGenerateAssessmentResults(any());
    }

    @Test
    void testGetAssessmentStatistics_Success() throws Exception {
        // 准备测试数据
        Long assessmentId = 1L;
        Integer timeRange = 30;
        
        AssessmentResultExtensionService.AssessmentResultStatistics statistics = 
                new AssessmentResultExtensionService.AssessmentResultStatistics();
        statistics.setTotalAssessments(100L);
        statistics.setCompletedAssessments(85L);
        statistics.setCompletionRate(85.0);
        statistics.setAverageScore(78.5);
        
        when(assessmentResultExtensionService.getAssessmentResultStatistics(assessmentId, timeRange))
                .thenReturn(statistics);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/statistics")
                .param("assessmentId", assessmentId.toString())
                .param("timeRange", timeRange.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalAssessments").value(100))
                .andExpect(jsonPath("$.data.completedAssessments").value(85))
                .andExpect(jsonPath("$.data.completionRate").value(85.0))
                .andExpect(jsonPath("$.data.averageScore").value(78.5));

        // 验证调用
        verify(assessmentResultExtensionService).getAssessmentResultStatistics(assessmentId, timeRange);
    }

    @Test
    void testGetDetailedTrendAnalysis_Success() throws Exception {
        // 准备测试数据
        Long assessmentId = 1L;
        
        AssessmentResultStatisticsService.TrendAnalysisResult result = 
                new AssessmentResultStatisticsService.TrendAnalysisResult();
        result.setOverallTrendSlope(2.5);
        result.setTrendDirection("轻微上升");
        
        when(assessmentResultStatisticsService.getAssessmentTrendAnalysis(eq(assessmentId), any(), any()))
                .thenReturn(result);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/detailed-trend-analysis")
                .param("assessmentId", assessmentId.toString())
                .param("startTime", "2024-01-01 00:00:00")
                .param("endTime", "2024-01-31 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.overallTrendSlope").value(2.5))
                .andExpect(jsonPath("$.data.trendDirection").value("轻微上升"));

        // 验证调用
        verify(assessmentResultStatisticsService).getAssessmentTrendAnalysis(eq(assessmentId), any(), any());
    }

    @Test
    void testGetRiskDistribution_Success() throws Exception {
        // 准备测试数据
        Long assessmentId = 1L;
        Integer timeRange = 30;
        
        AssessmentResultStatisticsService.RiskLevelDistribution distribution = 
                new AssessmentResultStatisticsService.RiskLevelDistribution();
        distribution.setMostCommonRiskLevel(2);
        distribution.setAverageRiskLevel(2.3);
        
        when(assessmentResultStatisticsService.getRiskLevelDistribution(assessmentId, timeRange))
                .thenReturn(distribution);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/risk-distribution")
                .param("assessmentId", assessmentId.toString())
                .param("timeRange", timeRange.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mostCommonRiskLevel").value(2))
                .andExpect(jsonPath("$.data.averageRiskLevel").value(2.3));

        // 验证调用
        verify(assessmentResultStatisticsService).getRiskLevelDistribution(assessmentId, timeRange);
    }

    @Test
    void testGetComparisonAnalysis_Success() throws Exception {
        // 准备测试数据
        List<Long> assessmentIds = Arrays.asList(1L, 2L, 3L);
        Integer timeRange = 30;
        
        AssessmentResultStatisticsService.ComparisonAnalysisResult result = 
                new AssessmentResultStatisticsService.ComparisonAnalysisResult();
        result.setComparisonSummary("对比分析显示各测评间存在显著差异");
        
        when(assessmentResultStatisticsService.getAssessmentComparisonAnalysis(assessmentIds, timeRange))
                .thenReturn(result);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/assessment-result-extension/comparison-analysis")
                .param("timeRange", timeRange.toString())
                .contentType("application/json")
                .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.comparisonSummary").value("对比分析显示各测评间存在显著差异"));

        // 验证调用
        verify(assessmentResultStatisticsService).getAssessmentComparisonAnalysis(assessmentIds, timeRange);
    }

    @Test
    void testGetEffectivenessEvaluation_Success() throws Exception {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        Integer evaluationPeriod = 30;
        
        AssessmentResultStatisticsService.EffectivenessEvaluation evaluation = 
                new AssessmentResultStatisticsService.EffectivenessEvaluation();
        evaluation.setImprovementScore(8.5);
        evaluation.setImprovementLevel("明显改善");
        evaluation.setEvaluationSummary("评估期间整体改善分数为8.5，改善水平为明显改善");
        
        when(assessmentResultStatisticsService.getAssessmentEffectivenessEvaluation(assessmentId, studentProfileId, evaluationPeriod))
                .thenReturn(evaluation);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/effectiveness-evaluation")
                .param("assessmentId", assessmentId.toString())
                .param("studentProfileId", studentProfileId.toString())
                .param("evaluationPeriod", evaluationPeriod.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.improvementScore").value(8.5))
                .andExpect(jsonPath("$.data.improvementLevel").value("明显改善"))
                .andExpect(jsonPath("$.data.evaluationSummary").value("评估期间整体改善分数为8.5，改善水平为明显改善"));

        // 验证调用
        verify(assessmentResultStatisticsService).getAssessmentEffectivenessEvaluation(assessmentId, studentProfileId, evaluationPeriod);
    }

    @Test
    void testGetVisualizationData_Success() throws Exception {
        // 准备测试数据
        AssessmentResultStatisticsService.VisualizationData data = 
                new AssessmentResultStatisticsService.VisualizationData();
        
        when(assessmentResultStatisticsService.getAssessmentVisualizationData(any()))
                .thenReturn(data);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/assessment-result-extension/visualization-data")
                .param("assessmentId", "1")
                .param("chartType", "line")
                .param("timeRange", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 验证调用
        verify(assessmentResultStatisticsService).getAssessmentVisualizationData(any());
    }

    @Test
    void testGenerateStatisticalReport_Success() throws Exception {
        // 准备测试数据
        AssessmentResultStatisticsService.StatisticalReport report = 
                new AssessmentResultStatisticsService.StatisticalReport();
        report.setReportTitle("心理健康测评统计分析报告");
        report.setGenerationTime(LocalDateTime.now());
        
        when(assessmentResultStatisticsService.generateStatisticalReport(any()))
                .thenReturn(report);

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentResultExtensionController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/assessment-result-extension/generate-report")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reportTitle").value("心理健康测评统计分析报告"));

        // 验证调用
        verify(assessmentResultStatisticsService).generateStatisticalReport(any());
    }

    private AssessmentResultExtensionService.AssessmentResultComparison createTestComparison(Long resultId, Double score, Integer riskLevel) {
        AssessmentResultExtensionService.AssessmentResultComparison comparison = 
                new AssessmentResultExtensionService.AssessmentResultComparison();
        comparison.setResultId(resultId);
        comparison.setTotalScore(score);
        comparison.setRiskLevel(riskLevel);
        comparison.setAssessmentTime(LocalDateTime.now());
        comparison.setScoreChange(5.0);
        comparison.setRiskLevelChange(-1);
        comparison.setChangeDescription("显著改善");
        return comparison;
    }

}