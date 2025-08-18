package cn.iocoder.yudao.module.psychology.service.questionnaire.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultServiceImpl;
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
 * 问卷结果服务测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class QuestionnaireResultServiceImplTest {

    @Mock
    private QuestionnaireResultMapper questionnaireResultMapper;

    @InjectMocks
    private QuestionnaireResultServiceImpl resultService;

    @Test
    void testCreateQuestionnaireResult_Success() {
        // 准备测试数据
        Object createReqVO = new Object();
        
        when(questionnaireResultMapper.insert(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        Long result = resultService.createQuestionnaireResult(createReqVO);
        
        // 验证结果
        assertNotNull(result);
        verify(questionnaireResultMapper).insert(any(QuestionnaireResultDO.class));
    }

    @Test
    void testGetQuestionnaireResult_Success() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO questionnaireResult = createTestQuestionnaireResult();
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(questionnaireResult);
        
        // 执行测试
        QuestionnaireResultDO result = resultService.getQuestionnaireResult(resultId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(resultId, result.getId());
        verify(questionnaireResultMapper).selectById(resultId);
    }

    @Test
    void testGetQuestionnaireResultPage_Success() {
        // 准备测试数据
        Object pageReqVO = new Object();
        PageResult<QuestionnaireResultDO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(createTestQuestionnaireResult()));
        pageResult.setTotal(1L);
        
        when(questionnaireResultMapper.selectPage(pageReqVO)).thenReturn(pageResult);
        
        // 执行测试
        PageResult<QuestionnaireResultDO> result = resultService.getQuestionnaireResultPage(pageReqVO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertFalse(result.getList().isEmpty());
        verify(questionnaireResultMapper).selectPage(pageReqVO);
    }

    @Test
    void testDeleteQuestionnaireResult_Success() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO questionnaireResult = createTestQuestionnaireResult();
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(questionnaireResult);
        when(questionnaireResultMapper.deleteById(resultId)).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> resultService.deleteQuestionnaireResult(resultId));
        
        // 验证结果
        verify(questionnaireResultMapper).selectById(resultId);
        verify(questionnaireResultMapper).deleteById(resultId);
    }

    @Test
    void testDeleteQuestionnaireResults_Success() {
        // 准备测试数据
        List<Long> resultIds = Arrays.asList(1L, 2L, 3L);
        QuestionnaireResultDO questionnaireResult = createTestQuestionnaireResult();
        
        when(questionnaireResultMapper.selectById(anyLong())).thenReturn(questionnaireResult);
        when(questionnaireResultMapper.deleteById(anyLong())).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> resultService.deleteQuestionnaireResults(resultIds));
        
        // 验证结果
        verify(questionnaireResultMapper, times(3)).selectById(anyLong());
        verify(questionnaireResultMapper, times(3)).deleteById(anyLong());
    }

    @Test
    void testGetUserQuestionnaireResults_Success() {
        // 准备测试数据
        Long studentProfileId = 100L;
        Long questionnaireId = 1L;
        List<QuestionnaireResultDO> results = Arrays.asList(createTestQuestionnaireResult());
        
        when(questionnaireResultMapper.selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId))
                .thenReturn(results);
        
        // 执行测试
        List<QuestionnaireResultDO> result = resultService.getUserQuestionnaireResults(studentProfileId, questionnaireId);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(questionnaireResultMapper).selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId);
    }

    @Test
    void testGetQuestionnaireResults_Success() {
        // 准备测试数据
        Long questionnaireId = 1L;
        List<QuestionnaireResultDO> results = Arrays.asList(createTestQuestionnaireResult());
        
        when(questionnaireResultMapper.selectListByQuestionnaire(questionnaireId)).thenReturn(results);
        
        // 执行测试
        List<QuestionnaireResultDO> result = resultService.getQuestionnaireResults(questionnaireId);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(questionnaireResultMapper).selectListByQuestionnaire(questionnaireId);
    }

    @Test
    void testHasUserCompletedQuestionnaire_Completed() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO completedResult = createTestQuestionnaireResult();
        completedResult.setGenerationStatus(2); // 已完成
        List<QuestionnaireResultDO> results = Arrays.asList(completedResult);
        
        when(questionnaireResultMapper.selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId))
                .thenReturn(results);
        
        // 执行测试
        boolean result = resultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
        
        // 验证结果
        assertTrue(result);
        verify(questionnaireResultMapper).selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId);
    }

    @Test
    void testHasUserCompletedQuestionnaire_NotCompleted() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO pendingResult = createTestQuestionnaireResult();
        pendingResult.setGenerationStatus(1); // 待处理
        List<QuestionnaireResultDO> results = Arrays.asList(pendingResult);
        
        when(questionnaireResultMapper.selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId))
                .thenReturn(results);
        
        // 执行测试
        boolean result = resultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
        
        // 验证结果
        assertFalse(result);
        verify(questionnaireResultMapper).selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId);
    }

    @Test
    void testGetQuestionnaireCompletionStats_Success() {
        // 准备测试数据
        Long questionnaireId = 1L;
        
        when(questionnaireResultMapper.countByQuestionnaire(questionnaireId)).thenReturn(100L);
        when(questionnaireResultMapper.countByQuestionnaireAndStatus(questionnaireId, 2)).thenReturn(80L);
        when(questionnaireResultMapper.countByQuestionnaireAndStatus(questionnaireId, 1)).thenReturn(20L);
        when(questionnaireResultMapper.getAverageScoreByQuestionnaire(questionnaireId)).thenReturn(85.5);
        when(questionnaireResultMapper.countByQuestionnaireAndTimeRange(eq(questionnaireId), any(), any()))
                .thenReturn(10L, 50L, 75L); // 今日、本周、本月
        
        // 执行测试
        QuestionnaireResultService.QuestionnaireCompletionStats result = 
                resultService.getQuestionnaireCompletionStats(questionnaireId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.getTotalResults());
        assertEquals(80L, result.getCompletedResults());
        assertEquals(20L, result.getPendingResults());
        assertEquals(80.0, result.getCompletionRate());
        assertEquals(85.5, result.getAverageScore());
        assertEquals(10L, result.getTodayCompleted());
        assertEquals(50L, result.getWeekCompleted());
        assertEquals(75L, result.getMonthCompleted());
    }

    @Test
    void testGetLatestUserQuestionnaireResult_Success() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO olderResult = createTestQuestionnaireResult();
        olderResult.setSubmitTime(LocalDateTime.now().minusDays(1));
        
        QuestionnaireResultDO newerResult = createTestQuestionnaireResult();
        newerResult.setId(2L);
        newerResult.setSubmitTime(LocalDateTime.now());
        
        List<QuestionnaireResultDO> results = Arrays.asList(olderResult, newerResult);
        
        when(questionnaireResultMapper.selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId))
                .thenReturn(results);
        
        // 执行测试
        QuestionnaireResultDO result = resultService.getLatestUserQuestionnaireResult(questionnaireId, studentProfileId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(questionnaireResultMapper).selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId);
    }

    @Test
    void testGetLatestUserQuestionnaireResult_NoResults() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        when(questionnaireResultMapper.selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId))
                .thenReturn(Arrays.asList());
        
        // 执行测试
        QuestionnaireResultDO result = resultService.getLatestUserQuestionnaireResult(questionnaireId, studentProfileId);
        
        // 验证结果
        assertNull(result);
        verify(questionnaireResultMapper).selectListByStudentAndQuestionnaire(studentProfileId, questionnaireId);
    }

    private QuestionnaireResultDO createTestQuestionnaireResult() {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(1L);
        result.setQuestionnaireId(1L);
        result.setStudentProfileId(100L);
        result.setAnswerData("{\"q1\":\"A\",\"q2\":\"B\"}");
        result.setTotalScore(85.5);
        result.setDimensionScores("{\"dimension1\":80,\"dimension2\":90}");
        result.setRiskLevel(2);
        result.setResultContent("测试结果内容");
        result.setSuggestions("测试建议");
        result.setGenerationStatus(2);
        result.setSubmitTime(LocalDateTime.now());
        result.setGenerationTime(LocalDateTime.now());
        return result;
    }

}