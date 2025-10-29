package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 咨询评估保存 Request VO")
@Data
public class ConsultationAssessmentSaveReqVO {

    @Schema(description = "咨询预约ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "咨询预约ID不能为空")
    private Long appointmentId;

    @Schema(description = "风险等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "风险等级不能为空")
    private Integer riskLevel;

    @Schema(description = "问题类型识别")
    private List<String> problemTypes;

    @Schema(description = "后续处理建议")
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
    private Boolean draft = false;
}