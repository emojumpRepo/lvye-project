package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 危机事件创建 Request VO")
@Data
public class CrisisEventCreateReqVO {

    @Schema(description = "学生档案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "学生档案不能为空")
    private Long studentProfileId;

    @Schema(description = "事件标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "事件标题不能为空")
    private String title;

    @Schema(description = "事件描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "事件描述不能为空")
    private String description;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "事发地点")
    private String location;

    @Schema(description = "风险等级")
    private Integer riskLevel;

    @Schema(description = "优先级", example = "1")
    private Integer priority = 2;

    @Schema(description = "附件ID列表")
    private List<Long> attachments;

    @Schema(description = "来源类型", example = "1")
    private Integer sourceType;
}