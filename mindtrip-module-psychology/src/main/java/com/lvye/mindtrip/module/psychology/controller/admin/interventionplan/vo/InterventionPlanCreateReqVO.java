package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 干预计划创建请求 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预计划创建请求 VO")
@Data
public class InterventionPlanCreateReqVO {

    @Schema(description = "学生档案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学生档案ID不能为空")
    private Long studentProfileId;

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    @Schema(description = "干预计划标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "学生心理干预计划")
    @NotBlank(message = "干预计划标题不能为空")
    private String title;

}
