package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 干预事件更新标题 Request VO")
@Data
public class InterventionEventUpdateTitleReqVO {

    @Schema(description = "干预事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "干预事件ID不能为空")
    private Long id;

    @Schema(description = "干预事件标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "情绪管理干预计划")
    @NotBlank(message = "干预事件标题不能为空")
    private String title;
}
