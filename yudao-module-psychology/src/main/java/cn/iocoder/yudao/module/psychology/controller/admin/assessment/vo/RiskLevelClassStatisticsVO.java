package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RiskLevelClassStatisticsVO {

    @Schema(description = "班级ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classDeptId;

    @Schema(description = "班级名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String className;

    @Schema(description = "参与总人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer total;

    @Schema(description = "班级风险分布", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RiskLevelStatisticsVO> riskLevelList;
}

