package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 周预约数据 Response VO")
@Data
public class ConsultationAppointmentWeeklyRespVO {

    @Schema(description = "周的开始日期（星期一）", example = "2025-10-20")
    private LocalDate weekStart;

    @Schema(description = "周的结束日期（星期日）", example = "2025-10-26")
    private LocalDate weekEnd;

    @Schema(description = "每日预约数据列表（按星期一到星期日排序）")
    private List<DailyAppointmentVO> dailyAppointments;
}
