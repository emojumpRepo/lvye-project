package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 每日预约数据 VO")
@Data
public class DailyAppointmentVO {

    @Schema(description = "星期几标识（1-7，1是星期一，7是星期天）", example = "1")
    private Integer key;

    @Schema(description = "具体日期", example = "2025-10-20")
    private LocalDate date;

    @Schema(description = "该天的预约列表")
    private List<ConsultationAppointmentRespVO> appointments;

    @Schema(description = "预约数量", example = "5")
    private Integer count;
}
