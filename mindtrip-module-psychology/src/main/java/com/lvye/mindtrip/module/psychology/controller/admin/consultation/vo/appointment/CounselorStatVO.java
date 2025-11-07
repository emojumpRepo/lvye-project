package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 咨询师统计 VO")
@Data
public class CounselorStatVO {

    @Schema(description = "咨询师用户ID", example = "1")
    private Long counselorUserId;

    @Schema(description = "咨询师姓名", example = "李老师")
    private String counselorName;

    @Schema(description = "预约总数", example = "10")
    private Integer totalCount;

    @Schema(description = "已预约数（状态=1）", example = "3")
    private Integer scheduledCount;

    @Schema(description = "已完成数（状态=2）", example = "4")
    private Integer completedCount;

    @Schema(description = "已闭环数（状态=3）", example = "2")
    private Integer closedLoopCount;

    @Schema(description = "已取消数（状态=4）", example = "1")
    private Integer canceledCount;

    @Schema(description = "逾期数", example = "1")
    private Integer overdueCount;

    @Schema(description = "总咨询时长（分钟）", example = "600")
    private Integer totalDurationMinutes;
}
