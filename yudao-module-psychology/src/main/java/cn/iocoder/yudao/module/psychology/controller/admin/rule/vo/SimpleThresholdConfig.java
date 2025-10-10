package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 简单阈值规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "简单阈值规则配置")
public class SimpleThresholdConfig extends RuleConfigBaseVO.RuleDetailConfig {

    @Schema(description = "题目编号", example = "Q1")
    @NotBlank(message = "题目编号不能为空")
    private String question;

    @Schema(description = "比较操作符", allowableValues = {">", ">=", "<", "<=", "=", "!="})
    @NotBlank(message = "比较操作符不能为空")
    private String operator;

    @Schema(description = "阈值")
    @NotNull(message = "阈值不能为空")
    private BigDecimal threshold;

    @Override
    public String getType() {
        return "simple_threshold";
    }
}
