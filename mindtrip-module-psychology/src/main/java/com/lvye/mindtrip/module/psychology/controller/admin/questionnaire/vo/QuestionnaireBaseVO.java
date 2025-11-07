package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 问卷基础 VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class QuestionnaireBaseVO {

    @Schema(description = "问卷标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "心理健康评估问卷")
    @NotBlank(message = "问卷标题不能为空")
    private String title;

    @Schema(description = "问卷描述", example = "用于评估学生心理健康状况的专业问卷")
    private String description;

    @Schema(description = "问卷类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "问卷类型不能为空")
    private Integer questionnaireType;

    @Schema(description = "目标对象", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标对象不能为空")
    private Integer targetAudience;

    @Schema(description = "测评维度（多选，逗号分隔）", example = "emotional_state,stress_level")
    private String assessmentDimension;

    @Schema(description = "外部系统问卷ID", example = "EXT_001")
    private String externalId;

    @Schema(description = "外部问卷链接", example = "https://survey.example.com/123")
    private String externalLink;

    @Schema(description = "问卷编码", example = "SURVEY_001")
    private String surveyCode;

    @Schema(description = "题目数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
    @NotNull(message = "题目数量不能为空")
    private Integer questionCount;

    @Schema(description = "预计用时（分钟）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    @NotNull(message = "预计用时不能为空")
    private Integer estimatedDuration;

    @Schema(description = "问卷内容（JSON格式）")
    private String content;

    @Schema(description = "评分规则配置（JSON格式）")
    private String scoringRules;

    @Schema(description = "结果报告模板（JSON格式）")
    private String resultTemplate;

    @Schema(description = "是否支持独立使用", example = "1")
    private Integer supportIndependentUse;

}