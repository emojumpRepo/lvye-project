package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 复杂逻辑规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "复杂逻辑规则配置")
public class ComplexLogicConfig extends RuleConfigBaseVO.RuleDetailConfig {

    @Schema(description = "逻辑组合类型", allowableValues = {"AND", "OR"})
    private String logic = "AND";

    @Schema(description = "子条件列表")
    @NotEmpty(message = "子条件不能为空")
    @Valid
    private List<LogicCondition> conditions;

    @Override
    public String getType() {
        return "complex_logic";
    }

    /**
     * 逻辑条件（支持嵌套）
     */
    @Data
    @Schema(description = "逻辑条件")
    public static class LogicCondition {
        @Schema(description = "条件类型", allowableValues = {"simple", "sum", "option", "nested"})
        private String type;

        @Schema(description = "简单条件配置")
        @Valid
        private SimpleCondition simple;

        @Schema(description = "求和条件配置")
        @Valid
        private SumCondition sum;

        @Schema(description = "选项条件配置")
        @Valid
        private OptionCondition option;

        @Schema(description = "嵌套条件配置")
        @Valid
        private NestedCondition nested;
    }

    /**
     * 简单条件
     */
    @Data
    @Schema(description = "简单条件")
    public static class SimpleCondition {
        @Schema(description = "题目编号")
        private String question;

        @Schema(description = "操作符")
        private String operator;

        @Schema(description = "比较值")
        private Object value;
    }

    /**
     * 求和条件
     */
    @Data
    @Schema(description = "求和条件")
    public static class SumCondition {
        @Schema(description = "题目列表")
        private List<String> questions;

        @Schema(description = "操作符")
        private String operator;

        @Schema(description = "阈值")
        private Object threshold;

        @Schema(description = "类别名称（可选）")
        private String category;
    }

    /**
     * 选项条件
     */
    @Data
    @Schema(description = "选项条件")
    public static class OptionCondition {
        @Schema(description = "题目编号")
        private String question;

        @Schema(description = "操作符")
        private String operator;

        @Schema(description = "选项值")
        private Object optionValue;
    }

    /**
     * 嵌套条件
     */
    @Data
    @Schema(description = "嵌套条件")
    public static class NestedCondition {
        @Schema(description = "嵌套逻辑")
        private String logic;

        @Schema(description = "嵌套子条件")
        private List<LogicCondition> subConditions;
    }
}
