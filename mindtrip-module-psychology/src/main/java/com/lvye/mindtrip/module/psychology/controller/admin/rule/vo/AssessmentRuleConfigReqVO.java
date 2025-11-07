package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 测评规则配置请求VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "测评规则配置请求")
public class AssessmentRuleConfigReqVO extends RuleConfigBaseVO {

    @Schema(description = "测评场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "测评场景ID不能为空")
    private Long scenarioId;

    @Schema(description = "测评类型", example = "COMPREHENSIVE")
    private String assessmentType;
}
