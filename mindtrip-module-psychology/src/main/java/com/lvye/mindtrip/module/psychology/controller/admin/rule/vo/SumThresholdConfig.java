package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 求和阈值规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "求和阈值规则配置")
public class SumThresholdConfig extends RuleConfigBaseVO.RuleDetailConfig {

    @Schema(description = "题目列表", example = "[\"Q3\", \"Q8\", \"Q14\"]")
    @NotEmpty(message = "题目列表不能为空")
    private List<String> questions;

    @Schema(description = "比较操作符", allowableValues = {">", ">=", "<", "<=", "="})
    @NotBlank(message = "比较操作符不能为空")
    private String operator;

    @Schema(description = "阈值")
    @NotNull(message = "阈值不能为空")
    private BigDecimal threshold;

    @Schema(description = "类别名称（可选）", example = "情感虐待")
    private String category;

    @Override
    public String getType() {
        return "sum_threshold";
    }
}
