package cn.iocoder.yudao.module.psychology.service.resultgenerator;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.ResultGenerationConfigDO;
import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;

import java.util.List;

/**
 * 结果生成配置服务接口
 *
 * @author 芋道源码
 */
public interface ResultGeneratorConfigService {

    /**
     * 创建配置
     *
     * @param configDO 配置信息
     * @return 配置ID
     */
    Long createConfig(ResultGenerationConfigDO configDO);

    /**
     * 更新配置
     *
     * @param configDO 配置信息
     */
    void updateConfig(ResultGenerationConfigDO configDO);

    /**
     * 删除配置
     *
     * @param id 配置ID
     */
    void deleteConfig(Long id);

    /**
     * 获取配置详情
     *
     * @param id 配置ID
     * @return 配置信息
     */
    ResultGenerationConfigDO getConfig(Long id);

    /**
     * 根据问卷ID获取激活的配置
     *
     * @param questionnaireId 问卷ID
     * @return 配置信息
     */
    ResultGenerationConfigDO getActiveConfigByQuestionnaireId(Long questionnaireId);

    /**
     * 根据测评模板ID获取激活的配置
     *
     * @param assessmentTemplateId 测评模板ID
     * @return 配置信息
     */
    ResultGenerationConfigDO getActiveConfigByAssessmentTemplateId(Long assessmentTemplateId);

    /**
     * 获取激活的配置列表
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    List<ResultGenerationConfigDO> getActiveConfigs(Integer configType);

    /**
     * 激活配置
     *
     * @param id 配置ID
     */
    void activateConfig(Long id);

    /**
     * 停用配置
     *
     * @param id 配置ID
     */
    void deactivateConfig(Long id);

    /**
     * 验证配置有效性
     *
     * @param configDO 配置信息
     * @return 验证结果
     */
    boolean validateConfig(ResultGenerationConfigDO configDO);

    /**
     * 回滚到指定版本
     *
     * @param configName 配置名称
     * @param version 版本号
     */
    void rollbackToVersion(String configName, String version);

    /**
     * 获取配置历史版本
     *
     * @param configName 配置名称
     * @return 历史版本列表
     */
    List<ResultGenerationConfigDO> getConfigVersions(String configName);

}