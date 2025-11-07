package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 维度规则配置响应VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "维度规则配置响应")
public class DimensionRuleConfigRespVO extends RuleConfigBaseVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "维度ID")
    private Long dimensionId;

    @Schema(description = "维度名称")
    private String dimensionName;

    @Schema(description = "问卷ID")
    private Long questionnaireId;

    @Schema(description = "问卷名称")
    private String questionnaireName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "状态", allowableValues = {"0: 禁用", "1: 启用"})
    private Integer status;
}
