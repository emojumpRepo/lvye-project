package com.lvye.mindtrip.module.psychology.service.questionnaire;

import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.QuestionnaireCreateReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.QuestionnairePageReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.QuestionnaireSimpleRespVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 问卷服务测试类
 */
@ExtendWith(MockitoExtension.class)
public class QuestionnaireServiceTest {

    @Mock
    private QuestionnaireMapper questionnaireMapper;

    @InjectMocks
    private QuestionnaireServiceImpl questionnaireService;

    @Test
    public void testCreateQuestionnaire_ShouldSetDefaultSupportIndependentUse() {
        // 准备测试数据
        QuestionnaireCreateReqVO createReqVO = new QuestionnaireCreateReqVO();
        createReqVO.setTitle("测试问卷");
        createReqVO.setQuestionnaireType(1);
        createReqVO.setTargetAudience(1);
        createReqVO.setQuestionCount(10);
        createReqVO.setEstimatedDuration(15);
        // 不设置 supportIndependentUse，应该使用默认值

        QuestionnaireDO savedQuestionnaire = new QuestionnaireDO();
        savedQuestionnaire.setId(1L);

        doAnswer(invocation -> {
            QuestionnaireDO questionnaire = invocation.getArgument(0);
            questionnaire.setId(1L);
            return 1;
        }).when(questionnaireMapper).insert(any(QuestionnaireDO.class));

        // 执行测试
        Long id = questionnaireService.createQuestionnaire(createReqVO);

        // 验证结果
        assertNotNull(id);
        verify(questionnaireMapper).insert(argThat((QuestionnaireDO questionnaire) ->
            questionnaire.getSupportIndependentUse() != null &&
            questionnaire.getSupportIndependentUse().equals(1)
        ));
    }

    @Test
    public void testGetSimpleQuestionnaireListWithFilter() {
        // 准备测试数据
        QuestionnaireDO questionnaire1 = new QuestionnaireDO();
        questionnaire1.setId(1L);
        questionnaire1.setTitle("支持独立使用的问卷");
        questionnaire1.setSupportIndependentUse(1);
        questionnaire1.setStatus(1);

        QuestionnaireDO questionnaire2 = new QuestionnaireDO();
        questionnaire2.setId(2L);
        questionnaire2.setTitle("不支持独立使用的问卷");
        questionnaire2.setSupportIndependentUse(0);
        questionnaire2.setStatus(1);

        when(questionnaireMapper.selectIndependentUseQuestionnaires(1))
            .thenReturn(Arrays.asList(questionnaire1));

        // 执行测试
        List<QuestionnaireSimpleRespVO> result = questionnaireService.getSimpleQuestionnaireList(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("支持独立使用的问卷", result.get(0).getTitle());
        assertEquals(Integer.valueOf(1), result.get(0).getSupportIndependentUse());

        verify(questionnaireMapper).selectIndependentUseQuestionnaires(1);
    }

    @Test
    public void testGetSimpleQuestionnaireListWithoutFilter() {
        // 准备测试数据
        QuestionnaireDO questionnaire1 = new QuestionnaireDO();
        questionnaire1.setId(1L);
        questionnaire1.setTitle("问卷1");
        questionnaire1.setSupportIndependentUse(1);

        QuestionnaireDO questionnaire2 = new QuestionnaireDO();
        questionnaire2.setId(2L);
        questionnaire2.setTitle("问卷2");
        questionnaire2.setSupportIndependentUse(0);

        when(questionnaireMapper.selectList())
            .thenReturn(Arrays.asList(questionnaire1, questionnaire2));

        // 执行测试
        List<QuestionnaireSimpleRespVO> result = questionnaireService.getSimpleQuestionnaireList();

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(questionnaireMapper).selectList();
    }
}
