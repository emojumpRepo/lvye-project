package com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


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
    private BigDecimal completionRate;

    @Schema(description = "风险等级统计")
    private List<RiskLevelStatisticsVO> riskLevelStatistics;

    @Schema(description = "部门统计树，includeDeptTree=1 时返回")
    private List<AssessmentDeptNodeVO> deptTree;

}