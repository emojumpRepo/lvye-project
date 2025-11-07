package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 根据日期查询咨询预约 Response VO")
@Data
public class ConsultationAppointmentDateQueryRespVO {

    @Schema(description = "查询日期", example = "2025-10-22")
    private LocalDate date;

    @Schema(description = "咨询师用户ID（如果指定了咨询师）", example = "1")
    private Long counselorUserId;

    @Schema(description = "预约总数量", example = "8")
    private Integer totalCount;

    @Schema(description = "预约列表")
    private List<ConsultationAppointmentRespVO> appointments;
}
