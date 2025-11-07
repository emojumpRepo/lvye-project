package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 危机事件更新描述 Request VO")
@Data
public class CrisisEventUpdateDescriptionReqVO {

    @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "事件ID不能为空")
    private Long id;

    @Schema(description = "事件描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "事件描述不能为空")
    private String description;
}
