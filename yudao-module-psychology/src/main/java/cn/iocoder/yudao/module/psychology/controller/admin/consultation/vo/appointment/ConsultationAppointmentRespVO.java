package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 咨询预约 Response VO")
@Data
public class ConsultationAppointmentRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "学生档案ID", example = "1")
    private Long studentProfileId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学生学号", example = "2021001")
    private String studentNumber;

    @Schema(description = "班级名称", example = "高一(1)班")
    private String className;

    @Schema(description = "咨询师ID", example = "1")
    private Long counselorUserId;

    @Schema(description = "咨询师姓名", example = "李老师")
    private String counselorName;

    @Schema(description = "预约开始时间")
    private LocalDateTime appointmentStartTime;

    @Schema(description = "预约结束时间")
    private LocalDateTime appointmentEndTime;

    @Schema(description = "咨询时长（分钟）", example = "60")
    private Integer durationMinutes;

    @Schema(description = "咨询类型", example = "初次咨询")
    private String consultationType;

    @Schema(description = "咨询地点", example = "心理咨询室201")
    private String location;

    @Schema(description = "备注", example = "学生情绪低落")
    private String notes;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "取消原因")
    private String cancellationReason;

    @Schema(description = "实际咨询时间")
    private LocalDateTime actualTime;

    @Schema(description = "是否逾期", example = "false")
    private Boolean overdue;

    @Schema(description = "是否通知学生", example = "true")
    private Boolean notifyStudent;

    @Schema(description = "是否提醒自己", example = "true")
    private Boolean remindSelf;

    @Schema(description = "提前提醒时间（分钟）", example = "30")
    private Integer remindTime;

    @Schema(description = "当前进度", example = "3")
    private Integer currentStep;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否已评估", example = "false")
    private Boolean hasAssessment;
}