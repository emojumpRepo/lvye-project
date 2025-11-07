package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 时间段汇总统计 VO")
@Data
public class TimePeriodSummaryVO {

    @Schema(description = "总预约数", example = "25")
    private Integer totalCount;

    @Schema(description = "已预约数（状态=1）", example = "8")
    private Integer scheduledCount;

    @Schema(description = "已完成数（状态=2）", example = "10")
    private Integer completedCount;

    @Schema(description = "已闭环数（状态=3）", example = "5")
    private Integer closedLoopCount;

    @Schema(description = "已取消数（状态=4）", example = "2")
    private Integer canceledCount;

    @Schema(description = "逾期数", example = "3")
    private Integer overdueCount;

    @Schema(description = "总咨询时长（分钟）", example = "1200")
    private Integer totalDurationMinutes;

    @Schema(description = "平均咨询时长（分钟）", example = "60")
    private Integer avgDurationMinutes;
}
