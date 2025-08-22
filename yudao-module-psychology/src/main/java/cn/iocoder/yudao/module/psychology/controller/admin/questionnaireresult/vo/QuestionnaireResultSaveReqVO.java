package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理端 - 问卷结果保存 Request VO")
@Data
public class QuestionnaireResultSaveReqVO {

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "问卷编号不能为空")
    private Long questionnaireId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "总分", example = "85")
    private Integer totalScore;

    @Schema(description = "满分", example = "100")
    private Integer maxScore;

    @Schema(description = "风险等级：1-正常，2-关注，3-预警，4-高危", example = "2")
    private Integer riskLevel;

    @Schema(description = "结果解读", example = "测试解读")
    private String resultInterpretation;

    @Schema(description = "建议", example = "测试建议")
    private String suggestions;

    @Schema(description = "答题时长（秒）", example = "900")
    private Integer answerDuration;
}