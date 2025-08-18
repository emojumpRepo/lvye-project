package cn.iocoder.yudao.module.psychology.controller.app.questionnaire;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireAccessDO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 学生问卷访问API控制器测试
 *
 * @author 芋道源码
 */
@Import(AppQuestionnaireController.class)
class AppQuestionnaireControllerTest extends BaseDbUnitTest {

    @Resource
    private AppQuestionnaireController appQuestionnaireController;

    @MockBean
    private QuestionnaireService questionnaireService;

    @MockBean
    private QuestionnaireAccessService questionnaireAccessService;

    @MockBean
    private QuestionnaireResultService questionnaireResultService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetAvailableQuestionnaireList_Success() throws Exception {
        // 准备测试数据
        Long studentProfileId = 100L;
        Integer targetAudience = 1;
        
        List<QuestionnaireDO> questionnaires = Arrays.asList(
                createTestQuestionnaire(1L, "问卷1"),
                createTestQuestionnaire(2L, "问卷2")
        );
        
        when(questionnaireService.getAvailableQuestionnaires(targetAudience)).thenReturn(questionnaires);
        when(questionnaireResultService.hasUserCompletedQuestionnaire(anyLong(), eq(studentProfileId)))
                .thenReturn(false, true); // 第一个未完成，第二个已完成
        when(questionnaireAccessService.checkQuestionnaireAccess(anyLong(), eq(studentProfileId)))
                .thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire/available-list")
                .param("studentProfileId", studentProfileId.toString())
                .param("targetAudience", targetAudience.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].completed").value(false))
                .andExpect(jsonPath("$.data[1].completed").value(true))
                .andExpect(jsonPath("$.data[0].accessible").value(true))
                .andExpect(jsonPath("$.data[1].accessible").value(true));

        // 验证调用
        verify(questionnaireService).getAvailableQuestionnaires(targetAudience);
        verify(questionnaireResultService, times(2)).hasUserCompletedQuestionnaire(anyLong(), eq(studentProfileId));
        verify(questionnaireAccessService, times(2)).checkQuestionnaireAccess(anyLong(), eq(studentProfileId));
    }

    @Test
    void testGetQuestionnaire_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        QuestionnaireDO questionnaire = createTestQuestionnaire(questionnaireId, "测试问卷");
        
        when(questionnaireService.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        when(questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId))
                .thenReturn(false);
        when(questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId))
                .thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire/get")
                .param("id", questionnaireId.toString())
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(questionnaireId))
                .andExpect(jsonPath("$.data.title").value("测试问卷"))
                .andExpect(jsonPath("$.data.completed").value(false))
                .andExpect(jsonPath("$.data.accessible").value(true));

        // 验证调用
        verify(questionnaireService).getQuestionnaire(questionnaireId);
        verify(questionnaireResultService).hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
        verify(questionnaireAccessService).checkQuestionnaireAccess(questionnaireId, studentProfileId);
    }

    @Test
    void testGetQuestionnaire_NotFound() throws Exception {
        // 准备测试数据
        Long questionnaireId = 999L;
        Long studentProfileId = 100L;
        
        when(questionnaireService.getQuestionnaire(questionnaireId)).thenReturn(null);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire/get")
                .param("id", questionnaireId.toString())
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isEmpty());

        // 验证调用
        verify(questionnaireService).getQuestionnaire(questionnaireId);
        verify(questionnaireResultService, never()).hasUserCompletedQuestionnaire(anyLong(), anyLong());
        verify(questionnaireAccessService, never()).checkQuestionnaireAccess(anyLong(), anyLong());
    }

    @Test
    void testAccessQuestionnaire_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        Long accessId = 1001L;
        
        AppQuestionnaireAccessReqVO accessReqVO = new AppQuestionnaireAccessReqVO();
        accessReqVO.setQuestionnaireId(questionnaireId);
        accessReqVO.setStudentProfileId(studentProfileId);
        accessReqVO.setAccessSource(1);
        accessReqVO.setUserAgent("Mozilla/5.0");
        
        QuestionnaireDO questionnaire = createTestQuestionnaire(questionnaireId, "测试问卷");
        
        when(questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId))
                .thenReturn(true);
        when(questionnaireService.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        when(questionnaireAccessService.recordQuestionnaireAccess(
                eq(questionnaireId), eq(studentProfileId), anyString(), anyString(), eq(1)))
                .thenReturn(accessId);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessReqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessId").value(accessId))
                .andExpect(jsonPath("$.data.questionnaireId").value(questionnaireId))
                .andExpect(jsonPath("$.data.accessible").value(true))
                .andExpect(jsonPath("$.data.statusMessage").value("访问成功"));

        // 验证调用
        verify(questionnaireAccessService).checkQuestionnaireAccess(questionnaireId, studentProfileId);
        verify(questionnaireService).getQuestionnaire(questionnaireId);
        verify(questionnaireAccessService).recordQuestionnaireAccess(
                eq(questionnaireId), eq(studentProfileId), anyString(), anyString(), eq(1));
    }

    @Test
    void testAccessQuestionnaire_NoPermission() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        AppQuestionnaireAccessReqVO accessReqVO = new AppQuestionnaireAccessReqVO();
        accessReqVO.setQuestionnaireId(questionnaireId);
        accessReqVO.setStudentProfileId(studentProfileId);
        accessReqVO.setAccessSource(1);
        
        when(questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId))
                .thenReturn(false);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessReqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessible").value(false))
                .andExpect(jsonPath("$.data.statusMessage").value("无权限访问该问卷"));

        // 验证调用
        verify(questionnaireAccessService).checkQuestionnaireAccess(questionnaireId, studentProfileId);
        verify(questionnaireService, never()).getQuestionnaire(anyLong());
        verify(questionnaireAccessService, never()).recordQuestionnaireAccess(anyLong(), anyLong(), anyString(), anyString(), anyInt());
    }

    @Test
    void testAccessQuestionnaire_QuestionnaireNotFound() throws Exception {
        // 准备测试数据
        Long questionnaireId = 999L;
        Long studentProfileId = 100L;
        
        AppQuestionnaireAccessReqVO accessReqVO = new AppQuestionnaireAccessReqVO();
        accessReqVO.setQuestionnaireId(questionnaireId);
        accessReqVO.setStudentProfileId(studentProfileId);
        accessReqVO.setAccessSource(1);
        
        when(questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId))
                .thenReturn(true);
        when(questionnaireService.getQuestionnaire(questionnaireId)).thenReturn(null);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessReqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessible").value(false))
                .andExpect(jsonPath("$.data.statusMessage").value("问卷不存在"));

        // 验证调用
        verify(questionnaireAccessService).checkQuestionnaireAccess(questionnaireId, studentProfileId);
        verify(questionnaireService).getQuestionnaire(questionnaireId);
        verify(questionnaireAccessService, never()).recordQuestionnaireAccess(anyLong(), anyLong(), anyString(), anyString(), anyInt());
    }

    @Test
    void testUpdateSessionDuration_Success() throws Exception {
        // 准备测试数据
        Long accessId = 1001L;
        Integer sessionDuration = 300; // 5分钟
        
        doNothing().when(questionnaireAccessService).updateSessionDuration(accessId, sessionDuration);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(post("/psychology/app/questionnaire/update-session")
                .param("accessId", accessId.toString())
                .param("sessionDuration", sessionDuration.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireAccessService).updateSessionDuration(accessId, sessionDuration);
    }

    @Test
    void testCheckQuestionnaireCompletion_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        when(questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId))
                .thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire/completion-check")
                .param("questionnaireId", questionnaireId.toString())
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireResultService).hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
    }

    @Test
    void testCheckQuestionnaireAccess_Success() throws Exception {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        when(questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId))
                .thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire/access-check")
                .param("questionnaireId", questionnaireId.toString())
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        // 验证调用
        verify(questionnaireAccessService).checkQuestionnaireAccess(questionnaireId, studentProfileId);
    }

    @Test
    void testGetRecommendedQuestionnaires_Success() throws Exception {
        // 准备测试数据
        Long studentProfileId = 100L;
        Integer limit = 3;
        
        List<QuestionnaireDO> questionnaires = Arrays.asList(
                createTestQuestionnaire(1L, "推荐问卷1"),
                createTestQuestionnaire(2L, "推荐问卷2"),
                createTestQuestionnaire(3L, "推荐问卷3")
        );
        
        when(questionnaireService.getAvailableQuestionnaires(null)).thenReturn(questionnaires);
        when(questionnaireResultService.hasUserCompletedQuestionnaire(anyLong(), eq(studentProfileId)))
                .thenReturn(false); // 都未完成
        when(questionnaireAccessService.checkQuestionnaireAccess(anyLong(), eq(studentProfileId)))
                .thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire/recommended")
                .param("studentProfileId", studentProfileId.toString())
                .param("limit", limit.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));

        // 验证调用
        verify(questionnaireService).getAvailableQuestionnaires(null);
        verify(questionnaireResultService, times(3)).hasUserCompletedQuestionnaire(anyLong(), eq(studentProfileId));
        verify(questionnaireAccessService, times(3)).checkQuestionnaireAccess(anyLong(), eq(studentProfileId));
    }

    @Test
    void testGetMyQuestionnaireStatistics_Success() throws Exception {
        // 准备测试数据
        Long studentProfileId = 100L;
        
        List<QuestionnaireDO> questionnaires = Arrays.asList(
                createTestQuestionnaire(1L, "问卷1"),
                createTestQuestionnaire(2L, "问卷2"),
                createTestQuestionnaire(3L, "问卷3")
        );
        
        when(questionnaireService.getAvailableQuestionnaires(null)).thenReturn(questionnaires);
        when(questionnaireResultService.hasUserCompletedQuestionnaire(anyLong(), eq(studentProfileId)))
                .thenReturn(true, false, true); // 完成了2个

        mockMvc = MockMvcBuilders.standaloneSetup(appQuestionnaireController).build();

        // 执行测试
        mockMvc.perform(get("/psychology/app/questionnaire/statistics")
                .param("studentProfileId", studentProfileId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalAvailable").value(3))
                .andExpect(jsonPath("$.data.completed").value(2))
                .andExpect(jsonPath("$.data.pending").value(1));

        // 验证调用
        verify(questionnaireService).getAvailableQuestionnaires(null);
        verify(questionnaireResultService, times(3)).hasUserCompletedQuestionnaire(anyLong(), eq(studentProfileId));
    }

    // 辅助方法

    private QuestionnaireDO createTestQuestionnaire(Long id, String title) {
        QuestionnaireDO questionnaire = new QuestionnaireDO();
        questionnaire.setId(id);
        questionnaire.setTitle(title);
        questionnaire.setDescription("测试问卷描述");
        questionnaire.setQuestionnaireType(1);
        questionnaire.setQuestionCount(20);
        questionnaire.setEstimatedDuration(15);
        questionnaire.setExternalLink("https://example.com/survey/" + id);
        questionnaire.setStatus(1); // 启用状态
        questionnaire.setCreateTime(LocalDateTime.now());
        return questionnaire;
    }

    private QuestionnaireAccessDO createTestAccess(Long id, Long questionnaireId, Long studentProfileId) {
        QuestionnaireAccessDO access = new QuestionnaireAccessDO();
        access.setId(id);
        access.setQuestionnaireId(questionnaireId);
        access.setStudentProfileId(studentProfileId);
        access.setAccessTime(LocalDateTime.now());
        access.setClientIp("127.0.0.1");
        access.setUserAgent("Mozilla/5.0");
        access.setAccessSource(1);
        return access;
    }

}