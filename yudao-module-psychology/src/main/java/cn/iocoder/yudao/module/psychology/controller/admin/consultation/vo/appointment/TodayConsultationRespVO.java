package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 今日咨询 Response VO")
@Data
public class TodayConsultationRespVO {

    @Schema(description = "今日咨询总数", example = "10")
    private Integer totalCount;

    @Schema(description = "已完成数", example = "5")
    private Integer completedCount;

    @Schema(description = "待完成数", example = "3")
    private Integer pendingCount;

    @Schema(description = "已取消数", example = "2")
    private Integer cancelledCount;

    @Schema(description = "逾期数", example = "1")
    private Integer overdueCount;

    @Schema(description = "咨询列表")
    private List<ConsultationAppointmentRespVO> appointments;
}