package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 模块联动规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模块联动规则配置")
public class ModuleInterlockConfig extends RuleConfigBaseVO.RuleDetailConfig {

    @Schema(description = "模块分组配置")
    @NotNull(message = "模块分组配置不能为空")
    @Valid
    private ModuleGrouping grouping;

    @Schema(description = "联动规则列表")
    @NotEmpty(message = "联动规则不能为空")
    @Valid
    private List<InterlockRule> rules;

    @Schema(description = "兜底规则")
    @Valid
    private FallbackRule fallback;

    @Override
    public String getType() {
        return "module_interlock";
    }

    /**
     * 模块分组配置
     */
    @Data
    @Schema(description = "模块分组配置")
    public static class ModuleGrouping {
        @Schema(description = "自身模块（极端行为等）")
        @Valid
        private ModuleGroup self;

        @Schema(description = "其他模块分组")
        @Valid
        private List<ModuleGroup> others;
    }

    /**
     * 模块分组
     */
    @Data
    @Schema(description = "模块分组")
    public static class ModuleGroup {
        @Schema(description = "分组名称")
        private String name;

        @Schema(description = "维度ID列表")
        private List<Long> dimensionIds;

        @Schema(description = "维度编码列表")
        private List<String> dimensionCodes;
    }

    /**
     * 联动规则
     */
    @Data
    @Schema(description = "联动规则")
    public static class InterlockRule {
        @Schema(description = "规则名称")
        private String name;

        @Schema(description = "条件配置")
        @Valid
        private InterlockCondition condition;

        @Schema(description = "输出结果")
        @Valid
        private InterlockResult result;
    }

    /**
     * 联动条件
     */
    @Data
    @Schema(description = "联动条件")
    public static class InterlockCondition {
        @Schema(description = "自身模块等级条件", allowableValues = {"NONE", "MILD", "MODERATE", "SEVERE"})
        private String selfLevel;

        @Schema(description = "其他模块重度数量条件")
        private IntegerCondition severeCount;

        @Schema(description = "其他模块中度数量条件")
        private IntegerCondition moderateCount;

        @Schema(description = "其他模块轻度数量条件")
        private IntegerCondition mildCount;

        @Schema(description = "条件组合逻辑", allowableValues = {"AND", "OR"})
        private String logic = "AND";
    }

    /**
     * 整数条件
     */
    @Data
    @Schema(description = "整数条件")
    public static class IntegerCondition {
        @Schema(description = "操作符", allowableValues = {"=", "!=", ">", ">=", "<", "<="})
        private String operator;

        @Schema(description = "值")
        private Integer value;
    }

    /**
     * 联动结果
     */
    @Data
    @Schema(description = "联动结果")
    public static class InterlockResult {
        @Schema(description = "结果等级")
        private String level;

        @Schema(description = "结果消息")
        private String message;

        @Schema(description = "是否停止后续规则")
        private Boolean stopOnMatch = true;
    }

    /**
     * 兜底规则
     */
    @Data
    @Schema(description = "兜底规则")
    public static class FallbackRule {
        @Schema(description = "默认等级")
        private String level;

        @Schema(description = "默认消息")
        private String message;
    }
}
