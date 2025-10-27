package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 危机事件分配负责人 Request VO")
@Data
public class CrisisEventAssignReqVO {

    @Schema(description = "处理人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理人不能为空")
    private Long handlerUserId;

    // 不再需要分配原因，由后端自动生成
}