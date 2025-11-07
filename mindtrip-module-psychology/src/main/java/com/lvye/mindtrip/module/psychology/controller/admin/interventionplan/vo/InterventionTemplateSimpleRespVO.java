package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 干预计划模板简单信息 Response VO
 * 用于列表查询，只包含基本字段，不包含步骤详情
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预计划模板简单信息 Response VO")
@Data
public class InterventionTemplateSimpleRespVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "模板标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "危机干预标准流程")
    private String title;

    @Schema(description = "是否官方模板", example = "false")
    private Boolean isOfficial;

}
