package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 问卷精简信息 Response VO")
@Data
public class QuestionnaireSimpleRespVO {

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "问卷标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "心理健康评估问卷")
    private String title;

    @Schema(description = "问卷描述", example = "这是一份用于评估学生心理健康状况的专业问卷")
    private String description;

    @Schema(description = "问卷类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer questionnaireType;

    @Schema(description = "目标对象", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer targetAudience;

    @Schema(description = "测评维度标签列表", example = "[\"情绪状态\", \"压力水平\"]")
    private List<String> assessmentDimensionLabels;

    @Schema(description = "题目数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
    private Integer questionCount;

    @Schema(description = "预计用时（分钟）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Integer estimatedDuration;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

}