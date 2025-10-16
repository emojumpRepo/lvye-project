package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 选项匹配规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "选项匹配规则配置")
public class OptionMatchConfig extends RuleConfigBaseVO.RuleDetailConfig {

    @Schema(description = "条件组合逻辑", allowableValues = {"AND", "OR"})
    @NotBlank(message = "逻辑组合不能为空")
    private String logic;

    @Schema(description = "匹配条件列表")
    @NotEmpty(message = "匹配条件不能为空")
    @Valid
    private List<OptionCondition> conditions;

    @Override
    public String getType() {
        return "option_match";
    }

    /**
     * 选项条件
     */
    @Data
    @Schema(description = "选项匹配条件")
    public static class OptionCondition {
        @Schema(description = "题目编号", example = "Q51")
        @NotBlank(message = "题目编号不能为空")
        private String question;

        @Schema(description = "比较操作符", allowableValues = {"=", "!=", "contains"})
        @NotBlank(message = "比较操作符不能为空")
        private String operator;

        @Schema(description = "匹配值")
        private Object value;
    }
}
