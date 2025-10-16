package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 干预等级学生 Response VO")
@Data
public class InterventionStudentRespVO {

    @Schema(description = "学生档案ID", example = "1")
    private Long studentProfileId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学号", example = "2021001")
    private String studentNumber;

    @Schema(description = "班级名称", example = "高一(1)班")
    private String className;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "当前风险等级", example = "1")
    private Integer currentRiskLevel;

    @Schema(description = "就读状态", example = "1")
    private Integer studyStatus;

    @Schema(description = "负责心理老师姓名")
    private String counselorName;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdateTime;

    @Schema(description = "最近评估时间")
    private LocalDateTime lastAssessmentTime;

    @Schema(description = "最近咨询时间")
    private LocalDateTime lastConsultationTime;

    @Schema(description = "危机事件数", example = "2")
    private Integer crisisEventCount;

    @Schema(description = "咨询次数", example = "5")
    private Integer consultationCount;

    @Schema(description = "标签")
    private String[] tags;
}