package cn.iocoder.yudao.module.infra.controller.app.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 是否启用密码登录响应 VO")
@Data
public class AppEnablePasswordLoginRespVO {

    @Schema(description = "是否启用密码登录", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private boolean enablePasswordLogin;
}


