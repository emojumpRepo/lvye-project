package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 学生风险等级调整 Request VO")
@Data
public class StudentLevelAdjustReqVO {

    @Schema(description = "目标等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标等级不能为空")
    private Integer targetLevel;

    @Schema(description = "调整原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "调整原因不能为空")
    private String reason;
}