package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 干预事件步骤更新 Request VO")
@Data
public class InterventionEventStepUpdateReqVO {

    @Schema(description = "步骤ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "步骤ID不能为空")
    private Long id;

    @Schema(description = "步骤标题", example = "家访沟通")
    private String title;

    @Schema(description = "步骤状态", example = "2")
    private Integer status;

    @Schema(description = "教师笔记", example = "已完成家访，学生情绪稳定")
    private String notes;

    @Schema(description = "附件ID列表", example = "[1, 2, 3]")
    private List<Long> attachmentIds;
}
