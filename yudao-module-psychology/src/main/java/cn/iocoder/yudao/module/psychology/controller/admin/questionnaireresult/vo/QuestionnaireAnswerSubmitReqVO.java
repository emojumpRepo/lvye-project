package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 问卷答案提交 Request VO")
@Data
public class QuestionnaireAnswerSubmitReqVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "答案数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"q1\":\"A\",\"q2\":\"B\"}")
    @NotBlank(message = "答案数据不能为空")
    private String answerData;

    @Schema(description = "访问记录ID", example = "1001")
    private Long accessId;

    @Schema(description = "会话时长（秒）", example = "300")
    private Integer sessionDuration;

}