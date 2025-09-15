package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 危机事件重新分配负责人 Request VO")
@Data
public class CrisisEventReassignReqVO {

    @Schema(description = "新处理人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "新处理人不能为空")
    private Long newHandlerUserId;

    @Schema(description = "变更原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "变更原因不能为空")
    private String reason;
}