package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 咨询预约取消 Request VO")
@Data
public class ConsultationAppointmentCancelReqVO {

    @Schema(description = "取消原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "取消原因不能为空")
    private String reason;

    @Schema(description = "自定义原因")
    private String customReason;
}