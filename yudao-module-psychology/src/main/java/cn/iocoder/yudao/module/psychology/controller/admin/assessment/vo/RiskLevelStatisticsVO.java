package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RiskLevelStatisticsVO {

    @Schema(description = "风险等级", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer riskLevel;

    @Schema(description = "风险等级名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String riskLevelName;

    @Schema(description = "人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer count;

}
