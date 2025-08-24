package cn.iocoder.yudao.module.psychology.controller.app.auth.vo;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.psychology.enums.LoginTypeEnum;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.CaptchaVerificationReqVO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Schema(description = "学生家长端 - 登录 Request VO")
@Data
public class WebAuthLoginReqVO extends CaptchaVerificationReqVO {

    @Schema(description = "学号或手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024001")
    @NotBlank(message = "学号或手机号不能为空")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "是否为家长登录", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(LoginTypeEnum.class)
    private Integer isParent;

    // ========== 绑定社交登录时，需要传递如下参数 ==========

    @Schema(description = "社交平台的类型，参见 SocialTypeEnum 枚举值", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @InEnum(SocialTypeEnum.class)
    private Integer socialType;

    @Schema(description = "授权码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String socialCode;

    @Schema(description = "state", requiredMode = Schema.RequiredMode.REQUIRED, example = "9b2ffbc1-7425-4155-9894-9d5c08541d62")
    private String socialState;

    @AssertTrue(message = "授权码不能为空")
    public boolean isSocialCodeValid() {
        return socialType == null || StrUtil.isNotEmpty(socialCode);
    }

    @AssertTrue(message = "授权 state 不能为空")
    public boolean isSocialState() {
        return socialType == null || StrUtil.isNotEmpty(socialState);
    }

}