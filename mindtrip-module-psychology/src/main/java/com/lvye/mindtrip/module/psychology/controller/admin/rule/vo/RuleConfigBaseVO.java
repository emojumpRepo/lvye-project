package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 规则配置基础VO
 */
@Data
@Schema(description = "规则配置基础信息")
public class RuleConfigBaseVO {

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "规则名称不能为空")
    private String name;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "优先级(数字越小优先级越高)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级不能为空")
    @Min(value = 1, message = "优先级最小值为1")
    @Max(value = 999, message = "优先级最大值为999")
    private Integer priority;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "规则类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    @Schema(description = "规则配置详情")
    @NotNull(message = "规则配置不能为空")
    private RuleDetailConfig config;

    @Schema(description = "输出配置")
    @NotNull(message = "输出配置不能为空")
    private RuleOutputConfig output;

    /**
     * 规则详细配置基类
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleThresholdConfig.class, name = "simple_threshold"),
        @JsonSubTypes.Type(value = SumThresholdConfig.class, name = "sum_threshold"),
        @JsonSubTypes.Type(value = OptionMatchConfig.class, name = "option_match"),
        @JsonSubTypes.Type(value = ComplexLogicConfig.class, name = "complex_logic"),
        @JsonSubTypes.Type(value = CollectionRuleConfig.class, name = "collection_rule"),
        @JsonSubTypes.Type(value = AbuseCategoryConfig.class, name = "abuse_category"),
        @JsonSubTypes.Type(value = ModuleInterlockConfig.class, name = "module_interlock"),
        @JsonSubTypes.Type(value = AbnormalAggregationConfig.class, name = "abnormal_aggregation")
    })
    @Data
    public static abstract class RuleDetailConfig {
        @Schema(description = "配置类型")
        public abstract String getType();
    }

    /**
     * 输出配置
     */
    @Data
    @Schema(description = "规则输出配置")
    public static class RuleOutputConfig {
        @Schema(description = "等级")
        private String level;

        @Schema(description = "是否异常")
        private Boolean isAbnormal;

        @Schema(description = "提示信息")
        private String message;

        @Schema(description = "描述")
        private String description;

        @Schema(description = "建议")
        private String suggestions;

        @Schema(description = "教师评语")
        private String teacherComment;

        @Schema(description = "学生评语数组")
        private List<String> studentComments;

        @Schema(description = "收集配置")
        private List<CollectionConfig> collections;
    }

    /**
     * 收集配置
     */
    @Data
    @Schema(description = "数据收集配置")
    public static class CollectionConfig {
        @Schema(description = "收集类型", allowableValues = {"reasons", "categories", "options", "custom"})
        @NotBlank(message = "收集类型不能为空")
        private String type;

        @Schema(description = "字段名称")
        @NotBlank(message = "字段名称不能为空")
        private String name;

        @Schema(description = "说明")
        private String description;
    }
}
