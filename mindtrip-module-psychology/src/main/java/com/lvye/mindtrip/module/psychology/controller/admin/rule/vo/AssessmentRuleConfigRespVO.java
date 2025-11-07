package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 测评规则配置响应VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "测评规则配置响应")
public class AssessmentRuleConfigRespVO extends RuleConfigBaseVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "测评场景ID")
    private Long scenarioId;

    @Schema(description = "场景名称")
    private String scenarioName;

    @Schema(description = "测评类型")
    private String assessmentType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "状态", allowableValues = {"0: 禁用", "1: 启用"})
    private Integer status;
}
