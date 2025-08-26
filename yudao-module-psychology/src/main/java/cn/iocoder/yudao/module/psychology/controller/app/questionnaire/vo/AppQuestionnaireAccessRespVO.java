package cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用端 - 问卷访问响应 VO
 */
@Schema(description = "应用端 - 问卷访问响应 VO")
@Data
public class AppQuestionnaireAccessRespVO {

    @Schema(description = "访问记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accessId;

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long questionnaireId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3072")
    private Long userId;

    @Schema(description = "访问时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime accessTime;

    @Schema(description = "访问IP", example = "192.168.1.1")
    private String accessIp;

    @Schema(description = "用户代理", example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "访问来源", example = "1")
    private Integer accessSource;

    @Schema(description = "会话时长（秒）", example = "300")
    private Integer sessionDuration;

    // 以下字段通过业务逻辑填充，不直接从 DO 映射
    @Schema(description = "问卷标题", example = "心理健康测评问卷")
    private String questionnaireTitle;

    @Schema(description = "外部链接", example = "https://example.com/questionnaire/123")
    private String externalLink;

    @Schema(description = "是否可访问", example = "true")
    private Boolean accessible;

    @Schema(description = "状态消息", example = "问卷可正常访问")
    private String statusMessage;

    @Schema(description = "预估完成时间（分钟）", example = "15")
    private Integer estimatedDuration;

    @Schema(description = "题目数量", example = "50")
    private Integer questionCount;

    @Schema(description = "问卷描述", example = "这是一份用于评估心理健康状况的专业问卷")
    private String description;

    @Schema(description = "问卷类型", example = "1")
    private Integer questionnaireType;

    @Schema(description = "是否已完成", example = "false")
    private Boolean completed;

    @Schema(description = "完成进度（百分比）", example = "75")
    private Integer progress;

}
