package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.AsyncResultGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 问卷结果管理API控制器测试
 *
 * @author 芋道源码
 */
@Import(QuestionnaireResultController.class)
class QuestionnaireResultControllerTest extends BaseDbUnitTest {

    @Resource
    private QuestionnaireResultController questionnaireResultController;

    @MockBean
    private QuestionnaireResultService questionnaireResultService;

    @MockBean
    private AsyncResultGenerationService asyncResultGenerationService;

    private MockMvc mockMvc;

    @Test
    void testSubmitQuestionnaireAnswers_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        when(questionnaireResultService.submitQuestionnaireAnswers(any())).thenReturn(resultId);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire-result/submit-answers")
                .contentType("application/json")
                .content("{\"questionnaireId\":1,\"studentProfileId\":100,\"answerData\":\"{\\\"q1\\\":\\\"A\\\"}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(resultId));

        // 验证调用
        verify(questionnaireResultService).submitQuestionnaireAnswers(any());
    }

    @Test
    void testGetQuestionnaireResult_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultDO result = createTestQuestionnaireResult();
        when(questionnaireResultService.getQuestionnaireResult(resultId)).thenReturn(result);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire-result/get")
                .param("id", resultId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(resultId))
                .andExpect(jsonPath("$.data.riskLevelDesc").value("中等风险"))
                .andExpect(jsonPath("$.data.generationStatusDesc").value("已完成"));

        // 验证调用
        verify(questionnaireResultService).getQuestionnaireResult(resultId);
    }

    @Test
    void testGetQuestionnaireResultPage_Success() throws Exception {
        // 准备测试数据
        PageResult<QuestionnaireResultDO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(createTestQuestionnaireResult()));
        pageResult.setTotal(1L);
        
        when(questionnaireResultService.queryQuestionnaireResults(any())).thenReturn(pageResult);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire-result/page")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].riskLevelDesc").value("中等风险"));

        // 验证调用
        verify(questionnaireResultService).queryQuestionnaireResults(any());
    }

    @Test
    void testDeleteQuestionnaireResult_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        doNothing().when(questionnaireResultService).deleteQuestionnaireResult(resultId);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(delete("/psychology/questionnaire-result/delete")
                .param("id", resultId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireResultService).deleteQuestionnaireResult(resultId);
    }

    @Test
    void testBatchDeleteQuestionnaireResults_Success() throws Exception {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(questionnaireResultService).deleteQuestionnaireResults(ids);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire-result/batch-delete")
                .contentType("application/json")
                .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireResultService).deleteQuestionnaireResults(ids);
    }

    @Test
    void testGenerateQuestionnaireResult_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        when(questionnaireResultService.generateResultAsync(resultId)).thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire-result/generate")
                .param("id", resultId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireResultService).generateResultAsync(resultId);
    }

    @Test
    void testRegenerateQuestionnaireResult_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        when(questionnaireResultService.regenerateResult(resultId)).thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire-result/regenerate")
                .param("id", resultId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireResultService).regenerateResult(resultId);
    }

    @Test
    void testBatchGenerateQuestionnaireResults_Success() throws Exception {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        QuestionnaireResultService.BatchGenerationResult batchResult = 
                new QuestionnaireResultService.BatchGenerationResult();
        batchResult.setTotalCount(3);
        batchResult.setSuccessCount(2);
        batchResult.setFailureCount(1);
        
        when(questionnaireResultService.batchGenerateResults(ids)).thenReturn(batchResult);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire-result/batch-generate")
                .contentType("application/json")
                .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failureCount").value(1));

        // 验证调用
        verify(questionnaireResultService).batchGenerateResults(ids);
    }

    @Test
    void testGetGenerationStatus_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        QuestionnaireResultService.ResultGenerationStatus status = 
                new QuestionnaireResultService.ResultGenerationStatus();
        status.setStatus(3);
        status.setMessage("已完成");
        status.setProgress(100);
        
        when(questionnaireResultService.getResultGenerationStatus(resultId)).thenReturn(status);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire-result/generation-status")
                .param("id", resultId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value(3))
                .andExpect(jsonPath("$.data.message").value("已完成"))
                .andExpect(jsonPath("$.data.progress").value(100));

        // 验证调用
        verify(questionnaireResultService).getResultGenerationStatus(resultId);
    }

    @Test
    void testGetCompletionStats_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        QuestionnaireResultService.QuestionnaireCompletionStats stats = 
                new QuestionnaireResultService.QuestionnaireCompletionStats();
        stats.setTotalResults(100L);
        stats.setCompletedResults(85L);
        stats.setCompletionRate(85.0);
        stats.setAverageScore(78.5);
        
        when(questionnaireResultService.getQuestionnaireCompletionStats(questionnaireId)).thenReturn(stats);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire-result/completion-stats")
                .param("questionnaireId", questionnaireId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalResults").value(100))
                .andExpect(jsonPath("$.data.completedResults").value(85))
                .andExpect(jsonPath("$.data.completionRate").value(85.0))
                .andExpect(jsonPath("$.data.averageScore").value(78.5));

        // 验证调用
        verify(questionnaireResultService).getQuestionnaireCompletionStats(questionnaireId);
    }

    @Test
    void testGetUserQuestionnaireResults_Success() throws Exception {
        // 准备测试数据
        Long studentProfileId = 100L;
        Long questionnaireId = 1L;
        List<QuestionnaireResultDO> results = Arrays.asList(createTestQuestionnaireResult());
        
        when(questionnaireResultService.getUserQuestionnaireResults(studentProfileId, questionnaireId))
                .thenReturn(results);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire-result/user-results")
                .param("studentProfileId", studentProfileId.toString())
                .param("questionnaireId", questionnaireId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].riskLevelDesc").value("中等风险"));

        // 验证调用
        verify(questionnaireResultService).getUserQuestionnaireResults(studentProfileId, questionnaireId);
    }

    @Test
    void testCheckUserCompletion_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        when(questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId))
                .thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire-result/completion-check")
                .param("questionnaireId", questionnaireId.toString())
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireResultService).hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
    }

    @Test
    void testGetTaskStatus_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        when(asyncResultGenerationService.checkTaskStatus(resultId))
                .thenReturn(AsyncResultGenerationService.TaskStatus.COMPLETED);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire-result/task-status")
                .param("resultId", resultId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("COMPLETED"));

        // 验证调用
        verify(asyncResultGenerationService).checkTaskStatus(resultId);
    }

    @Test
    void testCancelTask_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        when(asyncResultGenerationService.cancelTask(resultId)).thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire-result/cancel-task")
                .param("resultId", resultId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(asyncResultGenerationService).cancelTask(resultId);
    }

    @Test
    void testRetryTask_Success() throws Exception {
        // 准备测试数据
        Long resultId = 1L;
        when(asyncResultGenerationService.retryTask(resultId)).thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire-result/retry-task")
                .param("resultId", resultId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(asyncResultGenerationService).retryTask(resultId);
    }

    private QuestionnaireResultDO createTestQuestionnaireResult() {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(1L);
        result.setQuestionnaireId(1L);
        result.setStudentProfileId(100L);
        result.setAnswerData("{\"q1\":\"A\",\"q2\":\"B\"}");
        result.setTotalScore(85.5);
        result.setDimensionScores("{\"anxiety\":80,\"depression\":90}");
        result.setRiskLevel(3); // 中等风险
        result.setResultContent("测试结果内容");
        result.setSuggestions("测试建议");
        result.setGenerationStatus(3); // 已完成
        result.setSubmitTime(LocalDateTime.now());
        result.setGenerationTime(LocalDateTime.now());
        result.setCreateTime(LocalDateTime.now());
        result.setUpdateTime(LocalDateTime.now());
        return result;
    }

}