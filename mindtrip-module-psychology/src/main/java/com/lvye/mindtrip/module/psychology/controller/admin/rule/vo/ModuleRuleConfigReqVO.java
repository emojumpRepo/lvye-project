package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 模块规则配置请求VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模块规则配置请求")
public class ModuleRuleConfigReqVO extends RuleConfigBaseVO {

    @Schema(description = "测评场景插槽ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "测评场景插槽ID不能为空")
    private Long scenarioSlotId;

    @Schema(description = "模块类型", example = "EXTREME_BEHAVIOR")
    private String moduleType;
}
