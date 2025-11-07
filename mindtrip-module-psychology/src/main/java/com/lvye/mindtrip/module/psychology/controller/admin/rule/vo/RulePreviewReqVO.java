package com.lvye.mindtrip.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 规则预览请求VO
 */
@Data
@Schema(description = "规则预览请求")
public class RulePreviewReqVO {

    @Schema(description = "规则配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "规则配置不能为空")
    @Valid
    private RuleConfigBaseVO ruleConfig;

    @Schema(description = "测试数据")
    private PreviewTestData testData;

    @Schema(description = "预览类型", allowableValues = {"DIMENSION", "MODULE", "ASSESSMENT"})
    private String previewType;

    /**
     * 预览测试数据
     */
    @Data
    @Schema(description = "预览测试数据")
    public static class PreviewTestData {
        @Schema(description = "题目分数映射 (题目编号 -> 分数)")
        private Map<String, Object> questionScores;

        @Schema(description = "题目选项映射 (题目编号 -> 选项文本)")
        private Map<String, String> questionOptions;

        @Schema(description = "用户变量 (如年龄、性别等)")
        private Map<String, Object> userVariables;

        @Schema(description = "维度结果数据（用于模块/测评预览）")
        private Map<String, Object> dimensionResults;

        @Schema(description = "模块结果数据（用于测评预览）")
        private Map<String, Object> moduleResults;
    }
}
