package cn.iocoder.yudao.module.psychology.service.validation.impl;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.service.validation.DataValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 数据验证服务测试
 *
 * @author 芋道源码
 */
@Import(DataValidationServiceImpl.class)
class DataValidationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DataValidationService dataValidationService;

    @MockBean
    private QuestionnaireMapper questionnaireMapper;

    @MockBean
    private QuestionnaireResultMapper questionnaireResultMapper;

    @Test
    void testCheckDataIntegrity_Questionnaire_Success() {
        // 准备测试数据
        List<QuestionnaireDO> questionnaires = Arrays.asList(
                createValidQuestionnaire(1L, "问卷1"),
                createValidQuestionnaire(2L, "问卷2")
        );
        
        when(questionnaireMapper.selectList()).thenReturn(questionnaires);

        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.checkDataIntegrity("questionnaire");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getValidRecords()).isEqualTo(2);
        assertThat(result.getInvalidRecords()).isEqualTo(0);
        assertThat(result.getMessage()).contains("问卷完整性检查完成");
        assertThat(result.getErrors()).isEmpty();
        
        verify(questionnaireMapper).selectList();
    }

    @Test
    void testCheckDataIntegrity_Questionnaire_WithErrors() {
        // 准备测试数据 - 包含无效数据
        List<QuestionnaireDO> questionnaires = Arrays.asList(
                createValidQuestionnaire(1L, "问卷1"),
                createInvalidQuestionnaire(2L) // 缺少标题
        );
        
        when(questionnaireMapper.selectList()).thenReturn(questionnaires);

        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.checkDataIntegrity("questionnaire");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getValidRecords()).isEqualTo(1);
        assertThat(result.getInvalidRecords()).isEqualTo(1);
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("问卷标题不能为空");
    }

    @Test
    void testCheckDataIntegrity_QuestionnaireResult_Success() {
        // 准备测试数据
        List<QuestionnaireResultDO> results = Arrays.asList(
                createValidQuestionnaireResult(1L, 1L, 100L),
                createValidQuestionnaireResult(2L, 2L, 101L)
        );
        
        when(questionnaireResultMapper.selectList()).thenReturn(results);

        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.checkDataIntegrity("questionnaire_result");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getValidRecords()).isEqualTo(2);
        assertThat(result.getInvalidRecords()).isEqualTo(0);
        assertThat(result.getMessage()).contains("问卷结果完整性检查完成");
        
        verify(questionnaireResultMapper).selectList();
    }

    @Test
    void testCheckDataIntegrity_UnsupportedType() {
        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.checkDataIntegrity("unsupported_type");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("不支持的数据类型");
    }

    @Test
    void testValidateDataConsistency_Questionnaire_Success() {
        // 准备测试数据
        List<QuestionnaireDO> questionnaires = Arrays.asList(
                createValidQuestionnaire(1L, "问卷1"),
                createValidQuestionnaire(2L, "问卷2")
        );
        
        when(questionnaireMapper.selectList()).thenReturn(questionnaires);

        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.validateDataConsistency("questionnaire");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getValidRecords()).isEqualTo(2);
        assertThat(result.getMessage()).contains("问卷一致性验证完成");
    }

    @Test
    void testValidateDataConsistency_Questionnaire_WithInconsistentData() {
        // 准备测试数据 - 包含不一致数据
        QuestionnaireDO inconsistentQuestionnaire = createValidQuestionnaire(1L, "问卷1");
        inconsistentQuestionnaire.setQuestionCount(-5); // 无效的问题数量
        inconsistentQuestionnaire.setEstimatedDuration(0); // 无效的预估时长
        
        List<QuestionnaireDO> questionnaires = Arrays.asList(
                createValidQuestionnaire(2L, "问卷2"),
                inconsistentQuestionnaire
        );
        
        when(questionnaireMapper.selectList()).thenReturn(questionnaires);

        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.validateDataConsistency("questionnaire");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getValidRecords()).isEqualTo(1);
        assertThat(result.getInvalidRecords()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(2); // 两个字段错误
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("问题数量必须大于0");
        assertThat(result.getErrors().get(1).getErrorMessage()).contains("预估时长必须大于0");
    }

    @Test
    void testValidateDataConsistency_QuestionnaireResult_WithInconsistentData() {
        // 准备测试数据 - 包含不一致数据
        QuestionnaireResultDO inconsistentResult = createValidQuestionnaireResult(1L, 1L, 100L);
        inconsistentResult.setTotalScore(120); // 总分超过最高分
        inconsistentResult.setMaxScore(100);
        inconsistentResult.setRiskLevel(5); // 无效的风险等级
        
        List<QuestionnaireResultDO> results = Arrays.asList(
                createValidQuestionnaireResult(2L, 2L, 101L),
                inconsistentResult
        );
        
        when(questionnaireResultMapper.selectList()).thenReturn(results);

        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.validateDataConsistency("questionnaire_result");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getValidRecords()).isEqualTo(1);
        assertThat(result.getInvalidRecords()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(2); // 两个字段错误
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("总分不能超过最高分");
        assertThat(result.getErrors().get(1).getErrorMessage()).contains("风险等级必须在1-3之间");
    }

    @Test
    void testAssessDataQuality_Questionnaire() {
        // 准备测试数据
        List<QuestionnaireDO> questionnaires = Arrays.asList(
                createValidQuestionnaire(1L, "问卷1"),
                createPartialQuestionnaire(2L, "问卷2"), // 缺少描述
                createValidQuestionnaire(3L, "问卷3")
        );
        
        when(questionnaireMapper.selectList()).thenReturn(questionnaires);

        // 执行测试
        DataValidationService.DataQualityReport report = dataValidationService.assessDataQuality("questionnaire");

        // 验证结果
        assertThat(report).isNotNull();
        assertThat(report.getDataType()).isEqualTo("questionnaire");
        assertThat(report.getTotalRecords()).isEqualTo(3);
        assertThat(report.getOverallQualityScore()).isGreaterThan(0.0);
        assertThat(report.getFieldQualityScores()).isNotEmpty();
        assertThat(report.getFieldQualityScores()).containsKeys("title", "description", "externalLink");
        assertThat(report.getFieldQualityScores().get("title")).isEqualTo(100.0); // 所有问卷都有标题
        assertThat(report.getFieldQualityScores().get("description")).isLessThan(100.0); // 有问卷缺少描述
        assertThat(report.getRecommendations()).isNotEmpty();
    }

    @Test
    void testAssessDataQuality_QuestionnaireResult() {
        // 准备测试数据
        List<QuestionnaireResultDO> results = Arrays.asList(
                createValidQuestionnaireResult(1L, 1L, 100L),
                createPartialQuestionnaireResult(2L, 2L, 101L) // 缺少解读和建议
        );
        
        when(questionnaireResultMapper.selectList()).thenReturn(results);

        // 执行测试
        DataValidationService.DataQualityReport report = dataValidationService.assessDataQuality("questionnaire_result");

        // 验证结果
        assertThat(report).isNotNull();
        assertThat(report.getDataType()).isEqualTo("questionnaire_result");
        assertThat(report.getTotalRecords()).isEqualTo(2);
        assertThat(report.getOverallQualityScore()).isGreaterThan(0.0);
        assertThat(report.getFieldQualityScores()).containsKeys("totalScore", "resultInterpretation", "suggestions");
        assertThat(report.getFieldQualityScores().get("totalScore")).isEqualTo(100.0); // 所有结果都有总分
        assertThat(report.getRecommendations()).isNotEmpty();
    }

    @Test
    void testValidateField() {
        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.validateField("questionnaire", "title");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("字段验证完成");
    }

    @Test
    void testValidateBusinessRules() {
        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.validateBusinessRules("questionnaire", Arrays.asList("rule1", "rule2"));

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("业务规则验证完成");
    }

    @Test
    void testValidateDataRelationships() {
        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.validateDataRelationships("questionnaire", "questionnaire_result");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("关联性验证完成");
    }

    @Test
    void testDetectDuplicateData() {
        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.detectDuplicateData("questionnaire", Arrays.asList("title", "externalLink"));

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("重复数据检测完成");
    }

    @Test
    void testValidateDataRange() {
        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.validateDataRange("questionnaire", "questionCount", 1, 100);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("范围验证完成");
    }

    @Test
    void testValidateDataFormat() {
        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.validateDataFormat("questionnaire", "externalLink", "url");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("格式验证完成");
    }

    @Test
    void testGenerateValidationReport() {
        // 准备测试数据
        List<DataValidationService.ValidationResult> results = Arrays.asList(
                new DataValidationService.ValidationResult(true, "测试1通过"),
                new DataValidationService.ValidationResult(false, "测试2失败")
        );
        
        results.get(0).setTotalRecords(10);
        results.get(0).setValidRecords(10);
        results.get(0).setInvalidRecords(0);
        results.get(0).setDuration(100L);
        
        results.get(1).setTotalRecords(5);
        results.get(1).setValidRecords(3);
        results.get(1).setInvalidRecords(2);
        results.get(1).setDuration(200L);
        results.get(1).setErrors(Arrays.asList(
                new DataValidationService.ValidationError("1", "field1", "ERROR", "错误信息1")
        ));

        // 执行测试
        String report = dataValidationService.generateValidationReport(results);

        // 验证结果
        assertThat(report).isNotNull();
        assertThat(report).contains("数据验证报告");
        assertThat(report).contains("总测试数: 2");
        assertThat(report).contains("通过测试: 1");
        assertThat(report).contains("失败测试: 1");
        assertThat(report).contains("通过率: 50.00%");
        assertThat(report).contains("测试1通过");
        assertThat(report).contains("测试2失败");
        assertThat(report).contains("错误信息1");
    }

    @Test
    void testGetDataStatistics_Questionnaire() {
        // 准备测试数据
        when(questionnaireMapper.selectCount()).thenReturn(25);

        // 执行测试
        Map<String, Object> statistics = dataValidationService.getDataStatistics("questionnaire");

        // 验证结果
        assertThat(statistics).isNotNull();
        assertThat(statistics.get("totalCount")).isEqualTo(25);
        assertThat(statistics.get("dataType")).isEqualTo("questionnaire");
        assertThat(statistics.get("lastUpdated")).isNotNull();
        
        verify(questionnaireMapper).selectCount();
    }

    @Test
    void testGetDataStatistics_QuestionnaireResult() {
        // 准备测试数据
        when(questionnaireResultMapper.selectCount()).thenReturn(150);

        // 执行测试
        Map<String, Object> statistics = dataValidationService.getDataStatistics("questionnaire_result");

        // 验证结果
        assertThat(statistics).isNotNull();
        assertThat(statistics.get("totalCount")).isEqualTo(150);
        assertThat(statistics.get("dataType")).isEqualTo("questionnaire_result");
        assertThat(statistics.get("lastUpdated")).isNotNull();
        
        verify(questionnaireResultMapper).selectCount();
    }

    @Test
    void testGetDataStatistics_UnsupportedType() {
        // 执行测试
        Map<String, Object> statistics = dataValidationService.getDataStatistics("unsupported_type");

        // 验证结果
        assertThat(statistics).isNotNull();
        assertThat(statistics.get("error")).isEqualTo("不支持的数据类型: unsupported_type");
    }

    @Test
    void testFixDataIssues_DryRun() {
        // 准备测试数据
        Map<String, Object> fixRules = new HashMap<>();
        fixRules.put("rule1", "value1");

        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.fixDataIssues("questionnaire", fixRules, true);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("试运行完成");
    }

    @Test
    void testFixDataIssues_ActualFix() {
        // 准备测试数据
        Map<String, Object> fixRules = new HashMap<>();
        fixRules.put("rule1", "value1");

        // 执行测试
        DataValidationService.ValidationResult result = dataValidationService.fixDataIssues("questionnaire", fixRules, false);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("修复完成");
    }

    // 辅助方法

    private QuestionnaireDO createValidQuestionnaire(Long id, String title) {
        QuestionnaireDO questionnaire = new QuestionnaireDO();
        questionnaire.setId(id);
        questionnaire.setTitle(title);
        questionnaire.setDescription("测试问卷描述");
        questionnaire.setQuestionnaireType(1);
        questionnaire.setTargetAudience(1);
        questionnaire.setQuestionCount(20);
        questionnaire.setEstimatedDuration(15);
        questionnaire.setExternalLink("https://example.com/survey/" + id);
        questionnaire.setStatus(1);
        questionnaire.setCreateTime(LocalDateTime.now());
        return questionnaire;
    }

    private QuestionnaireDO createInvalidQuestionnaire(Long id) {
        QuestionnaireDO questionnaire = new QuestionnaireDO();
        questionnaire.setId(id);
        questionnaire.setTitle(""); // 空标题
        questionnaire.setQuestionnaireType(null); // 空类型
        questionnaire.setExternalLink(null); // 空链接
        questionnaire.setCreateTime(LocalDateTime.now());
        return questionnaire;
    }

    private QuestionnaireDO createPartialQuestionnaire(Long id, String title) {
        QuestionnaireDO questionnaire = new QuestionnaireDO();
        questionnaire.setId(id);
        questionnaire.setTitle(title);
        questionnaire.setDescription(null); // 缺少描述
        questionnaire.setQuestionnaireType(1);
        questionnaire.setQuestionCount(20);
        questionnaire.setEstimatedDuration(15);
        questionnaire.setExternalLink("https://example.com/survey/" + id);
        questionnaire.setStatus(1);
        questionnaire.setCreateTime(LocalDateTime.now());
        return questionnaire;
    }

    private QuestionnaireResultDO createValidQuestionnaireResult(Long id, Long questionnaireId, Long studentProfileId) {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(id);
        result.setQuestionnaireId(questionnaireId);
        result.setStudentProfileId(studentProfileId);
        result.setTotalScore(85);
        result.setMaxScore(100);
        result.setRiskLevel(2);
        result.setResultStatus(2);
        result.setResultInterpretation("测试解读");
        result.setSuggestions("测试建议");
        result.setAnswerDuration(900);
        result.setCreateTime(LocalDateTime.now());
        result.setCompleteTime(LocalDateTime.now());
        return result;
    }

    private QuestionnaireResultDO createPartialQuestionnaireResult(Long id, Long questionnaireId, Long studentProfileId) {
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setId(id);
        result.setQuestionnaireId(questionnaireId);
        result.setStudentProfileId(studentProfileId);
        result.setTotalScore(85);
        result.setMaxScore(100);
        result.setRiskLevel(2);
        result.setResultStatus(2);
        result.setResultInterpretation(null); // 缺少解读
        result.setSuggestions(""); // 空建议
        result.setAnswerDuration(900);
        result.setCreateTime(LocalDateTime.now());
        result.setCompleteTime(LocalDateTime.now());
        return result;
    }

}