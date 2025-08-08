package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 测评任务统计 Response VO")
@Data
public class AssessmentTaskStatisticsRespVO {

    @Schema(description = "总参与人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long totalParticipants;

    @Schema(description = "已完成人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Long completedParticipants;

    @Schema(description = "进行中人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Long inProgressParticipants;

    @Schema(description = "未开始人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long notStartedParticipants;

    @Schema(description = "完成率", requiredMode = Schema.RequiredMode.REQUIRED, example = "80.0")
    private Double completionRate;

}