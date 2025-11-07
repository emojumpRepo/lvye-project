package com.lvye.mindtrip.module.psychology.controller.admin.rule;

import com.lvye.mindtrip.framework.common.pojo.CommonResult;
import com.lvye.mindtrip.module.psychology.controller.admin.rule.vo.*;
import com.lvye.mindtrip.module.psychology.service.rule.RuleConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 规则配置管理控制器
 */
@Tag(name = "管理后台 - 心理测评规则配置")
@RestController
@RequestMapping("/admin/psychology/rule-config")
@Validated
@Slf4j
public class RuleConfigController {

    @Resource
    private RuleConfigService ruleConfigService;

    @GetMapping("/templates")
    @Operation(summary = "获取规则类型模板")
    public CommonResult<List<RuleTemplateRespVO>> getTemplates() {
        List<RuleTemplateRespVO> templates = ruleConfigService.getTemplates();
        return CommonResult.success(templates);
    }

    @PostMapping("/dimension/save")
    @Operation(summary = "保存维度规则配置")
    public CommonResult<Void> saveDimensionRule(@Valid @RequestBody DimensionRuleConfigReqVO request) {
        ruleConfigService.saveDimensionRule(request);
        return CommonResult.success(null);
    }

    @PostMapping("/module/save")
    @Operation(summary = "保存模块规则配置")
    public CommonResult<Void> saveModuleRule(@Valid @RequestBody ModuleRuleConfigReqVO request) {
        ruleConfigService.saveModuleRule(request);
        return CommonResult.success(null);
    }

    @PostMapping("/assessment/save")
    @Operation(summary = "保存测评规则配置")
    public CommonResult<Void> saveAssessmentRule(@Valid @RequestBody AssessmentRuleConfigReqVO request) {
        ruleConfigService.saveAssessmentRule(request);
        return CommonResult.success(null);
    }

    @PostMapping("/preview")
    @Operation(summary = "预览规则效果")
    public CommonResult<RulePreviewRespVO> previewRule(@Valid @RequestBody RulePreviewReqVO request) {
        RulePreviewRespVO result = ruleConfigService.previewRule(request);
        return CommonResult.success(result);
    }

    @PostMapping("/validate")
    @Operation(summary = "校验规则配置")
    public CommonResult<RuleValidationRespVO> validateRule(@Valid @RequestBody RuleValidationReqVO request) {
        RuleValidationRespVO result = ruleConfigService.validateRule(request);
        return CommonResult.success(result);
    }

    @GetMapping("/dimension/{dimensionId}")
    @Operation(summary = "获取维度规则配置")
    public CommonResult<List<DimensionRuleConfigRespVO>> getDimensionRules(@PathVariable Long dimensionId) {
        List<DimensionRuleConfigRespVO> rules = ruleConfigService.getDimensionRules(dimensionId);
        return CommonResult.success(rules);
    }

    @GetMapping("/module/{scenarioSlotId}")
    @Operation(summary = "获取模块规则配置")
    public CommonResult<List<ModuleRuleConfigRespVO>> getModuleRules(@PathVariable Long scenarioSlotId) {
        List<ModuleRuleConfigRespVO> rules = ruleConfigService.getModuleRules(scenarioSlotId);
        return CommonResult.success(rules);
    }

    @GetMapping("/assessment/{scenarioId}")
    @Operation(summary = "获取测评规则配置")
    public CommonResult<List<AssessmentRuleConfigRespVO>> getAssessmentRules(@PathVariable Long scenarioId) {
        List<AssessmentRuleConfigRespVO> rules = ruleConfigService.getAssessmentRules(scenarioId);
        return CommonResult.success(rules);
    }

    @DeleteMapping("/dimension/{id}")
    @Operation(summary = "删除维度规则")
    public CommonResult<Void> deleteDimensionRule(@PathVariable Long id) {
        ruleConfigService.deleteDimensionRule(id);
        return CommonResult.success(null);
    }

    @DeleteMapping("/module/{id}")
    @Operation(summary = "删除模块规则")
    public CommonResult<Void> deleteModuleRule(@PathVariable Long id) {
        ruleConfigService.deleteModuleRule(id);
        return CommonResult.success(null);
    }

    @DeleteMapping("/assessment/{id}")
    @Operation(summary = "删除测评规则")
    public CommonResult<Void> deleteAssessmentRule(@PathVariable Long id) {
        ruleConfigService.deleteAssessmentRule(id);
        return CommonResult.success(null);
    }
}
