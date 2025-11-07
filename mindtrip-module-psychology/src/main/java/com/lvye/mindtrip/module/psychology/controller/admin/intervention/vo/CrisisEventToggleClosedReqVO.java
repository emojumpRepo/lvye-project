package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 切换危机事件关闭状态 Request VO")
@Data
public class CrisisEventToggleClosedReqVO {

    @Schema(description = "是否关闭", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "关闭状态不能为空")
    private Boolean closed;
}
