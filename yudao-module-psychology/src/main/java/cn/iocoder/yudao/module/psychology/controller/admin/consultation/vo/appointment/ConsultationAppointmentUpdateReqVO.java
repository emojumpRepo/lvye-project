package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 咨询预约更新 Request VO")
@Data
public class ConsultationAppointmentUpdateReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;

    @Schema(description = "咨询时长（分钟）", example = "60")
    private Integer durationMinutes;

    @Schema(description = "咨询类型", example = "1")
    private Integer consultationType;

    @Schema(description = "咨询地点", example = "心理咨询室201")
    private String location;

    @Schema(description = "备注", example = "学生情绪低落")
    private String notes;

    @Schema(description = "是否通知学生", example = "true")
    private Boolean notifyStudent;

    @Schema(description = "是否提醒自己", example = "true")
    private Boolean remindSelf;

    @Schema(description = "提前提醒时间（分钟）", example = "30")
    private Integer remindTime;
}