package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 干预事件移除关联事件 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预事件移除关联事件 Request VO")
@Data
public class InterventionEventRemoveRelativeEventReqVO {

    @Schema(description = "干预事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "干预事件ID不能为空")
    private Long id;

    @Schema(description = "要移除的关联事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "关联事件ID不能为空")
    private Long relativeEventId;

}
