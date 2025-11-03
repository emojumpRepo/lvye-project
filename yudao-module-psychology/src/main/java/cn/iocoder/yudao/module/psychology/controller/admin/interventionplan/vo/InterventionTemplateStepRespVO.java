package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 干预计划模板步骤 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预计划模板步骤 Response VO")
@Data
public class InterventionTemplateStepRespVO {

    @Schema(description = "步骤ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "步骤标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "初步评估")
    private String title;

}
