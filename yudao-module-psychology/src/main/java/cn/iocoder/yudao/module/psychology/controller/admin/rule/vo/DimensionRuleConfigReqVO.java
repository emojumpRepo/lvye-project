package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 维度规则配置请求VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "维度规则配置请求")
public class DimensionRuleConfigReqVO extends RuleConfigBaseVO {

    @Schema(description = "维度ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "维度ID不能为空")
    private Long dimensionId;

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;
}
