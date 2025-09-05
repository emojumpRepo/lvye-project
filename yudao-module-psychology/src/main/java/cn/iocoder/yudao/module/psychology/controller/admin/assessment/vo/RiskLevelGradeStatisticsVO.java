package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RiskLevelGradeStatisticsVO {

    @Schema(description = "年级ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long gradeDeptId;

    @Schema(description = "年级名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String gradeName;

    @Schema(description = "年级风险分布", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RiskLevelStatisticsVO> riskLevelList;

}
