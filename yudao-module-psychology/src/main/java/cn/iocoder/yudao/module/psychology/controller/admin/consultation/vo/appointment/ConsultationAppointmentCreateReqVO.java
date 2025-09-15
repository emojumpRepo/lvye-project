package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 咨询预约创建 Request VO")
@Data
public class ConsultationAppointmentCreateReqVO {

    @Schema(description = "学生档案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学生档案不能为空")
    private Long studentProfileId;

    @Schema(description = "预约时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentTime;

    @Schema(description = "咨询时长（分钟）", example = "60")
    private Integer durationMinutes = 60;

    @Schema(description = "咨询类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "咨询类型不能为空")
    private Integer consultationType;

    @Schema(description = "咨询地点", example = "心理咨询室201")
    private String location;

    @Schema(description = "备注", example = "学生情绪低落")
    private String notes;

    @Schema(description = "是否通知学生", example = "true")
    private Boolean notifyStudent = true;

    @Schema(description = "是否提醒自己", example = "true")
    private Boolean remindSelf = true;

    @Schema(description = "提前提醒时间（分钟）", example = "30")
    private Integer remindTime = 30;
}