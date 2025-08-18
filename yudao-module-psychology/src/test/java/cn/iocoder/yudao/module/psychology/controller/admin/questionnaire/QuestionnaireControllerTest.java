package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireSyncService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 问卷管理API控制器测试
 *
 * @author 芋道源码
 */
@Import(QuestionnaireController.class)
class QuestionnaireControllerTest extends BaseDbUnitTest {

    @Resource
    private QuestionnaireController questionnaireController;

    @MockBean
    private QuestionnaireService questionnaireService;

    @MockBean
    private QuestionnaireSyncService questionnaireSyncService;

    @MockBean
    private QuestionnaireAccessService questionnaireAccessService;

    private MockMvc mockMvc;

    @Test
    void testCreateQuestionnaire_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        when(questionnaireService.createQuestionnaire(any())).thenReturn(questionnaireId);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire/create")
                .contentType("application/json")
                .content("{\"title\":\"测试问卷\",\"questionnaireType\":1,\"targetAudience\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(questionnaireId));

        // 验证调用
        verify(questionnaireService).createQuestionnaire(any());
    }

    @Test
    void testUpdateQuestionnaire_Success() throws Exception {
        // 准备测试数据
        doNothing().when(questionnaireService).updateQuestionnaire(any());

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(put("/psychology/questionnaire/update")
                .contentType("application/json")
                .content("{\"id\":1,\"title\":\"更新后的问卷\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireService).updateQuestionnaire(any());
    }

    @Test
    void testDeleteQuestionnaire_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        doNothing().when(questionnaireService).deleteQuestionnaire(questionnaireId);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(delete("/psychology/questionnaire/delete")
                .param("id", questionnaireId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireService).deleteQuestionnaire(questionnaireId);
    }

    @Test
    void testGetQuestionnaire_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        when(questionnaireService.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire/get")
                .param("id", questionnaireId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpected(jsonPath("$.data").exists());

        // 验证调用
        verify(questionnaireService).getQuestionnaire(questionnaireId);
    }

    @Test
    void testGetQuestionnairePage_Success() throws Exception {
        // 准备测试数据
        PageResult<QuestionnaireDO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(createTestQuestionnaire()));
        pageResult.setTotal(1L);
        
        when(questionnaireService.getQuestionnairePage(any())).thenReturn(pageResult);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire/page")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));

        // 验证调用
        verify(questionnaireService).getQuestionnairePage(any());
    }

    @Test
    void testGetAvailableQuestionnaires_Success() throws Exception {
        // 准备测试数据
        List<QuestionnaireDO> questionnaires = Arrays.asList(createTestQuestionnaire());
        when(questionnaireService.getAvailableQuestionnaires(any())).thenReturn(questionnaires);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire/available")
                .param("targetAudience", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());

        // 验证调用
        verify(questionnaireService).getAvailableQuestionnaires(1);
    }

    @Test
    void testPublishQuestionnaire_Success() throws Exception {
        // 准备测试数据
        doNothing().when(questionnaireService).publishQuestionnaireToExternal(anyLong());

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire/publish")
                .contentType("application/json")
                .content("{\"id\":1,\"syncType\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("发布成功"));

        // 验证调用
        verify(questionnaireService).publishQuestionnaireToExternal(1L);
    }

    @Test
    void testPublishQuestionnaire_Failure() throws Exception {
        // 准备测试数据
        doThrow(new RuntimeException("发布失败")).when(questionnaireService).publishQuestionnaireToExternal(anyLong());

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire/publish")
                .contentType("application/json")
                .content("{\"id\":1,\"syncType\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.message").value("发布失败: 发布失败"));

        // 验证调用
        verify(questionnaireService).publishQuestionnaireToExternal(1L);
    }

    @Test
    void testPauseQuestionnaire_Success() throws Exception {
        // 准备测试数据
        doNothing().when(questionnaireService).pauseQuestionnaireInExternal(anyLong());

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire/pause")
                .contentType("application/json")
                .content("{\"id\":1,\"syncType\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("暂停成功"));

        // 验证调用
        verify(questionnaireService).pauseQuestionnaireInExternal(1L);
    }

    @Test
    void testSyncQuestionnaireStatus_Success() throws Exception {
        // 准备测试数据
        doNothing().when(questionnaireService).syncQuestionnaireStatus();

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire/sync-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("同步成功"));

        // 验证调用
        verify(questionnaireService).syncQuestionnaireStatus();
    }

    @Test
    void testTestQuestionnaireLink_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        when(questionnaireService.testQuestionnaireLink(questionnaireId)).thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire/test-link")
                .param("id", questionnaireId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireService).testQuestionnaireLink(questionnaireId);
    }

    @Test
    void testGetAccessStats_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        QuestionnaireAccessService.QuestionnaireAccessStats stats = 
                new QuestionnaireAccessService.QuestionnaireAccessStats();
        stats.setTotalAccess(100L);
        stats.setUniqueUsers(80L);
        stats.setAverageSessionDuration(300.0);
        
        when(questionnaireAccessService.getQuestionnaireAccessStats(eq(questionnaireId), any(), any()))
                .thenReturn(stats);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire/access-stats")
                .param("id", questionnaireId.toString())
                .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalAccess").value(100))
                .andExpect(jsonPath("$.data.uniqueUsers").value(80));

        // 验证调用
        verify(questionnaireAccessService).getQuestionnaireAccessStats(eq(questionnaireId), any(), any());
    }

    @Test
    void testGetAccessTrend_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        List<Map<String, Object>> trendData = Arrays.asList(
                createTrendDataPoint("2024-01-01", 10L),
                createTrendDataPoint("2024-01-02", 15L)
        );
        
        when(questionnaireAccessService.getQuestionnaireAccessTrend(questionnaireId, 7))
                .thenReturn(trendData);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire/access-trend")
                .param("id", questionnaireId.toString())
                .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 验证调用
        verify(questionnaireAccessService).getQuestionnaireAccessTrend(questionnaireId, 7);
    }

    @Test
    void testCheckAvailability_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        QuestionnaireAccessService.QuestionnaireAvailabilityResult result = 
                new QuestionnaireAccessService.QuestionnaireAvailabilityResult(true, "问卷可用");
        result.setStatusCode(200);
        result.setResponseTime(150L);
        
        when(questionnaireAccessService.checkQuestionnaireAvailability(questionnaireId))
                .thenReturn(result);

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/questionnaire/availability-check")
                .param("id", questionnaireId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.reason").value("问卷可用"))
                .andExpect(jsonPath("$.data.statusCode").value(200));

        // 验证调用
        verify(questionnaireAccessService).checkQuestionnaireAvailability(questionnaireId);
    }

    @Test
    void testBatchDeleteQuestionnaires_Success() throws Exception {
        // 准备测试数据
        doNothing().when(questionnaireService).deleteQuestionnaire(anyLong());

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire/batch-delete")
                .contentType("application/json")
                .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireService, times(3)).deleteQuestionnaire(anyLong());
    }

    @Test
    void testBatchPublishQuestionnaires_Success() throws Exception {
        // 准备测试数据
        doNothing().when(questionnaireService).publishQuestionnaireToExternal(anyLong());

        mockMvc = MockMvcBuilders.standaloneSetup(questionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/questionnaire/batch-publish")
                .contentType("application/json")
                .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.successCount").value(3))
                .andExpect(jsonPath("$.data.failureCount").value(0));

        // 验证调用
        verify(questionnaireService, times(3)).publishQuestionnaireToExternal(anyLong());
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
        questionnaire.setStatus(1);
        questionnaire.setIsOpen(1);
        questionnaire.setAccessCount(0);
        questionnaire.setCompletionCount(0);
        questionnaire.setSyncStatus(1);
        questionnaire.setCreateTime(LocalDateTime.now());
        questionnaire.setUpdateTime(LocalDateTime.now());
        return questionnaire;
    }

    private Map<String, Object> createTrendDataPoint(String date, Long count) {
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("date", date);
        dataPoint.put("accessCount", count);
        return dataPoint;
    }

}