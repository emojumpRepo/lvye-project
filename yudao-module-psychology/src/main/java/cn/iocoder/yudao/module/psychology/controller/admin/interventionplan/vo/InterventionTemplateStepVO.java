package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 干预计划模板步骤 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预计划模板步骤 VO")
@Data
public class InterventionTemplateStepVO {

    @Schema(description = "步骤ID", example = "1")
    private Long id;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "步骤排序不能为空")
    private Integer sort;

    @Schema(description = "步骤标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "初步评估")
    @NotBlank(message = "步骤标题不能为空")
    private String title;

}
