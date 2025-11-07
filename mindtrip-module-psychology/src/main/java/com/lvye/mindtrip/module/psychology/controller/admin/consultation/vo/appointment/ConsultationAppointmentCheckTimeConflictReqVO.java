package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 咨询预约时间冲突校验 Request VO")
@Data
public class ConsultationAppointmentCheckTimeConflictReqVO {

    @Schema(description = "咨询师用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "咨询师用户ID不能为空")
    private Long counselorUserId;

    @Schema(description = "预约开始时间戳（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1640966400000")
    @NotNull(message = "预约开始时间不能为空")
    private Long appointmentStartTime;

    @Schema(description = "预约结束时间戳（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1640970000000")
    @NotNull(message = "预约结束时间不能为空")
    private Long appointmentEndTime;

    @Schema(description = "排除的预约ID（用于更新场景）", example = "1")
    private Long excludeId;
}
