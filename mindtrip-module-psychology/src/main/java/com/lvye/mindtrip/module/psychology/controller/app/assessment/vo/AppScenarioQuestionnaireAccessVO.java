package com.lvye.mindtrip.module.psychology.controller.app.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "App - 场景问卷访问 VO（含完成/访问状态）")
public class AppScenarioQuestionnaireAccessVO {

    @Schema(description = "问卷ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "类型")
    private Integer questionnaireType;

    @Schema(description = "目标人群")
    private Integer targetAudience;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "外部链接")
    private String externalLink;

    @Schema(description = "预计耗时(分钟)")
    private Integer estimatedDuration;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否已完成")
    private Boolean completed;

    @Schema(description = "是否可访问")
    private Boolean accessible;

    @Schema(description = "生成状态：0-待生成，1-生成中，2-已生成，3-生成失败")
    private Integer generationStatus;
}
