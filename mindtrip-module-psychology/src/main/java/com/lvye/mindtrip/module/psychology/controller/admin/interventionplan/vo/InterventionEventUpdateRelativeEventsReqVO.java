package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 干预事件更新关联事件列表 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预事件更新关联事件列表 Request VO")
@Data
public class InterventionEventUpdateRelativeEventsReqVO {

    @Schema(description = "干预事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "干预事件ID不能为空")
    private Long id;

    @Schema(description = "关联事件ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    @NotNull(message = "关联事件ID列表不能为null")
    private List<Long> relativeEventIds;

}
