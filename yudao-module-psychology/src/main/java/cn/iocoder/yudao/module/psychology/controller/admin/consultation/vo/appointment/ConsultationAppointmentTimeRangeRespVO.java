package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 咨询数据时间范围 Response VO")
@Data
public class ConsultationAppointmentTimeRangeRespVO {

    @Schema(description = "时间粒度", example = "week")
    private String timeGranularity;

    @Schema(description = "查询范围开始日期", example = "2025-10-20")
    private LocalDate startDate;

    @Schema(description = "查询范围结束日期", example = "2025-10-26")
    private LocalDate endDate;

    @Schema(description = "总预约数量", example = "25")
    private Integer totalCount;

    @Schema(description = "按咨询师统计（仅当未指定咨询师时返回）")
    private List<CounselorStatVO> counselorStats;

    @Schema(description = "每日预约数据列表")
    private List<DailyAppointmentVO> dailyData;

    @Schema(description = "时间段汇总统计")
    private TimePeriodSummaryVO summary;
}
