package cn.iocoder.yudao.module.psychology.service.questionnaire.impl;

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
 * 问卷答案提交和结果生成集成测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class QuestionnaireAnswerSubmissionTest {

    @Mock
    private QuestionnaireResultMapper questionnaireResultMapper;

    @InjectMocks
    private QuestionnaireResultServiceImpl resultService;

    @Test
    void testSubmitQuestionnaireAnswers_Success() {
        // 准备测试数据
        Object submitReqVO = new Object();
        
        // Mock 用户未完成问卷
        when(questionnaireResultMapper.selectListByStudentAndQuestionnaire(anyLong(), anyLong()))
                .thenReturn(Arrays.asList());
        
        when(questionnaireResultMapper.insert(any(QuestionnaireResultDO.class))).thenReturn(1);
        when(questionnaireResultMapper.selectById(anyLong())).thenReturn(createTestQuestionnaireResult());
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        Long resultId = resultService.submitQuestionnaireAnswers(submitReqVO);
        
        // 验证结果
        assertNotNull(resultId);
        verify(questionnaireResultMapper).insert(any(QuestionnaireResultDO.class));
        verify(questionnaireResultMapper, atLeastOnce()).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testGenerateResultAsync_Success() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(1); // 待处理
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        boolean success = resultService.generateResultAsync(resultId);
        
        // 验证结果
        assertTrue(success);
        verify(questionnaireResultMapper).selectById(resultId);
        verify(questionnaireResultMapper, atLeastOnce()).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testGetResultGenerationStatus_Pending() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(1); // 待处理
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        
        // 执行测试
        QuestionnaireResultService.ResultGenerationStatus status = 
                resultService.getResultGenerationStatus(resultId);
        
        // 验证结果
        assertNotNull(status);
        assertEquals(1, status.getStatus());
        assertEquals("待处理", status.getMessage());
        assertEquals(0, status.getProgress());
        verify(questionnaireResultMapper).selectById(resultId);
    }

    @Test
    void testGetResultGenerationStatus_InProgress() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(2); // 生成中
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        
        // 执行测试
        QuestionnaireResultService.ResultGenerationStatus status = 
                resultService.getResultGenerationStatus(resultId);
        
        // 验证结果
        assertNotNull(status);
        assertEquals(2, status.getStatus());
        assertEquals("生成中", status.getMessage());
        assertEquals(50, status.getProgress());
        verify(questionnaireResultMapper).selectById(resultId);
    }

    @Test
    void testGetResultGenerationStatus_Completed() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(3); // 已完成
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        
        // 执行测试
        QuestionnaireResultService.ResultGenerationStatus status = 
                resultService.getResultGenerationStatus(resultId);
        
        // 验证结果
        assertNotNull(status);
        assertEquals(3, status.getStatus());
        assertEquals("已完成", status.getMessage());
        assertEquals(100, status.getProgress());
        verify(questionnaireResultMapper).selectById(resultId);
    }

    @Test
    void testGetResultGenerationStatus_Failed() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(4); // 失败
        result.setErrorMessage("生成失败原因");
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        
        // 执行测试
        QuestionnaireResultService.ResultGenerationStatus status = 
                resultService.getResultGenerationStatus(resultId);
        
        // 验证结果
        assertNotNull(status);
        assertEquals(4, status.getStatus());
        assertEquals("生成失败", status.getMessage());
        assertEquals(0, status.getProgress());
        assertEquals("生成失败原因", status.getErrorMessage());
        verify(questionnaireResultMapper).selectById(resultId);
    }

    @Test
    void testRegenerateResult_Success() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(4); // 失败状态
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        boolean success = resultService.regenerateResult(resultId);
        
        // 验证结果
        assertTrue(success);
        verify(questionnaireResultMapper, atLeastOnce()).selectById(resultId);
        verify(questionnaireResultMapper, atLeastOnce()).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testBatchGenerateResults_Success() {
        // 准备测试数据
        List<Long> resultIds = Arrays.asList(1L, 2L, 3L);
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        
        when(questionnaireResultMapper.selectById(anyLong())).thenReturn(result);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        QuestionnaireResultService.BatchGenerationResult batchResult = 
                resultService.batchGenerateResults(resultIds);
        
        // 验证结果
        assertNotNull(batchResult);
        assertEquals(3, batchResult.getTotalCount());
        assertEquals(3, batchResult.getSuccessCount());
        assertEquals(0, batchResult.getFailureCount());
        assertTrue(batchResult.getErrorMessages().isEmpty());
        assertNotNull(batchResult.getStartTime());
        assertNotNull(batchResult.getEndTime());
    }

    @Test
    void testBatchGenerateResults_PartialFailure() {
        // 准备测试数据
        List<Long> resultIds = Arrays.asList(1L, 2L, 3L);
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        
        // 模拟第二个结果不存在
        when(questionnaireResultMapper.selectById(1L)).thenReturn(result);
        when(questionnaireResultMapper.selectById(2L)).thenReturn(null);
        when(questionnaireResultMapper.selectById(3L)).thenReturn(result);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        QuestionnaireResultService.BatchGenerationResult batchResult = 
                resultService.batchGenerateResults(resultIds);
        
        // 验证结果
        assertNotNull(batchResult);
        assertEquals(3, batchResult.getTotalCount());
        assertEquals(2, batchResult.getSuccessCount());
        assertEquals(1, batchResult.getFailureCount());
        assertFalse(batchResult.getErrorMessages().isEmpty());
    }

    @Test
    void testSubmitQuestionnaireAnswers_AlreadyCompleted() {
        // 准备测试数据
        Object submitReqVO = new Object();
        
        // Mock 用户已完成问卷
        QuestionnaireResultDO completedResult = createTestQuestionnaireResult();
        completedResult.setGenerationStatus(2); // 已完成
        when(questionnaireResultMapper.selectListByStudentAndQuestionnaire(anyLong(), anyLong()))
                .thenReturn(Arrays.asList(completedResult));
        
        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            resultService.submitQuestionnaireAnswers(submitReqVO);
        });
        
        // 验证不会插入新记录
        verify(questionnaireResultMapper, never()).insert(any(QuestionnaireResultDO.class));
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
        result.setGenerationStatus(1);
        result.setSubmitTime(LocalDateTime.now());
        result.setGenerationTime(LocalDateTime.now());
        return result;
    }

}