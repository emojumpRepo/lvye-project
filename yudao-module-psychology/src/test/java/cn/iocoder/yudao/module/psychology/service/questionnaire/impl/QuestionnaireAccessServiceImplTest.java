package cn.iocoder.yudao.module.psychology.service.questionnaire.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireAccessDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireAccessMapper;
import cn.iocoder.yudao.module.psychology.enums.QuestionnaireStatusEnum;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessServiceImpl;
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
import static org.mockito.Mockito.*;

/**
 * 问卷访问服务测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class QuestionnaireAccessServiceImplTest {

    @Mock
    private QuestionnaireAccessMapper questionnaireAccessMapper;

    @Mock
    private QuestionnaireMapper questionnaireMapper;

    @InjectMocks
    private QuestionnaireAccessServiceImpl accessService;

    @Test
    void testRecordQuestionnaireAccess_Success() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        String accessIp = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        Integer accessSource = 1;
        
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        questionnaire.setStatus(QuestionnaireStatusEnum.PUBLISHED.getStatus());
        
        when(questionnaireMapper.selectById(questionnaireId)).thenReturn(questionnaire);
        when(questionnaireAccessMapper.insert(any(QuestionnaireAccessDO.class))).thenReturn(1);
        doNothing().when(questionnaireMapper).updateAccessCount(questionnaireId);
        
        // 执行测试
        Long result = accessService.recordQuestionnaireAccess(
                questionnaireId, studentProfileId, accessIp, userAgent, accessSource);
        
        // 验证结果
        assertNotNull(result);
        verify(questionnaireMapper).selectById(questionnaireId);
        verify(questionnaireAccessMapper).insert(any(QuestionnaireAccessDO.class));
        verify(questionnaireMapper).updateAccessCount(questionnaireId);
    }

    @Test
    void testUpdateSessionDuration_Success() {
        // 准备测试数据
        Long accessId = 1L;
        Integer sessionDuration = 300;
        
        when(questionnaireAccessMapper.updateById(any(QuestionnaireAccessDO.class))).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> accessService.updateSessionDuration(accessId, sessionDuration));
        
        // 验证结果
        verify(questionnaireAccessMapper).updateById(any(QuestionnaireAccessDO.class));
    }

    @Test
    void testGetQuestionnaireAccessPage_Success() {
        // 准备测试数据
        Object pageReqVO = new Object();
        PageResult<QuestionnaireAccessDO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(createTestAccessRecord()));
        pageResult.setTotal(1L);
        
        when(questionnaireAccessMapper.selectPage(pageReqVO)).thenReturn(pageResult);
        
        // 执行测试
        PageResult<QuestionnaireAccessDO> result = accessService.getQuestionnaireAccessPage(pageReqVO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertFalse(result.getList().isEmpty());
        verify(questionnaireAccessMapper).selectPage(pageReqVO);
    }

    @Test
    void testGetQuestionnaireAccessList_Success() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        List<QuestionnaireAccessDO> accessList = Arrays.asList(createTestAccessRecord());
        
        when(questionnaireAccessMapper.selectListByQuestionnaireAndStudent(questionnaireId, studentProfileId))
                .thenReturn(accessList);
        
        // 执行测试
        List<QuestionnaireAccessDO> result = accessService.getQuestionnaireAccessList(questionnaireId, studentProfileId);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(questionnaireAccessMapper).selectListByQuestionnaireAndStudent(questionnaireId, studentProfileId);
    }

    @Test
    void testCheckQuestionnaireAccess_Success() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        questionnaire.setStatus(QuestionnaireStatusEnum.PUBLISHED.getStatus());
        questionnaire.setIsOpen(1);
        
        when(questionnaireMapper.selectById(questionnaireId)).thenReturn(questionnaire);
        
        // 执行测试
        boolean result = accessService.checkQuestionnaireAccess(questionnaireId, studentProfileId);
        
        // 验证结果
        assertTrue(result);
        verify(questionnaireMapper).selectById(questionnaireId);
    }

    @Test
    void testCheckQuestionnaireAccess_NotPublished() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Long studentProfileId = 100L;
        
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        questionnaire.setStatus(QuestionnaireStatusEnum.DRAFT.getStatus());
        
        when(questionnaireMapper.selectById(questionnaireId)).thenReturn(questionnaire);
        
        // 执行测试
        boolean result = accessService.checkQuestionnaireAccess(questionnaireId, studentProfileId);
        
        // 验证结果
        assertFalse(result);
        verify(questionnaireMapper).selectById(questionnaireId);
    }

    @Test
    void testGetQuestionnaireAccessStats_Success() {
        // 准备测试数据
        Long questionnaireId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        
        when(questionnaireAccessMapper.countByQuestionnaireAndTimeRange(eq(questionnaireId), any(), any()))
                .thenReturn(100L, 10L, 50L, 30L); // 总访问、今日、本周、本月
        when(questionnaireAccessMapper.countUniqueUsersByQuestionnaireAndTimeRange(eq(questionnaireId), any(), any()))
                .thenReturn(80L);
        when(questionnaireAccessMapper.getAverageSessionDurationByQuestionnaireAndTimeRange(eq(questionnaireId), any(), any()))
                .thenReturn(250.5);
        
        // 执行测试
        QuestionnaireAccessService.QuestionnaireAccessStats result = 
                accessService.getQuestionnaireAccessStats(questionnaireId, startTime, endTime);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.getTotalAccess());
        assertEquals(80L, result.getUniqueUsers());
        assertEquals(250.5, result.getAverageSessionDuration());
        assertEquals(10L, result.getTodayAccess());
        assertEquals(50L, result.getWeekAccess());
        assertEquals(30L, result.getMonthAccess());
    }

    @Test
    void testGetQuestionnaireAccessTrend_Success() {
        // 准备测试数据
        Long questionnaireId = 1L;
        Integer days = 7;
        
        when(questionnaireAccessMapper.countByQuestionnaireAndTimeRange(eq(questionnaireId), any(), any()))
                .thenReturn(10L, 15L, 8L, 20L, 12L, 18L, 25L);
        
        // 执行测试
        List<Map<String, Object>> result = accessService.getQuestionnaireAccessTrend(questionnaireId, days);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(days, result.size());
        
        for (Map<String, Object> dayData : result) {
            assertTrue(dayData.containsKey("date"));
            assertTrue(dayData.containsKey("accessCount"));
            assertTrue(dayData.containsKey("dayOfWeek"));
        }
    }

    @Test
    void testGetPopularQuestionnaires_Success() {
        // 准备测试数据
        Integer limit = 10;
        Integer days = 7;
        
        List<Map<String, Object>> popularList = Arrays.asList(
                createPopularQuestionnaireData(1L, "问卷1", 100L),
                createPopularQuestionnaireData(2L, "问卷2", 80L)
        );
        
        when(questionnaireAccessMapper.selectPopularQuestionnaires(eq(limit), any(), any()))
                .thenReturn(popularList);
        
        // 执行测试
        List<Map<String, Object>> result = accessService.getPopularQuestionnaires(limit, days);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(questionnaireAccessMapper).selectPopularQuestionnaires(eq(limit), any(), any());
    }

    @Test
    void testCheckQuestionnaireAvailability_Available() {
        // 准备测试数据
        Long questionnaireId = 1L;
        
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        questionnaire.setStatus(QuestionnaireStatusEnum.PUBLISHED.getStatus());
        questionnaire.setIsOpen(1);
        questionnaire.setExternalLink("https://example.com/survey/1");
        
        when(questionnaireMapper.selectById(questionnaireId)).thenReturn(questionnaire);
        
        // 执行测试
        QuestionnaireAccessService.QuestionnaireAvailabilityResult result = 
                accessService.checkQuestionnaireAvailability(questionnaireId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isAvailable());
        assertEquals("问卷可用", result.getReason());
        assertEquals(200, result.getStatusCode());
        assertNotNull(result.getResponseTime());
        verify(questionnaireMapper).selectById(questionnaireId);
    }

    @Test
    void testCheckQuestionnaireAvailability_NotPublished() {
        // 准备测试数据
        Long questionnaireId = 1L;
        
        QuestionnaireDO questionnaire = createTestQuestionnaire();
        questionnaire.setStatus(QuestionnaireStatusEnum.DRAFT.getStatus());
        
        when(questionnaireMapper.selectById(questionnaireId)).thenReturn(questionnaire);
        
        // 执行测试
        QuestionnaireAccessService.QuestionnaireAvailabilityResult result = 
                accessService.checkQuestionnaireAvailability(questionnaireId);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isAvailable());
        assertEquals("问卷未发布", result.getReason());
        verify(questionnaireMapper).selectById(questionnaireId);
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

    private QuestionnaireAccessDO createTestAccessRecord() {
        QuestionnaireAccessDO accessRecord = new QuestionnaireAccessDO();
        accessRecord.setId(1L);
        accessRecord.setQuestionnaireId(1L);
        accessRecord.setStudentProfileId(100L);
        accessRecord.setAccessIp("192.168.1.1");
        accessRecord.setUserAgent("Mozilla/5.0");
        accessRecord.setAccessSource(1);
        accessRecord.setAccessTime(LocalDateTime.now());
        accessRecord.setSessionDuration(300);
        return accessRecord;
    }

    private Map<String, Object> createPopularQuestionnaireData(Long id, String title, Long accessCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("questionnaireId", id);
        data.put("title", title);
        data.put("accessCount", accessCount);
        return data;
    }

}