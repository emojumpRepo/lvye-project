package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "管理后台 - 根据日期查询咨询预约 Request VO")
@Data
public class ConsultationAppointmentDateQueryReqVO {

    @Schema(description = "咨询师用户ID（不传表示查询所有咨询师）", example = "1")
    private Long counselorUserId;

    @Schema(description = "查询日期", required = true, example = "2025-10-22")
    @NotNull(message = "查询日期不能为空")
    private LocalDate date;
}
