package cn.iocoder.yudao.module.psychology.service.rule;

import cn.iocoder.yudao.module.psychology.controller.admin.rule.vo.*;

import java.util.List;

/**
 * 规则配置服务接口
 */
public interface RuleConfigService {

    /**
     * 获取规则模板列表
     */
    List<RuleTemplateRespVO> getTemplates();

    /**
     * 保存维度规则配置
     */
    void saveDimensionRule(DimensionRuleConfigReqVO request);

    /**
     * 保存模块规则配置
     */
    void saveModuleRule(ModuleRuleConfigReqVO request);

    /**
     * 保存测评规则配置
     */
    void saveAssessmentRule(AssessmentRuleConfigReqVO request);

    /**
     * 预览规则效果
     */
    RulePreviewRespVO previewRule(RulePreviewReqVO request);

    /**
     * 校验规则配置
     */
    RuleValidationRespVO validateRule(RuleValidationReqVO request);

    /**
     * 获取维度规则配置列表
     */
    List<DimensionRuleConfigRespVO> getDimensionRules(Long dimensionId);

    /**
     * 获取模块规则配置列表
     */
    List<ModuleRuleConfigRespVO> getModuleRules(Long scenarioSlotId);

    /**
     * 获取测评规则配置列表
     */
    List<AssessmentRuleConfigRespVO> getAssessmentRules(Long scenarioId);

    /**
     * 删除维度规则
     */
    void deleteDimensionRule(Long id);

    /**
     * 删除模块规则
     */
    void deleteModuleRule(Long id);

    /**
     * 删除测评规则
     */
    void deleteAssessmentRule(Long id);
}
