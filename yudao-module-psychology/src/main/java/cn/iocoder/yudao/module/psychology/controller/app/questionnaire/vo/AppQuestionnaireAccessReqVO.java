package cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 应用端 - 问卷访问请求 VO
 */
@Schema(description = "应用端 - 问卷访问请求 VO")
@Data
public class AppQuestionnaireAccessReqVO {

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "问卷编号不能为空")
    private Long questionnaireId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "访问来源", example = "1")
    private Integer accessSource;

    @Schema(description = "访问IP", example = "192.168.1.1")
    private String accessIp;

    @Schema(description = "用户代理", example = "Mozilla/5.0")
    private String userAgent;

}
