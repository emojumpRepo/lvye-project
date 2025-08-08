package cn.iocoder.yudao.module.psychology.controller.web.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "学生家长端 - 登录 Response VO")
@Data
@Builder
public class WebAuthLoginRespVO {

    @Schema(description = "访问令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "happy")
    private String token;

}