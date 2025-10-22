package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 咨询预约统计 Response VO")
@Data
public class ConsultationStatisticsRespVO {

    @Schema(description = "今天的咨询数", example = "10")
    private Integer todayCount;

    @Schema(description = "已完成数(已闭环)", example = "5")
    private Integer completedCount;

    @Schema(description = "待完成数(未逾期)", example = "3")
    private Integer pendingCount;

    @Schema(description = "逾期数", example = "2")
    private Integer overdueCount;
}
