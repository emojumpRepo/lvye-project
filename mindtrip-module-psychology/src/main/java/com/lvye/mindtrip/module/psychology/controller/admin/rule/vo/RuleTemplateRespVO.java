package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 规则模板响应VO
 */
@Data
@Schema(description = "规则模板响应")
public class RuleTemplateRespVO {

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "适用场景", allowableValues = {"DIMENSION", "MODULE", "ASSESSMENT"})
    private String applicableScope;

    @Schema(description = "模板配置")
    private Object templateConfig;

    @Schema(description = "配置字段定义")
    private List<ConfigField> configFields;

    @Schema(description = "示例配置")
    private Object exampleConfig;

    /**
     * 配置字段定义
     */
    @Data
    @Schema(description = "配置字段定义")
    public static class ConfigField {
        @Schema(description = "字段名")
        private String fieldName;

        @Schema(description = "字段标签")
        private String fieldLabel;

        @Schema(description = "字段类型", allowableValues = {"text", "number", "select", "multiSelect", "switch"})
        private String fieldType;

        @Schema(description = "是否必填")
        private Boolean required;

        @Schema(description = "默认值")
        private Object defaultValue;

        @Schema(description = "选项列表（适用于select类型）")
        private List<FieldOption> options;

        @Schema(description = "验证规则")
        private Object validation;
    }

    /**
     * 字段选项
     */
    @Data
    @Schema(description = "字段选项")
    public static class FieldOption {
        @Schema(description = "选项值")
        private Object value;

        @Schema(description = "选项标签")
        private String label;

        @Schema(description = "选项描述")
        private String description;
    }
}
