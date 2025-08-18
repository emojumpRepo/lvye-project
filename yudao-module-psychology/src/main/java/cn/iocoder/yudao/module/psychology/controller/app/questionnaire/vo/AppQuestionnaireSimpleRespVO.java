package cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生家长端 - 问卷简单响应 VO
 */
@Schema(description = "学生家长端 - 问卷简单响应 VO")
@Data
public class AppQuestionnaireSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "问卷标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "心理健康测评问卷")
    private String title;

    @Schema(description = "问卷描述", example = "这是一份心理健康测评问卷，用于了解学生的心理状态")
    private String description;

    @Schema(description = "问卷类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "问卷类型描述", example = "心理测评")
    private String typeDesc;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "已发布")
    private String statusDesc;

    @Schema(description = "是否开放", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isOpen;

    @Schema(description = "访问次数", example = "100")
    private Integer accessCount;

    @Schema(description = "完成次数", example = "80")
    private Integer completionCount;

    @Schema(description = "完成率", example = "80.0")
    private Double completionRate;

    @Schema(description = "是否已完成", example = "false")
    private Boolean completed;

    @Schema(description = "是否可访问", example = "true")
    private Boolean accessible;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
