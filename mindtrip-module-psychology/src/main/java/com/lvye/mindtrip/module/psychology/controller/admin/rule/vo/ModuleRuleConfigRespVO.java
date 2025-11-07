package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模块规则配置响应VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模块规则配置响应")
public class ModuleRuleConfigRespVO extends RuleConfigBaseVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "测评场景插槽ID")
    private Long scenarioSlotId;

    @Schema(description = "插槽名称")
    private String slotName;

    @Schema(description = "模块类型")
    private String moduleType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "状态", allowableValues = {"0: 禁用", "1: 启用"})
    private Integer status;
}
