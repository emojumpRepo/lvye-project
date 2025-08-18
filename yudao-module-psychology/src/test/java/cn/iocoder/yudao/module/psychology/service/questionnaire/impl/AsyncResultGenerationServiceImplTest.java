package cn.iocoder.yudao.module.psychology.service.questionnaire.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.enums.QuestionnaireStatusEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorFactory;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorStrategy;
import cn.iocoder.yudao.module.psychology.service.questionnaire.AsyncResultGenerationService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.AsyncResultGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * 异步结果生成服务测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class AsyncResultGenerationServiceImplTest {

    @Mock
    private QuestionnaireResultMapper questionnaireResultMapper;

    @Mock
    private QuestionnaireMapper questionnaireMapper;

    @Mock
    private ResultGeneratorFactory resultGeneratorFactory;

    @Mock
    private ResultGeneratorStrategy resultGeneratorStrategy;

    @InjectMocks
    private AsyncResultGenerationServiceImpl asyncService;

    @Test
    void testGenerateResultAsync_Success() {
        // 准备测试数据
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        ResultGenerationContext generatedContext = createTestGenerationContext();
        
        when(questionnaireMapper.selectById(result.getQuestionnaireId())).thenReturn(questionnaire);
        when(resultGeneratorFactory.getGenerator(anyInt())).thenReturn(resultGeneratorStrategy);
        when(resultGeneratorStrategy.generateResult(any(ResultGenerationContext.class)))
                .thenReturn(generatedContext);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> asyncService.generateResultAsync(result));
        
        // 验证调用
        verify(questionnaireMapper).selectById(result.getQuestionnaireId());
        verify(resultGeneratorFactory).getGenerator(questionnaire.getQuestionnaireType());
        verify(resultGeneratorStrategy).generateResult(any(ResultGenerationContext.class));
        verify(questionnaireResultMapper, atLeastOnce()).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testGenerateResultAsync_QuestionnaireNotFound() {
        // 准备测试数据
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        
        when(questionnaireMapper.selectById(result.getQuestionnaireId())).thenReturn(null);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> asyncService.generateResultAsync(result));
        
        // 验证调用
        verify(questionnaireMapper).selectById(result.getQuestionnaireId());
        verify(questionnaireResultMapper, atLeastOnce()).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testGenerateResultAsync_GeneratorNotFound() {
        // 准备测试数据
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        
        when(questionnaireMapper.selectById(result.getQuestionnaireId())).thenReturn(questionnaire);
        when(resultGeneratorFactory.getGenerator(anyInt())).thenReturn(null);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> asyncService.generateResultAsync(result));
        
        // 验证调用
        verify(questionnaireMapper).selectById(result.getQuestionnaireId());
        verify(resultGeneratorFactory).getGenerator(questionnaire.getQuestionnaireType());
        verify(questionnaireResultMapper, atLeastOnce()).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testCheckTaskStatus_FromCache() {
        // 准备测试数据
        Long resultId = 1L;
        
        // 先调用一次生成任务，将状态放入缓存
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        ResultGenerationContext generatedContext = createTestGenerationContext();
        
        when(questionnaireMapper.selectById(result.getQuestionnaireId())).thenReturn(questionnaire);
        when(resultGeneratorFactory.getGenerator(anyInt())).thenReturn(resultGeneratorStrategy);
        when(resultGeneratorStrategy.generateResult(any(ResultGenerationContext.class)))
                .thenReturn(generatedContext);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        asyncService.generateResultAsync(result);
        
        // 执行测试
        AsyncResultGenerationService.TaskStatus status = asyncService.checkTaskStatus(resultId);
        
        // 验证结果
        assertNotNull(status);
        assertEquals(AsyncResultGenerationService.TaskStatus.COMPLETED, status);
    }

    @Test
    void testCheckTaskStatus_FromDatabase() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(2); // 运行中
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        
        // 执行测试
        AsyncResultGenerationService.TaskStatus status = asyncService.checkTaskStatus(resultId);
        
        // 验证结果
        assertNotNull(status);
        assertEquals(AsyncResultGenerationService.TaskStatus.RUNNING, status);
        verify(questionnaireResultMapper).selectById(resultId);
    }

    @Test
    void testCheckTaskStatus_ResultNotFound() {
        // 准备测试数据
        Long resultId = 999L;
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(null);
        
        // 执行测试
        AsyncResultGenerationService.TaskStatus status = asyncService.checkTaskStatus(resultId);
        
        // 验证结果
        assertEquals(AsyncResultGenerationService.TaskStatus.FAILED, status);
        verify(questionnaireResultMapper).selectById(resultId);
    }

    @Test
    void testCancelTask_Success() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(2); // 运行中
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        boolean success = asyncService.cancelTask(resultId);
        
        // 验证结果
        assertTrue(success);
        verify(questionnaireResultMapper).selectById(resultId);
        verify(questionnaireResultMapper).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testCancelTask_InvalidStatus() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(3); // 已完成
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        
        // 执行测试
        boolean success = asyncService.cancelTask(resultId);
        
        // 验证结果
        assertFalse(success);
        verify(questionnaireResultMapper).selectById(resultId);
        verify(questionnaireResultMapper, never()).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testRetryTask_Success() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(4); // 失败
        
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        ResultGenerationContext generatedContext = createTestGenerationContext();
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        when(questionnaireMapper.selectById(result.getQuestionnaireId())).thenReturn(questionnaire);
        when(resultGeneratorFactory.getGenerator(anyInt())).thenReturn(resultGeneratorStrategy);
        when(resultGeneratorStrategy.generateResult(any(ResultGenerationContext.class)))
                .thenReturn(generatedContext);
        when(questionnaireResultMapper.updateById(any(QuestionnaireResultDO.class))).thenReturn(1);
        
        // 执行测试
        boolean success = asyncService.retryTask(resultId);
        
        // 验证结果
        assertTrue(success);
        verify(questionnaireResultMapper, atLeastOnce()).selectById(resultId);
        verify(questionnaireResultMapper, atLeastOnce()).updateById(any(QuestionnaireResultDO.class));
    }

    @Test
    void testRetryTask_InvalidStatus() {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        result.setGenerationStatus(3); // 已完成
        
        when(questionnaireResultMapper.selectById(resultId)).thenReturn(result);
        
        // 执行测试
        boolean success = asyncService.retryTask(resultId);
        
        // 验证结果
        assertFalse(success);
        verify(questionnaireResultMapper).selectById(resultId);
    }

    private QuestionnaireResultDO createTestQuestionnaireResult() {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(1L);
        result.setQuestionnaireId(1L);
        result.setStudentProfileId(100L);
        result.setAnswerData("{\"q1\":\"A\",\"q2\":\"B\"}");
        result.setGenerationStatus(1);
        result.setSubmitTime(LocalDateTime.now());
        return result;
    }

    private QuestionnaireDO createTestQuestionnaire() {
        QuestionnaireDO questionnaire = new QuestionnaireDO();
        questionnaire.setId(1L);
        questionnaire.setTitle("测试问卷");
        questionnaire.setQuestionnaireType(1);
        questionnaire.setStatus(QuestionnaireStatusEnum.PUBLISHED.getStatus());
        return questionnaire;
    }

    private ResultGenerationContext createTestGenerationContext() {
        ResultGenerationContext context = new ResultGenerationContext();
        context.setQuestionnaireId(1L);
        context.setStudentProfileId(100L);
        context.setTotalScore(85.5);
        context.setRiskLevel(2);
        context.setResultContent("测试结果内容");
        context.setSuggestions("测试建议");
        return context;
    }

}