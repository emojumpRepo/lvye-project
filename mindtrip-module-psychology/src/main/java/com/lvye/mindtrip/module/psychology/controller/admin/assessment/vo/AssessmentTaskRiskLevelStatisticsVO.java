package com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 测评任务风险等级统计")
@Data
public class AssessmentTaskRiskLevelStatisticsVO {

    @Schema(description = "总风险人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RiskLevelStatisticsVO> totalList;

    @Schema(description = "年级风险人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RiskLevelGradeStatisticsVO> gradeList;


}
