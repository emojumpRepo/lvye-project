package com.lvye.mindtrip.module.psychology.controller.app.auth.vo;

import com.lvye.mindtrip.framework.common.validation.InEnum;
import com.lvye.mindtrip.framework.common.validation.Mobile;
import com.lvye.mindtrip.module.system.enums.sms.SmsSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "学生家长端 - 发送手机验证码 Request VO")
@Data
public class WebAuthSmsSendReqVO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691234")
    @Mobile
    @NotNull(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "发送场景,对应 SmsSceneEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "发送场景不能为空")
    @InEnum(SmsSceneEnum.class)
    private Integer scene;

}
