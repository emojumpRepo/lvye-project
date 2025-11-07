package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 干预计划模板更新 Request VO")
@Data
public class InterventionTemplateUpdateReqVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模板ID不能为空")
    private Long id;

    @Schema(description = "模板标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "情绪管理干预计划")
    @NotBlank(message = "模板标题不能为空")
    private String title;

    @Schema(description = "是否是官方模板", example = "false")
    private Boolean isOfficial;

    @Schema(description = "干预步骤列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "模板步骤不能为空")
    @Valid
    private List<InterventionTemplateStepVO> steps;
}
