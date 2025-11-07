package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 咨询预约补录 Request VO")
@Data
public class ConsultationAppointmentSupplementReqVO {

    @Schema(description = "实际咨询时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实际咨询时间不能为空")
    private LocalDateTime actualTime;

    @Schema(description = "补录说明")
    private String notes;
}