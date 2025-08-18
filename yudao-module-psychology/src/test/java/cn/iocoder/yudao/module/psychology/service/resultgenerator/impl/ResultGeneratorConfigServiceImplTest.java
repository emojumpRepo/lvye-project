package cn.iocoder.yudao.module.psychology.service.resultgenerator.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.ResultGenerationConfigDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.ResultGenerationConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 结果生成配置服务测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class ResultGeneratorConfigServiceImplTest {

    @Mock
    private ResultGenerationConfigMapper configMapper;

    @InjectMocks
    private ResultGeneratorConfigServiceImpl configService;

    @Test
    void testValidateConfig_ValidSingleQuestionnaireConfig() {
        ResultGenerationConfigDO config = createValidSingleQuestionnaireConfig();
        
        assertTrue(configService.validateConfig(config));
    }

    @Test
    void testValidateConfig_ValidCombinedAssessmentConfig() {
        ResultGenerationConfigDO config = createValidCombinedAssessmentConfig();
        
        assertTrue(configService.validateConfig(config));
    }

    @Test
    void testValidateConfig_MissingConfigName() {
        ResultGenerationConfigDO config = createValidSingleQuestionnaireConfig();
        config.setConfigName(null);
        
        assertFalse(configService.validateConfig(config));
    }

    @Test
    void testValidateConfig_MissingVersion() {
        ResultGenerationConfigDO config = createValidSingleQuestionnaireConfig();
        config.setVersion(null);
        
        assertFalse(configService.validateConfig(config));
    }

    @Test
    void testValidateConfig_MissingQuestionnaireIdForSingleType() {
        ResultGenerationConfigDO config = createValidSingleQuestionnaireConfig();
        config.setQuestionnaireId(null);
        
        assertFalse(configService.validateConfig(config));
    }

    @Test
    void testValidateConfig_MissingAssessmentTemplateIdForCombinedType() {
        ResultGenerationConfigDO config = createValidCombinedAssessmentConfig();
        config.setAssessmentTemplateId(null);
        
        assertFalse(configService.validateConfig(config));
    }

    @Test
    void testValidateConfig_MissingWeightConfigForCombinedType() {
        ResultGenerationConfigDO config = createValidCombinedAssessmentConfig();
        config.setWeightConfig(null);
        
        assertFalse(configService.validateConfig(config));
    }

    @Test
    void testCreateConfig_Success() {
        ResultGenerationConfigDO config = createValidSingleQuestionnaireConfig();
        
        when(configMapper.selectByConfigNameAndVersion(config.getConfigName(), config.getVersion()))
                .thenReturn(null);
        when(configMapper.insert(any(ResultGenerationConfigDO.class))).thenReturn(1);
        
        Long result = configService.createConfig(config);
        
        assertNotNull(result);
        verify(configMapper).insert(config);
        verify(configMapper).deactivateOtherVersions(config.getConfigName(), config.getVersion());
    }

    @Test
    void testActivateConfig_Success() {
        Long configId = 1L;
        ResultGenerationConfigDO config = createValidSingleQuestionnaireConfig();
        config.setId(configId);
        
        when(configMapper.selectById(configId)).thenReturn(config);
        when(configMapper.updateById(any(ResultGenerationConfigDO.class))).thenReturn(1);
        
        configService.activateConfig(configId);
        
        verify(configMapper).updateById(config);
        verify(configMapper).deactivateOtherVersions(config.getConfigName(), config.getVersion());
        assertEquals(1, config.getIsActive());
    }

    @Test
    void testDeactivateConfig_Success() {
        Long configId = 1L;
        ResultGenerationConfigDO config = createValidSingleQuestionnaireConfig();
        config.setId(configId);
        config.setIsActive(1);
        
        when(configMapper.selectById(configId)).thenReturn(config);
        when(configMapper.updateById(any(ResultGenerationConfigDO.class))).thenReturn(1);
        
        configService.deactivateConfig(configId);
        
        verify(configMapper).updateById(config);
        assertEquals(0, config.getIsActive());
    }

    private ResultGenerationConfigDO createValidSingleQuestionnaireConfig() {
        ResultGenerationConfigDO config = new ResultGenerationConfigDO();
        config.setConfigName("测试单问卷配置");
        config.setConfigType(1); // 单问卷结果
        config.setQuestionnaireId(1L);
        config.setVersion("1.0.0");
        config.setScoringAlgorithm("{\"type\":\"weighted_sum\"}");
        config.setRiskLevelRules("{\"thresholds\":[]}");
        config.setReportTemplate("{\"template\":\"default\"}");
        config.setIsActive(1);
        config.setEffectiveTime(LocalDateTime.now());
        return config;
    }

    private ResultGenerationConfigDO createValidCombinedAssessmentConfig() {
        ResultGenerationConfigDO config = new ResultGenerationConfigDO();
        config.setConfigName("测试组合测评配置");
        config.setConfigType(2); // 组合测评结果
        config.setAssessmentTemplateId(1L);
        config.setVersion("1.0.0");
        config.setScoringAlgorithm("{\"type\":\"weighted_average\"}");
        config.setRiskLevelRules("{\"algorithm\":\"max_risk\"}");
        config.setWeightConfig("{\"questionnaire_weights\":{}}");
        config.setReportTemplate("{\"template\":\"combined\"}");
        config.setIsActive(1);
        config.setEffectiveTime(LocalDateTime.now());
        return config;
    }

}