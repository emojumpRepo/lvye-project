package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学生独立评估提交 Request VO")
@Data
public class StudentAssessmentSubmitReqVO {

    @Schema(description = "学生档案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "学生档案ID不能为空")
    private Long studentProfileId;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "来源类型不能为空")
    private Integer sourceType;

    @Schema(description = "风险等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "风险等级不能为空")
    private Integer riskLevel;

    @Schema(description = "问题类型")
    private List<String> problemTypes;

    @Schema(description = "后续建议")
    private Integer followUpSuggestion;

    @Schema(description = "评估内容")
    private String content;

    @Schema(description = "是否有就诊用药情况")
    private Boolean hasMedicalVisit;

    @Schema(description = "就诊记录")
    private String medicalVisitRecord;

    @Schema(description = "持续关注记录")
    private String observationRecord;

    @Schema(description = "附件ID列表")
    private List<Long> attachmentIds;
}
