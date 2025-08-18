package cn.iocoder.yudao.module.psychology.service.assessment.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorFactory;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorStrategy;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultExtensionService;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultExtensionServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * 测评结果扩展服务测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class AssessmentResultExtensionServiceImplTest {

    @Mock
    private QuestionnaireResultMapper questionnaireResultMapper;

    @Mock
    private ResultGeneratorFactory resultGeneratorFactory;

    @Mock
    private ResultGeneratorStrategy resultGeneratorStrategy;

    @InjectMocks
    private AssessmentResultExtensionServiceImpl extensionService;

    @Test
    void testGenerateCombinedAssessmentResult_Success() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        List<Long> questionnaireResultIds = Arrays.asList(1L, 2L, 3L);
        
        List<QuestionnaireResultDO> questionnaireResults = Arrays.asList(
                createTestQuestionnaireResult(1L, studentProfileId),
                createTestQuestionnaireResult(2L, studentProfileId),
                createTestQuestionnaireResult(3L, studentProfileId)
        );
        
        ResultGenerationContext generatedContext = new ResultGenerationContext();
        generatedContext.setTotalScore(85.5);
        generatedContext.setRiskLevel(2);
        
        when(questionnaireResultMapper.selectBatchIds(questionnaireResultIds)).thenReturn(questionnaireResults);
        when(resultGeneratorFactory.getGenerator(999)).thenReturn(resultGeneratorStrategy);
        when(resultGeneratorStrategy.generateResult(any(ResultGenerationContext.class))).thenReturn(generatedContext);
        
        // 执行测试
        Long resultId = extensionService.generateCombinedAssessmentResult(assessmentId, studentProfileId, questionnaireResultIds);
        
        // 验证结果
        assertNotNull(resultId);
        verify(questionnaireResultMapper).selectBatchIds(questionnaireResultIds);
        verify(resultGeneratorFactory).getGenerator(999);
        verify(resultGeneratorStrategy).generateResult(any(ResultGenerationContext.class));
    }

    @Test
    void testGenerateCombinedAssessmentResult_NoResults() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        List<Long> questionnaireResultIds = Arrays.asList(1L, 2L, 3L);
        
        when(questionnaireResultMapper.selectBatchIds(questionnaireResultIds)).thenReturn(Arrays.asList());
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            extensionService.generateCombinedAssessmentResult(assessmentId, studentProfileId, questionnaireResultIds);
        });
        
        verify(questionnaireResultMapper).selectBatchIds(questionnaireResultIds);
        verify(resultGeneratorFactory, never()).getGenerator(anyInt());
    }

    @Test
    void testGenerateCombinedAssessmentResult_DifferentStudents() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        List<Long> questionnaireResultIds = Arrays.asList(1L, 2L, 3L);
        
        List<QuestionnaireResultDO> questionnaireResults = Arrays.asList(
                createTestQuestionnaireResult(1L, studentProfileId),
                createTestQuestionnaireResult(2L, 200L), // 不同的学生
                createTestQuestionnaireResult(3L, studentProfileId)
        );
        
        when(questionnaireResultMapper.selectBatchIds(questionnaireResultIds)).thenReturn(questionnaireResults);
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            extensionService.generateCombinedAssessmentResult(assessmentId, studentProfileId, questionnaireResultIds);
        });
        
        verify(questionnaireResultMapper).selectBatchIds(questionnaireResultIds);
        verify(resultGeneratorFactory, never()).getGenerator(anyInt());
    }

    @Test
    void testCheckAssessmentCompletionStatus_Completed() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        
        List<QuestionnaireResultDO> allResults = Arrays.asList(
                createCompletedQuestionnaireResult(1L, studentProfileId),
                createCompletedQuestionnaireResult(2L, studentProfileId),
                createCompletedQuestionnaireResult(3L, studentProfileId)
        );
        
        when(questionnaireResultMapper.selectListByStudentAndAssessment(studentProfileId, assessmentId))
                .thenReturn(allResults);
        
        // 执行测试
        AssessmentResultExtensionService.AssessmentCompletionStatus status = 
                extensionService.checkAssessmentCompletionStatus(assessmentId, studentProfileId);
        
        // 验证结果
        assertNotNull(status);
        assertTrue(status.isCompleted());
        assertEquals(3, status.getTotalQuestionnaires());
        assertEquals(3, status.getCompletedQuestionnaires());
        assertEquals(100.0, status.getCompletionRate());
        assertTrue(status.getPendingQuestionnaires().isEmpty());
        assertEquals("测评已完成", status.getStatusMessage());
    }

    @Test
    void testCheckAssessmentCompletionStatus_Partial() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        
        List<QuestionnaireResultDO> allResults = Arrays.asList(
                createCompletedQuestionnaireResult(1L, studentProfileId),
                createPendingQuestionnaireResult(2L, studentProfileId),
                createCompletedQuestionnaireResult(3L, studentProfileId)
        );
        
        when(questionnaireResultMapper.selectListByStudentAndAssessment(studentProfileId, assessmentId))
                .thenReturn(allResults);
        
        // 执行测试
        AssessmentResultExtensionService.AssessmentCompletionStatus status = 
                extensionService.checkAssessmentCompletionStatus(assessmentId, studentProfileId);
        
        // 验证结果
        assertNotNull(status);
        assertFalse(status.isCompleted());
        assertEquals(3, status.getTotalQuestionnaires());
        assertEquals(2, status.getCompletedQuestionnaires());
        assertEquals(66.66666666666667, status.getCompletionRate(), 0.01);
        assertEquals(1, status.getPendingQuestionnaires().size());
        assertTrue(status.getStatusMessage().contains("已完成 2/3 个问卷"));
    }

    @Test
    void testGetUserAssessmentRecords_Success() {
        // 准备测试数据
        Long studentProfileId = 100L;
        Object pageReqVO = new Object();
        
        // 执行测试
        PageResult<Object> result = extensionService.getUserAssessmentRecords(studentProfileId, pageReqVO);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals(0L, result.getTotal());
    }

    @Test
    void testGetHistoryAssessmentComparison_Success() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        Integer limit = 5;
        
        List<QuestionnaireResultDO> historyResults = Arrays.asList(
                createHistoryQuestionnaireResult(1L, studentProfileId, 85.0, LocalDateTime.now()),
                createHistoryQuestionnaireResult(2L, studentProfileId, 80.0, LocalDateTime.now().minusDays(7)),
                createHistoryQuestionnaireResult(3L, studentProfileId, 75.0, LocalDateTime.now().minusDays(14))
        );
        
        when(questionnaireResultMapper.selectHistoryResults(studentProfileId, assessmentId, limit))
                .thenReturn(historyResults);
        
        // 执行测试
        List<AssessmentResultExtensionService.AssessmentResultComparison> comparisons = 
                extensionService.getHistoryAssessmentComparison(assessmentId, studentProfileId, limit);
        
        // 验证结果
        assertNotNull(comparisons);
        assertEquals(3, comparisons.size());
        
        // 验证第一个结果（最新的）
        AssessmentResultExtensionService.AssessmentResultComparison first = comparisons.get(0);
        assertEquals(1L, first.getResultId());
        assertEquals(85.0, first.getTotalScore());
        assertEquals(5.0, first.getScoreChange()); // 85 - 80
        assertEquals("显著改善", first.getChangeDescription());
        
        verify(questionnaireResultMapper).selectHistoryResults(studentProfileId, assessmentId, limit);
    }

    @Test
    void testGetAssessmentTrendAnalysis_Success() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        
        List<QuestionnaireResultDO> results = Arrays.asList(
                createHistoryQuestionnaireResult(1L, studentProfileId, 85.0, LocalDateTime.now()),
                createHistoryQuestionnaireResult(2L, studentProfileId, 80.0, LocalDateTime.now().minusDays(7)),
                createHistoryQuestionnaireResult(3L, studentProfileId, 75.0, LocalDateTime.now().minusDays(14))
        );
        
        when(questionnaireResultMapper.selectResultsByTimeRange(studentProfileId, assessmentId, startTime, endTime))
                .thenReturn(results);
        
        // 执行测试
        AssessmentResultExtensionService.AssessmentTrendAnalysis analysis = 
                extensionService.getAssessmentTrendAnalysis(assessmentId, studentProfileId, startTime, endTime);
        
        // 验证结果
        assertNotNull(analysis);
        assertNotNull(analysis.getTrendData());
        assertEquals(3, analysis.getTrendData().size());
        assertNotNull(analysis.getTrendSummary());
        assertNotNull(analysis.getInsights());
        assertNotNull(analysis.getOverallTrend());
        assertNotNull(analysis.getTrendDescription());
        
        // 验证趋势计算（85 - 75 = 10，显著改善）
        assertEquals(10.0, analysis.getOverallTrend());
        assertTrue(analysis.getTrendDescription().contains("显著上升趋势"));
        
        verify(questionnaireResultMapper).selectResultsByTimeRange(studentProfileId, assessmentId, startTime, endTime);
    }

    @Test
    void testGetAssessmentTrendAnalysis_NoData() {
        // 准备测试数据
        Long assessmentId = 1L;
        Long studentProfileId = 100L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        
        when(questionnaireResultMapper.selectResultsByTimeRange(studentProfileId, assessmentId, startTime, endTime))
                .thenReturn(Arrays.asList());
        
        // 执行测试
        AssessmentResultExtensionService.AssessmentTrendAnalysis analysis = 
                extensionService.getAssessmentTrendAnalysis(assessmentId, studentProfileId, startTime, endTime);
        
        // 验证结果
        assertNotNull(analysis);
        assertTrue(analysis.getTrendData().isEmpty());
        assertEquals(0.0, analysis.getOverallTrend());
        assertTrue(analysis.getInsights().contains("暂无数据"));
        assertEquals("暂无足够数据进行趋势分析", analysis.getTrendDescription());
    }

    @Test
    void testBatchGenerateAssessmentResults_Success() {
        // 准备测试数据
        Object batchGenerationReqVO = new Object();
        
        // 执行测试
        AssessmentResultExtensionService.BatchAssessmentGenerationResult result = 
                extensionService.batchGenerateAssessmentResults(batchGenerationReqVO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getTotalCount());
        assertEquals(8, result.getSuccessCount());
        assertEquals(2, result.getFailureCount());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
        assertNotNull(result.getSummary());
        assertEquals(2, result.getErrorMessages().size());
    }

    @Test
    void testGetAssessmentResultStatistics_Success() {
        // 准备测试数据
        Long assessmentId = 1L;
        Integer timeRange = 30;
        
        // 执行测试
        AssessmentResultExtensionService.AssessmentResultStatistics statistics = 
                extensionService.getAssessmentResultStatistics(assessmentId, timeRange);
        
        // 验证结果
        assertNotNull(statistics);
        assertEquals(100L, statistics.getTotalAssessments());
        assertEquals(85L, statistics.getCompletedAssessments());
        assertEquals(85.0, statistics.getCompletionRate());
        assertEquals(78.5, statistics.getAverageScore());
        assertNotNull(statistics.getRiskLevelDistribution());
        assertNotNull(statistics.getDimensionAverages());
        assertNotNull(statistics.getTimeSeriesData());
        
        // 验证风险等级分布
        assertEquals(3, statistics.getRiskLevelDistribution().size());
        assertEquals(20L, statistics.getRiskLevelDistribution().get(1));
        
        // 验证维度平均分
        assertEquals(3, statistics.getDimensionAverages().size());
        assertEquals(75.0, statistics.getDimensionAverages().get("anxiety"));
        
        // 验证时间序列数据
        assertEquals(31, statistics.getTimeSeriesData().size()); // timeRange + 1
    }

    private QuestionnaireResultDO createTestQuestionnaireResult(Long id, Long studentProfileId) {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(id);
        result.setQuestionnaireId(id);
        result.setStudentProfileId(studentProfileId);
        result.setAnswerData("{\"q1\":\"A\",\"q2\":\"B\"}");
        result.setTotalScore(85.5);
        result.setDimensionScores("{\"anxiety\":80,\"depression\":90}");
        result.setRiskLevel(2);
        result.setResultContent("测试结果内容");
        result.setSuggestions("测试建议");
        result.setGenerationStatus(3);
        result.setSubmitTime(LocalDateTime.now());
        result.setGenerationTime(LocalDateTime.now());
        return result;
    }

    private QuestionnaireResultDO createCompletedQuestionnaireResult(Long id, Long studentProfileId) {
        QuestionnaireResultDO result = createTestQuestionnaireResult(id, studentProfileId);
        result.setGenerationStatus(3); // 已完成
        return result;
    }

    private QuestionnaireResultDO createPendingQuestionnaireResult(Long id, Long studentProfileId) {
        QuestionnaireResultDO result = createTestQuestionnaireResult(id, studentProfileId);
        result.setGenerationStatus(1); // 待处理
        return result;
    }

    private QuestionnaireResultDO createHistoryQuestionnaireResult(Long id, Long studentProfileId, 
                                                                  Double score, LocalDateTime time) {
        QuestionnaireResultDO result = createTestQuestionnaireResult(id, studentProfileId);
        result.setTotalScore(score);
        result.setGenerationTime(time);
        return result;
    }

}