package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 规则校验请求VO
 */
@Data
@Schema(description = "规则校验请求")
public class RuleValidationReqVO {

    @Schema(description = "规则配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "规则配置不能为空")
    @Valid
    private RuleConfigBaseVO ruleConfig;

    @Schema(description = "校验类型", allowableValues = {"DIMENSION", "MODULE", "ASSESSMENT"})
    private String validationType;

    @Schema(description = "是否检查冲突")
    private Boolean checkConflicts = true;

    @Schema(description = "关联的维度ID（用于冲突检测）")
    private Long dimensionId;

    @Schema(description = "关联的场景插槽ID（用于冲突检测）")
    private Long scenarioSlotId;

    @Schema(description = "关联的场景ID（用于冲突检测）")
    private Long scenarioId;
}
