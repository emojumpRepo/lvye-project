package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 干预计划模板创建 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预计划模板创建 Request VO")
@Data
public class InterventionTemplateCreateReqVO {

    @Schema(description = "模板标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "危机干预标准流程")
    @NotBlank(message = "模板标题不能为空")
    private String title;

    @Schema(description = "是否官方模板", example = "false")
    private Boolean isOfficial = false;

    @Schema(description = "模板步骤列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "模板步骤不能为空")
    @Valid
    private List<InterventionTemplateStepVO> steps;

}
