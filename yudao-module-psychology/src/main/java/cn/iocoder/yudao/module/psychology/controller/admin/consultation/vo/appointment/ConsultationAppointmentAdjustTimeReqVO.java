package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 咨询预约调整时间 Request VO")
@Data
public class ConsultationAppointmentAdjustTimeReqVO {

    @Schema(description = "新预约开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "新预约开始时间不能为空")
    private LocalDateTime newAppointmentStartTime;

    @Schema(description = "新预约结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "新预约结束时间不能为空")
    private LocalDateTime newAppointmentEndTime;

    @Schema(description = "调整原因", example = "学生临时有事")
    private String reason;
}