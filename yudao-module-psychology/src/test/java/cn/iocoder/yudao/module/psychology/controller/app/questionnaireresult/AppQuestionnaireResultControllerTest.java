package cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 学生问卷结果API控制器测试
 *
 * @author 芋道源码
 */
@Import(AppQuestionnaireResultController.class)
class AppQuestionnaireResultControllerTest extends BaseDbUnitTest {

    @Resource
    private AppQuestionnaireResultController appQuestionnaireResultController;

    @MockBean
    private QuestionnaireResultService questionnaireResultService;

    @MockBean
    private QuestionnaireService questionnaireService;

    @MockBean
    private QuestionnaireAccessService questionnaireAccessService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSubmitQuestionnaireAnswers_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        Long resultId = 2001L;
        
        AppQuestionnaireAnswerSubmitReqVO submitReqVO = createTestSubmitReqVO(questionnaireId, studentProfileId);
        QuestionnaireDO questionnaire = createTestQuestionnaire(questionnaireId, "测试问卷");
        
        when(questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId))
                .thenReturn(true);
        when(questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId))
                .thenReturn(false);
        when(questionnaireService.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        when(questionnaireResultService.submitQuestionnaireAnswers(
                eq(questionnaireId), eq(studentProfileId), any(Map.class), anyInt(), 
                anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(resultId);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire-result/submit-answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitReqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.resultId").value(resultId))
                .andExpect(jsonPath("$.data.questionnaireId").value(questionnaireId))
                .andExpect(jsonPath("$.data.resultStatus").value(1))
                .andExpect(jsonPath("$.data.needWaitResult").value(true))
                .andExpect(jsonPath("$.data.statusMessage").value("答案提交成功，正在生成结果..."));

        // 验证调用
        verify(questionnaireAccessService).checkQuestionnaireAccess(questionnaireId, studentProfileId);
        verify(questionnaireResultService).hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
        verify(questionnaireService).getQuestionnaire(questionnaireId);
        verify(questionnaireResultService).submitQuestionnaireAnswers(
                eq(questionnaireId), eq(studentProfileId), any(Map.class), anyInt(), 
                anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void testSubmitQuestionnaireAnswers_NoPermission() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        AppQuestionnaireAnswerSubmitReqVO submitReqVO = createTestSubmitReqVO(questionnaireId, studentProfileId);
        
        when(questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId))
                .thenReturn(false);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire-result/submit-answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitReqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.resultStatus").value(3))
                .andExpect(jsonPath("$.data.needWaitResult").value(false))
                .andExpect(jsonPath("$.data.statusMessage").value("无权限提交该问卷答案"));

        // 验证调用
        verify(questionnaireAccessService).checkQuestionnaireAccess(questionnaireId, studentProfileId);
        verify(questionnaireResultService, never()).hasUserCompletedQuestionnaire(anyLong(), anyLong());
        verify(questionnaireService, never()).getQuestionnaire(anyLong());
    }

    @Test
    void testSubmitQuestionnaireAnswers_AlreadyCompleted() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        AppQuestionnaireAnswerSubmitReqVO submitReqVO = createTestSubmitReqVO(questionnaireId, studentProfileId);
        
        when(questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId))
                .thenReturn(true);
        when(questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId))
                .thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire-result/submit-answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitReqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.resultStatus").value(2))
                .andExpect(jsonPath("$.data.needWaitResult").value(false))
                .andExpect(jsonPath("$.data.statusMessage").value("您已经完成过该问卷，无需重复提交"));

        // 验证调用
        verify(questionnaireAccessService).checkQuestionnaireAccess(questionnaireId, studentProfileId);
        verify(questionnaireResultService).hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
        verify(questionnaireService, never()).getQuestionnaire(anyLong());
    }

    @Test
    void testGetQuestionnaireResult_Success() throws Exception {
        // 准备测试数据
        Long resultId = 2001L;
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO result = createTestResult(resultId, questionnaireId, studentProfileId);
        QuestionnaireDO questionnaire = createTestQuestionnaire(questionnaireId, "测试问卷");
        
        when(questionnaireResultService.getQuestionnaireResult(resultId)).thenReturn(result);
        when(questionnaireService.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire-result/get/{id}", resultId)
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(resultId))
                .andExpect(jsonPath("$.data.questionnaireId").value(questionnaireId))
                .andExpect(jsonPath("$.data.studentProfileId").value(studentProfileId))
                .andExpect(jsonPath("$.data.totalScore").value(85))
                .andExpect(jsonPath("$.data.riskLevel").value(2));

        // 验证调用
        verify(questionnaireResultService).getQuestionnaireResult(resultId);
        verify(questionnaireService).getQuestionnaire(questionnaireId);
    }

    @Test
    void testGetQuestionnaireResult_WrongStudent() throws Exception {
        // 准备测试数据
        Long resultId = 2001L;
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        Long wrongStudentId = 999L;
        
        QuestionnaireResultDO result = createTestResult(resultId, questionnaireId, studentProfileId);
        
        when(questionnaireResultService.getQuestionnaireResult(resultId)).thenReturn(result);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire-result/get/{id}", resultId)
                .param("studentProfileId", wrongStudentId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isEmpty());

        // 验证调用
        verify(questionnaireResultService).getQuestionnaireResult(resultId);
        verify(questionnaireService, never()).getQuestionnaire(anyLong());
    }

    @Test
    void testGetResultGenerationProgress_Success() throws Exception {
        // 准备测试数据
        Long resultId = 2001L;
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO result = createTestResult(resultId, questionnaireId, studentProfileId);
        result.setResultStatus(1); // 生成中
        result.setGenerationProgress(75);
        result.setCurrentStep("正在计算各维度得分...");
        
        QuestionnaireDO questionnaire = createTestQuestionnaire(questionnaireId, "测试问卷");
        
        when(questionnaireResultService.getQuestionnaireResult(resultId)).thenReturn(result);
        when(questionnaireService.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire-result/progress/{resultId}", resultId)
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.resultId").value(resultId))
                .andExpect(jsonPath("$.data.resultStatus").value(1))
                .andExpect(jsonPath("$.data.progress").value(75))
                .andExpect(jsonPath("$.data.currentStep").value("正在计算各维度得分..."))
                .andExpect(jsonPath("$.data.isCompleted").value(false))
                .andExpect(jsonPath("$.data.isSuccess").value(false));

        // 验证调用
        verify(questionnaireResultService).getQuestionnaireResult(resultId);
        verify(questionnaireService).getQuestionnaire(questionnaireId);
    }

    @Test
    void testRetryResultGeneration_Success() throws Exception {
        // 准备测试数据
        Long resultId = 2001L;
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO result = createTestResult(resultId, questionnaireId, studentProfileId);
        result.setResultStatus(3); // 生成失败
        
        when(questionnaireResultService.getQuestionnaireResult(resultId)).thenReturn(result);
        doNothing().when(questionnaireResultService).retryResultGeneration(resultId);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire-result/retry-generation/{resultId}", resultId)
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireResultService).getQuestionnaireResult(resultId);
        verify(questionnaireResultService).retryResultGeneration(resultId);
    }

    @Test
    void testRetryResultGeneration_NotFailed() throws Exception {
        // 准备测试数据
        Long resultId = 2001L;
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO result = createTestResult(resultId, questionnaireId, studentProfileId);
        result.setResultStatus(2); // 生成成功，不能重试
        
        when(questionnaireResultService.getQuestionnaireResult(resultId)).thenReturn(result);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire-result/retry-generation/{resultId}", resultId)
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(false));

        // 验证调用
        verify(questionnaireResultService).getQuestionnaireResult(resultId);
        verify(questionnaireResultService, never()).retryResultGeneration(anyLong());
    }

    @Test
    void testCheckCanRetake_CanRetake() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO latestResult = createTestResult(2001L, questionnaireId, studentProfileId);
        latestResult.setCompleteTime(LocalDateTime.now().minusDays(35)); // 35天前完成
        
        when(questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId))
                .thenReturn(true);
        when(questionnaireResultService.getLatestQuestionnaireResult(questionnaireId, studentProfileId))
                .thenReturn(latestResult);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire-result/can-retake/{questionnaireId}", questionnaireId)
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.hasCompleted").value(true))
                .andExpect(jsonPath("$.data.canRetake").value(true))
                .andExpect(jsonPath("$.data.reason").value("可以重新测试"));

        // 验证调用
        verify(questionnaireResultService).hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
        verify(questionnaireResultService).getLatestQuestionnaireResult(questionnaireId, studentProfileId);
    }

    @Test
    void testCheckCanRetake_TooSoon() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireResultDO latestResult = createTestResult(2001L, questionnaireId, studentProfileId);
        latestResult.setCompleteTime(LocalDateTime.now().minusDays(15)); // 15天前完成，还不能重测
        
        when(questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId))
                .thenReturn(true);
        when(questionnaireResultService.getLatestQuestionnaireResult(questionnaireId, studentProfileId))
                .thenReturn(latestResult);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireResultController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire-result/can-retake/{questionnaireId}", questionnaireId)
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.hasCompleted").value(true))
                .andExpect(jsonPath("$.data.canRetake").value(false))
                .andExpect(jsonPath("$.data.reason").value("距离上次测试时间过短"));

        // 验证调用
        verify(questionnaireResultService).hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
        verify(questionnaireResultService).getLatestQuestionnaireResult(questionnaireId, studentProfileId);
    }

    // 辅助方法

    private AppQuestionnaireAnswerSubmitReqVO createTestSubmitReqVO(Long questionnaireId, Long studentProfileId) {
        AppQuestionnaireAnswerSubmitReqVO submitReqVO = new AppQuestionnaireAnswerSubmitReqVO();
        submitReqVO.setQuestionnaireId(questionnaireId);
        submitReqVO.setStudentProfileId(studentProfileId);
        submitReqVO.setAccessId(1001L);
        submitReqVO.setSubmitSource(1);
        submitReqVO.setUserAgent("Mozilla/5.0");
        submitReqVO.setAnswerDuration(900);
        submitReqVO.setRemark("学生自主完成");
        
        // 创建答案数据
        AppQuestionnaireAnswerSubmitReqVO.QuestionAnswerVO answer1 = new AppQuestionnaireAnswerSubmitReqVO.QuestionAnswerVO();
        answer1.setQuestionId("Q001");
        answer1.setQuestionType(1);
        answer1.setAnswerContent("A");
        answer1.setAnswerScore(3);
        answer1.setAnswerWeight(1.0);
        answer1.setIsSkipped(false);
        answer1.setQuestionDuration(30);
        
        AppQuestionnaireAnswerSubmitReqVO.QuestionAnswerVO answer2 = new AppQuestionnaireAnswerSubmitReqVO.QuestionAnswerVO();
        answer2.setQuestionId("Q002");
        answer2.setQuestionType(1);
        answer2.setAnswerContent("B");
        answer2.setAnswerScore(2);
        answer2.setAnswerWeight(1.0);
        answer2.setIsSkipped(false);
        answer2.setQuestionDuration(25);
        
        submitReqVO.setAnswers(Arrays.asList(answer1, answer2));
        
        return submitReqVO;
    }

    private QuestionnaireDO createTestQuestionnaire(Long id, String title) {
        QuestionnaireDO questionnaire = new QuestionnaireDO();
        questionnaire.setId(id);
        questionnaire.setTitle(title);
        questionnaire.setDescription("测试问卷描述");
        questionnaire.setQuestionnaireType(1);
        questionnaire.setQuestionCount(20);
        questionnaire.setEstimatedDuration(15);
        questionnaire.setExternalLink("https://example.com/survey/" + id);
        questionnaire.setStatus(1);
        questionnaire.setCreateTime(LocalDateTime.now());
        return questionnaire;
    }

    private QuestionnaireResultDO createTestResult(Long id, Long questionnaireId, Long studentProfileId) {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(id);
        result.setQuestionnaireId(questionnaireId);
        result.setStudentProfileId(studentProfileId);
        result.setTotalScore(85);
        result.setMaxScore(100);
        result.setRiskLevel(2);
        result.setResultStatus(2); // 生成成功
        result.setResultInterpretation("您的心理健康状况总体良好");
        result.setSuggestions("建议您保持良好的作息");
        result.setDetailedReport("详细报告内容");
        result.setAnswerDuration(900);
        result.setCreateTime(LocalDateTime.now().minusHours(1));
        result.setCompleteTime(LocalDateTime.now());
        return result;
    }

}