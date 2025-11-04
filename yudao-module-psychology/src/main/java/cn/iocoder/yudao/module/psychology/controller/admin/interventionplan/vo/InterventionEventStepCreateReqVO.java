package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 新增干预事件步骤 Request VO")
@Data
public class InterventionEventStepCreateReqVO {

    @Schema(description = "干预事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "干预事件ID不能为空")
    private Long interventionId;

    @Schema(description = "步骤标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "第一次家访")
    @NotBlank(message = "步骤标题不能为空")
    @Size(max = 100, message = "步骤标题长度不能超过100个字符")
    private String title;

    @Schema(description = "排序值", example = "1")
    private Integer sort;

    @Schema(description = "步骤状态", example = "1")
    private Integer status;

    @Schema(description = "教师笔记", example = "初步沟通情况记录")
    @Size(max = 2000, message = "教师笔记长度不能超过2000个字符")
    private String notes;

    @Schema(description = "附件ID列表", example = "[1, 2, 3]")
    private List<Long> attachmentIds;
}
