package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 危机事件处理 Request VO")
@Data
public class CrisisEventProcessReqVO {

    @Schema(description = "处理方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理方式不能为空")
    private Integer processMethod;

    @Schema(description = "处理原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理原因不能为空")
    private String processReason;
}