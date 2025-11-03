package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 干预计划模板 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预计划模板 Response VO")
@Data
public class InterventionTemplateRespVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "模板标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "危机干预标准流程")
    private String title;

    @Schema(description = "是否官方模板", example = "false")
    private Boolean isOfficial;

    @Schema(description = "模板步骤列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<InterventionTemplateStepRespVO> steps;

}
