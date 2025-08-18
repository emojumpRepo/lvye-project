package cn.iocoder.yudao.module.psychology.service.resultgenerator.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.ResultGenerationConfigDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.ResultGenerationConfigMapper;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.service.resultgenerator.ResultGeneratorConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 结果生成配置服务实现
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class ResultGeneratorConfigServiceImpl implements ResultGeneratorConfigService {

    @Resource
    private ResultGenerationConfigMapper resultGenerationConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConfig(ResultGenerationConfigDO configDO) {
        // 验证配置有效性
        if (!validateConfig(configDO)) {
            throw exception(ErrorCodeConstants.RESULT_GENERATION_CONFIG_INVALID);
        }

        // 检查配置名称和版本是否已存在
        ResultGenerationConfigDO existingConfig = resultGenerationConfigMapper.selectByConfigNameAndVersion(
                configDO.getConfigName(), configDO.getVersion());
        if (existingConfig != null) {
            throw exception(ErrorCodeConstants.RESULT_GENERATION_CONFIG_INVALID, "配置名称和版本已存在");
        }

        // 设置默认值
        if (configDO.getEffectiveTime() == null) {
            configDO.setEffectiveTime(LocalDateTime.now());
        }
        if (configDO.getIsActive() == null) {
            configDO.setIsActive(1);
        }

        // 插入配置
        resultGenerationConfigMapper.insert(configDO);

        // 如果是激活状态，停用同名配置的其他版本
        if (configDO.getIsActive() == 1) {
            resultGenerationConfigMapper.deactivateOtherVersions(configDO.getConfigName(), configDO.getVersion());
        }

        log.info("创建结果生成配置成功，配置名称: {}, 版本: {}",
                configDO.getConfigName(), configDO.getVersion());
        return configDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "resultGeneratorConfig", allEntries = true)
    public void updateConfig(ResultGenerationConfigDO configDO) {
        // 验证配置存在
        ResultGenerationConfigDO existingConfig = validateConfigExists(configDO.getId());
        
        // 验证配置有效性
        if (!validateConfig(configDO)) {
            throw exception(ErrorCodeConstants.RESULT_GENERATION_CONFIG_INVALID);
        }

        // 更新配置
        resultGenerationConfigMapper.updateById(configDO);

        // 如果激活状态发生变化，处理其他版本的状态
        if (configDO.getIsActive() != null && configDO.getIsActive() == 1) {
            resultGenerationConfigMapper.deactivateOtherVersions(existingConfig.getConfigName(), existingConfig.getVersion());
        }

        log.info("更新结果生成配置成功，配置ID: {}", configDO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "resultGeneratorConfig", allEntries = true)
    public void deleteConfig(Long id) {
        // 验证配置存在
        validateConfigExists(id);
        
        // 删除配置
        resultGenerationConfigMapper.deleteById(id);
        
        log.info("删除结果生成配置成功，配置ID: {}", id);
    }

    @Override
    public ResultGenerationConfigDO getConfig(Long id) {
        return resultGenerationConfigMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "resultGeneratorConfig", key = "'questionnaire:' + #questionnaireId")
    public ResultGenerationConfigDO getActiveConfigByQuestionnaireId(Long questionnaireId) {
        return resultGenerationConfigMapper.selectActiveByQuestionnaireId(questionnaireId);
    }

    @Override
    @Cacheable(value = "resultGeneratorConfig", key = "'assessment:' + #assessmentTemplateId")
    public ResultGenerationConfigDO getActiveConfigByAssessmentTemplateId(Long assessmentTemplateId) {
        return resultGenerationConfigMapper.selectActiveByAssessmentTemplateId(assessmentTemplateId);
    }

    @Override
    public List<ResultGenerationConfigDO> getActiveConfigs(Integer configType) {
        return resultGenerationConfigMapper.selectActiveConfigs(configType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "resultGeneratorConfig", allEntries = true)
    public void activateConfig(Long id) {
        // 验证配置存在
        ResultGenerationConfigDO config = validateConfigExists(id);
        
        // 激活当前配置
        config.setIsActive(1);
        resultGenerationConfigMapper.updateById(config);

        // 停用同名配置的其他版本
        resultGenerationConfigMapper.deactivateOtherVersions(config.getConfigName(), config.getVersion());
        
        log.info("激活结果生成配置成功，配置ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "resultGeneratorConfig", allEntries = true)
    public void deactivateConfig(Long id) {
        // 验证配置存在
        ResultGenerationConfigDO config = validateConfigExists(id);
        
        // 停用配置
        config.setIsActive(0);
        resultGenerationConfigMapper.updateById(config);
        
        log.info("停用结果生成配置成功，配置ID: {}", id);
    }

    @Override
    public boolean validateConfig(ResultGenerationConfigDO configDO) {
        if (configDO == null) {
            return false;
        }
        
        // 验证必填字段
        if (configDO.getConfigName() == null || configDO.getConfigName().trim().isEmpty()) {
            return false;
        }
        if (configDO.getVersion() == null || configDO.getVersion().trim().isEmpty()) {
            return false;
        }
        if (configDO.getConfigType() == null) {
            return false;
        }
        if (configDO.getScoringAlgorithm() == null || configDO.getScoringAlgorithm().trim().isEmpty()) {
            return false;
        }
        if (configDO.getRiskLevelRules() == null || configDO.getRiskLevelRules().trim().isEmpty()) {
            return false;
        }
        if (configDO.getReportTemplate() == null || configDO.getReportTemplate().trim().isEmpty()) {
            return false;
        }
        
        // 验证配置类型相关字段
        if (configDO.getConfigType() == 1) { // 单问卷结果
            if (configDO.getQuestionnaireId() == null) {
                return false;
            }
        } else if (configDO.getConfigType() == 2) { // 组合测评结果
            if (configDO.getAssessmentTemplateId() == null) {
                return false;
            }
            if (configDO.getWeightConfig() == null || configDO.getWeightConfig().trim().isEmpty()) {
                return false;
            }
        }
        
        // TODO: 验证JSON格式的配置字段
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "resultGeneratorConfig", allEntries = true)
    public void rollbackToVersion(String configName, String version) {
        // 查找指定版本的配置
        ResultGenerationConfigDO targetConfig = resultGenerationConfigMapper.selectByConfigNameAndVersion(configName, version);
        if (targetConfig == null) {
            throw exception(ErrorCodeConstants.RESULT_GENERATION_CONFIG_NOT_FOUND,
                    "指定版本的配置不存在: " + configName + ":" + version);
        }

        // 停用同名配置的所有版本
        resultGenerationConfigMapper.deactivateOtherVersions(configName, "");

        // 激活目标版本
        targetConfig.setIsActive(1);
        resultGenerationConfigMapper.updateById(targetConfig);

        log.info("回滚结果生成配置成功，配置名称: {}, 版本: {}", configName, version);
    }

    @Override
    public List<ResultGenerationConfigDO> getConfigVersions(String configName) {
        return resultGenerationConfigMapper.selectList("config_name", configName);
    }

    /**
     * 验证配置是否存在
     */
    private ResultGenerationConfigDO validateConfigExists(Long id) {
        ResultGenerationConfigDO config = resultGenerationConfigMapper.selectById(id);
        if (config == null) {
            throw exception(ErrorCodeConstants.RESULT_GENERATION_CONFIG_NOT_FOUND);
        }
        return config;
    }

}