package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 危机事件处理记录更新 Request VO")
@Data
public class CrisisEventProcessUpdateReqVO {

    @Schema(description = "处理记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理记录ID不能为空")
    private Long id;

    @Schema(description = "更新内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "更新内容不能为空")
    private String content;
}