package cn.iocoder.yudao.module.psychology.framework.resultgenerator;

import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.AnswerVO;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.QuestionnaireResultVO;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果生成器测试基础类
 *
 * @author 芋道源码
 */
public abstract class ResultGeneratorTestBase {

    protected ResultGenerationContext.ResultGenerationContextBuilder contextBuilder;

    @BeforeEach
    void setUp() {
        contextBuilder = ResultGenerationContext.builder();
    }

    /**
     * 创建测试答题数据
     */
    protected List<AnswerVO> createTestAnswers() {
        List<AnswerVO> answers = new ArrayList<>();
        
        AnswerVO answer1 = new AnswerVO();
        answer1.setQuestionId("Q001");
        answer1.setQuestionType("SINGLE_CHOICE");
        answer1.setAnswerContent("A");
        answer1.setAnswerScore(3);
        answer1.setDimensionCode("EMOTION");
        answers.add(answer1);

        AnswerVO answer2 = new AnswerVO();
        answer2.setQuestionId("Q002");
        answer2.setQuestionType("SINGLE_CHOICE");
        answer2.setAnswerContent("B");
        answer2.setAnswerScore(2);
        answer2.setDimensionCode("BEHAVIOR");
        answers.add(answer2);

        return answers;
    }

    /**
     * 创建测试问卷结果数据
     */
    protected List<QuestionnaireResultVO> createTestQuestionnaireResults() {
        List<QuestionnaireResultVO> results = new ArrayList<>();
        
        Map<String, BigDecimal> dimensionScores1 = new HashMap<>();
        dimensionScores1.put("EMOTION", new BigDecimal("75.5"));
        dimensionScores1.put("BEHAVIOR", new BigDecimal("68.2"));
        
        QuestionnaireResultVO result1 = QuestionnaireResultVO.builder()
                .questionnaireId(1L)
                .rawScore(new BigDecimal("85.0"))
                .standardScore(new BigDecimal("72.3"))
                .riskLevel(2)
                .dimensionScores(dimensionScores1)
                .build();
        results.add(result1);

        return results;
    }

}