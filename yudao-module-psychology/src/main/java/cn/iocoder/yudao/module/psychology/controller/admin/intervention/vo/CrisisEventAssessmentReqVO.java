package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 危机事件阶段性评估 Request VO")
@Data
public class CrisisEventAssessmentReqVO {

    @Schema(description = "风险等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "风险等级不能为空")
    private Integer riskLevel;

    @Schema(description = "问题类型")
    private List<String> problemTypes;

    @Schema(description = "后续建议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "后续建议不能为空")
    private Integer followUpSuggestion;

    @Schema(description = "评估内容")
    private String content;
}