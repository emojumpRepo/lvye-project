package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "场景-问卷访问信息 VO（含完成/访问状态）")
public class ScenarioQuestionnaireAccessVO {

    @Schema(description = "问卷ID", example = "1001")
    private Long id;

    @Schema(description = "标题", example = "心理健康量表")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "问卷类型", example = "ANXIETY")
    private Integer questionnaireType;

    @Schema(description = "目标人群", example = "STUDENT")
    private Integer targetAudience;

    @Schema(description = "题目数量", example = "20")
    private Integer questionCount;

    @Schema(description = "预计耗时(分钟)", example = "10")
    private Integer estimatedDuration;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "是否已完成")
    private Boolean completed;

    @Schema(description = "是否可访问")
    private Boolean accessible;
}
