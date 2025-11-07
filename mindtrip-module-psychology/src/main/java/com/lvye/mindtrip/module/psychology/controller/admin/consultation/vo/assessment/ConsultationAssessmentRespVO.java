package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 咨询评估 Response VO")
@Data
public class ConsultationAssessmentRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "咨询预约ID", example = "1")
    private Long appointmentId;

    @Schema(description = "学生档案ID", example = "1")
    private Long studentProfileId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学生学号", example = "2021001")
    private String studentNumber;

    @Schema(description = "班级名称", example = "高一(1)班")
    private String className;

    @Schema(description = "评估人ID", example = "1")
    private Long counselorUserId;

    @Schema(description = "评估人姓名", example = "李老师")
    private String counselorName;

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "问题类型识别")
    private List<String> problemTypes;

    @Schema(description = "后续处理建议", example = "1")
    private Integer followUpSuggestion;

    @Schema(description = "评估内容")
    private String content;

    @Schema(description = "是否就医")
    private Boolean hasMedicalVisit;

    @Schema(description = "就医记录")
    private String medicalVisitRecord;

    @Schema(description = "观察记录")
    private String observationRecord;

    @Schema(description = "附件ID列表")
    private List<Long> attachmentIds;

    @Schema(description = "是否为草稿", example = "false")
    private Boolean draft;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "咨询时间")
    private LocalDateTime appointmentTime;
}