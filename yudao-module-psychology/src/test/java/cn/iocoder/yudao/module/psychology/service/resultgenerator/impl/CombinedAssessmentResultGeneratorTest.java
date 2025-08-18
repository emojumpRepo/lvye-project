package cn.iocoder.yudao.module.psychology.service.resultgenerator.impl;

import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorTestBase;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.AssessmentResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 组合测评结果生成器测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class CombinedAssessmentResultGeneratorTest extends ResultGeneratorTestBase {

    @InjectMocks
    private CombinedAssessmentResultGenerator generator;

    @Test
    void testGetGeneratorType() {
        assertEquals(ResultGeneratorTypeEnum.COMBINED_ASSESSMENT, generator.getGeneratorType());
    }

    @Test
    void testSupports() {
        assertTrue(generator.supports(1L, ResultGeneratorTypeEnum.COMBINED_ASSESSMENT));
        assertFalse(generator.supports(1L, ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE));
        assertFalse(generator.supports(null, ResultGeneratorTypeEnum.COMBINED_ASSESSMENT));
    }

    @Test
    void testGenerateResult() {
        // 准备测试数据
        ResultGenerationContext context = contextBuilder
                .generationType(ResultGeneratorTypeEnum.COMBINED_ASSESSMENT)
                .assessmentId(1L)
                .studentProfileId(100L)
                .questionnaireResults(createTestQuestionnaireResults())
                .participantType(1)
                .build();

        // 执行生成
        AssessmentResultVO result = generator.generateResult(context);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getAssessmentId());
        assertNotNull(result.getCombinedScore());
        assertNotNull(result.getCombinedRiskLevel());
        assertNotNull(result.getRiskFactors());
        assertNotNull(result.getInterventionSuggestions());
        assertNotNull(result.getComprehensiveReport());
        assertNotNull(result.getQuestionnaireResults());
        assertFalse(result.getQuestionnaireResults().isEmpty());
    }

    @Test
    void testValidateGenerationParams_Success() {
        ResultGenerationContext context = contextBuilder
                .assessmentId(1L)
                .studentProfileId(100L)
                .questionnaireResults(createTestQuestionnaireResults())
                .build();

        assertDoesNotThrow(() -> generator.validateGenerationParams(context));
    }

    @Test
    void testValidateGenerationParams_MissingAssessmentId() {
        ResultGenerationContext context = contextBuilder
                .studentProfileId(100L)
                .questionnaireResults(createTestQuestionnaireResults())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.validateGenerationParams(context));
        assertEquals("测评ID不能为空", exception.getMessage());
    }

    @Test
    void testValidateGenerationParams_MissingQuestionnaireResults() {
        ResultGenerationContext context = contextBuilder
                .assessmentId(1L)
                .studentProfileId(100L)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.validateGenerationParams(context));
        assertEquals("问卷结果数据不能为空", exception.getMessage());
    }

    @Test
    void testValidateGenerationParams_MissingStudentProfileId() {
        ResultGenerationContext context = contextBuilder
                .assessmentId(1L)
                .questionnaireResults(createTestQuestionnaireResults())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.validateGenerationParams(context));
        assertEquals("学生档案ID不能为空", exception.getMessage());
    }

}