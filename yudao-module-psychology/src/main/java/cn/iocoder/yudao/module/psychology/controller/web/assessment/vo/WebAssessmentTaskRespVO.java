package cn.iocoder.yudao.module.psychology.controller.web.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "学生家长端 - 测评任务 Response VO")
@Data
public class WebAssessmentTaskRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TASK2024001")
    private String taskNo;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024年春季心理测评")
    private String name;

    @Schema(description = "量表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "A")
    private String scaleCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}