package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RiskLevelDeptStatisticsVO {

    @Schema(description = "班级ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classDeptId;

    @Schema(description = "年级ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long gradeDeptId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "学生档案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentProfileId;

    @Schema(description = "风险等级", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer riskLevel;

}
