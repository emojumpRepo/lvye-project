package cn.iocoder.yudao.module.psychology.service.questionnaire.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.enums.QuestionnaireStatusEnum;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 问卷结果查询和导出功能测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class QuestionnaireResultQueryAndExportTest {

    @Mock
    private QuestionnaireResultMapper questionnaireResultMapper;

    @Mock
    private QuestionnaireMapper questionnaireMapper;

    @InjectMocks
    private QuestionnaireResultServiceImpl resultService;

    @Test
    void testQueryQuestionnaireResults_Success() {
        // 准备测试数据
        Object queryReqVO = new Object();
        PageResult<QuestionnaireResultDO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(createTestQuestionnaireResult()));
        pageResult.setTotal(1L);
        
        when(questionnaireResultMapper.selectPage(queryReqVO)).thenReturn(pageResult);
        
        // 执行测试
        PageResult<QuestionnaireResultDO> result = resultService.queryQuestionnaireResults(queryReqVO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertFalse(result.getList().isEmpty());
        verify(questionnaireResultMapper).selectPage(queryReqVO);
    }

    @Test
    void testExportQuestionnaireResultsToExcel_Success() {
        // 准备测试数据
        Object exportReqVO = new Object();
        List<QuestionnaireResultDO> results = Arrays.asList(
                createTestQuestionnaireResult(),
                createTestQuestionnaireResult()
        );
        
        when(questionnaireResultMapper.selectListForExport(exportReqVO)).thenReturn(results);
        
        // 执行测试
        byte[] excelData = resultService.exportQuestionnaireResultsToExcel(exportReqVO);
        
        // 验证结果
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        verify(questionnaireResultMapper).selectListForExport(exportReqVO);
    }

    @Test
    void testGetQuestionnaireResultAnalysis_Success() {
        // 准备测试数据
        Object analysisReqVO = new Object();
        Long questionnaireId = 1L;
        
        when(questionnaireResultMapper.countByQuestionnaire(questionnaireId)).thenReturn(100L);
        when(questionnaireResultMapper.getAverageScoreByQuestionnaire(questionnaireId)).thenReturn(85.5);
        when(questionnaireResultMapper.getMaxScoreByQuestionnaire(questionnaireId)).thenReturn(98.0);
        when(questionnaireResultMapper.getMinScoreByQuestionnaire(questionnaireId)).thenReturn(65.0);
        
        Map<Integer, Long> riskDistribution = new HashMap<>();
        riskDistribution.put(1, 20L);
        riskDistribution.put(2, 60L);
        riskDistribution.put(3, 20L);
        when(questionnaireResultMapper.getRiskLevelDistribution(questionnaireId)).thenReturn(riskDistribution);
        
        Map<String, Double> dimensionAverages = new HashMap<>();
        dimensionAverages.put("anxiety", 80.0);
        dimensionAverages.put("depression", 75.0);
        when(questionnaireResultMapper.getDimensionAverages(questionnaireId)).thenReturn(dimensionAverages);
        
        List<Map<String, Object>> trendData = Arrays.asList(
                createTrendDataPoint("2024-01-01", 10L),
                createTrendDataPoint("2024-01-02", 15L)
        );
        when(questionnaireResultMapper.getTrendData(questionnaireId, 30)).thenReturn(trendData);
        
        // 执行测试
        QuestionnaireResultService.QuestionnaireResultAnalysis analysis = 
                resultService.getQuestionnaireResultAnalysis(analysisReqVO);
        
        // 验证结果
        assertNotNull(analysis);
        assertEquals(100L, analysis.getTotalResults());
        assertEquals(85.5, analysis.getAverageScore());
        assertEquals(98.0, analysis.getMaxScore());
        assertEquals(65.0, analysis.getMinScore());
        assertEquals(3, analysis.getRiskLevelDistribution().size());
        assertEquals(2, analysis.getDimensionAverages().size());
        assertEquals(2, analysis.getTrendData().size());
        assertNotNull(analysis.getCorrelationAnalysis());
    }

    @Test
    void testCheckQuestionnaireSupportability_FullySupported() {
        // 准备测试数据
        Long questionnaireId = 1L;
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        questionnaire.setStatus(QuestionnaireStatusEnum.PUBLISHED.getStatus());
        questionnaire.setQuestionCount(20);
        questionnaire.setExternalLink("https://example.com/survey/1");
        
        when(questionnaireMapper.selectById(questionnaireId)).thenReturn(questionnaire);
        when(questionnaireResultMapper.countByQuestionnaire(questionnaireId)).thenReturn(50L);
        
        // 执行测试
        QuestionnaireResultService.QuestionnaireSupportabilityResult result = 
                resultService.checkQuestionnaireSupportability(questionnaireId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSupportable());
        assertEquals("问卷支持性良好", result.getReason());
        assertTrue(result.getSupportabilityScore() >= 70);
        assertTrue(result.getIssues().isEmpty());
        verify(questionnaireMapper).selectById(questionnaireId);
    }

    @Test
    void testCheckQuestionnaireSupportability_NotSupported() {
        // 准备测试数据
        Long questionnaireId = 1L;
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        questionnaire.setStatus(QuestionnaireStatusEnum.DRAFT.getStatus()); // 未发布
        questionnaire.setQuestionCount(0); // 题目数量不正确
        questionnaire.setExternalLink(null); // 缺少外部链接
        
        when(questionnaireMapper.selectById(questionnaireId)).thenReturn(questionnaire);
        when(questionnaireResultMapper.countByQuestionnaire(questionnaireId)).thenReturn(0L);
        
        // 执行测试
        QuestionnaireResultService.QuestionnaireSupportabilityResult result = 
                resultService.checkQuestionnaireSupportability(questionnaireId);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSupportable());
        assertEquals("问卷支持性不足，需要改进", result.getReason());
        assertTrue(result.getSupportabilityScore() < 70);
        assertFalse(result.getIssues().isEmpty());
        assertFalse(result.getRecommendations().isEmpty());
        verify(questionnaireMapper).selectById(questionnaireId);
    }

    @Test
    void testCheckQuestionnaireSupportability_QuestionnaireNotFound() {
        // 准备测试数据
        Long questionnaireId = 999L;
        
        when(questionnaireMapper.selectById(questionnaireId)).thenReturn(null);
        
        // 执行测试
        QuestionnaireResultService.QuestionnaireSupportabilityResult result = 
                resultService.checkQuestionnaireSupportability(questionnaireId);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSupportable());
        assertEquals("问卷不存在", result.getReason());
        assertEquals(0, result.getSupportabilityScore());
        assertTrue(result.getIssues().contains("问卷ID不存在"));
        verify(questionnaireMapper).selectById(questionnaireId);
    }

    @Test
    void testGetOptimizedQuestionnaireResults_Success() {
        // 准备测试数据
        List<Long> resultIds = Arrays.asList(1L, 2L, 3L);
        List<QuestionnaireResultDO> results = Arrays.asList(
                createTestQuestionnaireResult(),
                createTestQuestionnaireResult(),
                createTestQuestionnaireResult()
        );
        
        when(questionnaireResultMapper.selectBatchIds(resultIds)).thenReturn(results);
        
        // 执行测试
        List<QuestionnaireResultDO> result = resultService.getOptimizedQuestionnaireResults(resultIds);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(questionnaireResultMapper).selectBatchIds(resultIds);
    }

    @Test
    void testGetOptimizedQuestionnaireResults_EmptyList() {
        // 准备测试数据
        List<Long> resultIds = Arrays.asList();
        
        // 执行测试
        List<QuestionnaireResultDO> result = resultService.getOptimizedQuestionnaireResults(resultIds);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(questionnaireResultMapper, never()).selectBatchIds(any());
    }

    @Test
    void testGetOptimizedQuestionnaireResults_BatchQueryFails() {
        // 准备测试数据
        List<Long> resultIds = Arrays.asList(1L, 2L, 3L);
        QuestionnaireResultDO result1 = createTestQuestionnaireResult();
        result1.setId(1L);
        QuestionnaireResultDO result2 = createTestQuestionnaireResult();
        result2.setId(2L);
        
        when(questionnaireResultMapper.selectBatchIds(resultIds))
                .thenThrow(new RuntimeException("批量查询失败"));
        when(questionnaireResultMapper.selectById(1L)).thenReturn(result1);
        when(questionnaireResultMapper.selectById(2L)).thenReturn(result2);
        when(questionnaireResultMapper.selectById(3L)).thenReturn(null);
        
        // 执行测试
        List<QuestionnaireResultDO> result = resultService.getOptimizedQuestionnaireResults(resultIds);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size()); // 只有两个成功查询
        verify(questionnaireResultMapper).selectBatchIds(resultIds);
        verify(questionnaireResultMapper, times(3)).selectById(anyLong());
    }

    private QuestionnaireResultDO createTestQuestionnaireResult() {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(1L);
        result.setQuestionnaireId(1L);
        result.setStudentProfileId(100L);
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

    private QuestionnaireDO createTestQuestionnaire() {
        QuestionnaireDO questionnaire = new QuestionnaireDO();
        questionnaire.setId(1L);
        questionnaire.setTitle("测试问卷");
        questionnaire.setDescription("测试问卷描述");
        questionnaire.setQuestionnaireType(1);
        questionnaire.setTargetAudience(1);
        questionnaire.setQuestionCount(10);
        questionnaire.setEstimatedDuration(15);
        questionnaire.setStatus(QuestionnaireStatusEnum.PUBLISHED.getStatus());
        questionnaire.setIsOpen(1);
        questionnaire.setAccessCount(0);
        questionnaire.setCompletionCount(0);
        questionnaire.setSyncStatus(1);
        return questionnaire;
    }

    private Map<String, Object> createTrendDataPoint(String date, Long count) {
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("date", date);
        dataPoint.put("count", count);
        return dataPoint;
    }

}