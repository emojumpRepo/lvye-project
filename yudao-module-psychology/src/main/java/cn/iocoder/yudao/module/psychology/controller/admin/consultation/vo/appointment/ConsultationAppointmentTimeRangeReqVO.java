package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "管理后台 - 咨询数据时间范围查询 Request VO")
@Data
public class ConsultationAppointmentTimeRangeReqVO {

    @Schema(description = "时间粒度（day-日，week-周，month-月）", required = true, example = "week", allowableValues = {"day", "week", "month"})
    @NotBlank(message = "时间粒度不能为空")
    private String timeGranularity;

    @Schema(description = "咨询师用户ID（不传表示查询所有咨询师）", example = "1")
    private Long counselorUserId;

    @Schema(description = "参考日期（不传使用当前日期）", example = "2025-10-22")
    private LocalDate referenceDate;

    @Schema(description = "时间偏移量（0-当前周期，-1-上一周期，1-下一周期）", example = "0")
    private Integer offset = 0;

    @Schema(description = "是否包含咨询师统计（默认 false，仅当未指定咨询师时有效）", example = "false")
    private Boolean includeCounselorStats = false;

    @Schema(description = "是否包含汇总统计（默认 false）", example = "false")
    private Boolean includeSummary = false;
}
