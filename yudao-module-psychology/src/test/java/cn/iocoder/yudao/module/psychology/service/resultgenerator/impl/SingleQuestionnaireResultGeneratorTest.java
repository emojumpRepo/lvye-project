package cn.iocoder.yudao.module.psychology.service.resultgenerator.impl;

import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorTestBase;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.QuestionnaireResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单问卷结果生成器测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class SingleQuestionnaireResultGeneratorTest extends ResultGeneratorTestBase {

    @InjectMocks
    private SingleQuestionnaireResultGenerator generator;

    @Test
    void testGetGeneratorType() {
        assertEquals(ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE, generator.getGeneratorType());
    }

    @Test
    void testSupports() {
        assertTrue(generator.supports(1L, ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE));
        assertFalse(generator.supports(1L, ResultGeneratorTypeEnum.COMBINED_ASSESSMENT));
        assertFalse(generator.supports(null, ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE));
    }

    @Test
    void testGenerateResult() {
        // 准备测试数据
        ResultGenerationContext context = contextBuilder
                .generationType(ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE)
                .questionnaireId(1L)
                .studentProfileId(100L)
                .answers(createTestAnswers())
                .participantType(1)
                .build();

        // 执行生成
        QuestionnaireResultVO result = generator.generateResult(context);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getQuestionnaireId());
        assertNotNull(result.getRawScore());
        assertNotNull(result.getStandardScore());
        assertNotNull(result.getRiskLevel());
        assertNotNull(result.getDimensionScores());
        assertNotNull(result.getReportContent());
        assertNotNull(result.getSuggestions());
    }

    @Test
    void testValidateGenerationParams_Success() {
        ResultGenerationContext context = contextBuilder
                .questionnaireId(1L)
                .studentProfileId(100L)
                .answers(createTestAnswers())
                .build();

        assertDoesNotThrow(() -> generator.validateGenerationParams(context));
    }

    @Test
    void testValidateGenerationParams_MissingQuestionnaireId() {
        ResultGenerationContext context = contextBuilder
                .studentProfileId(100L)
                .answers(createTestAnswers())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.validateGenerationParams(context));
        assertEquals("问卷ID不能为空", exception.getMessage());
    }

    @Test
    void testValidateGenerationParams_MissingAnswers() {
        ResultGenerationContext context = contextBuilder
                .questionnaireId(1L)
                .studentProfileId(100L)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.validateGenerationParams(context));
        assertEquals("答题数据不能为空", exception.getMessage());
    }

    @Test
    void testValidateGenerationParams_MissingStudentProfileId() {
        ResultGenerationContext context = contextBuilder
                .questionnaireId(1L)
                .answers(createTestAnswers())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.validateGenerationParams(context));
        assertEquals("学生档案ID不能为空", exception.getMessage());
    }

}