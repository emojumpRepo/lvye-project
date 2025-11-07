package com.lvye.mindtrip.module.infra.controller.app.config;

import com.lvye.mindtrip.framework.common.pojo.CommonResult;
import com.lvye.mindtrip.module.infra.api.config.ConfigApi;
import com.lvye.mindtrip.module.infra.controller.app.config.vo.AppEnablePasswordLoginRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.lvye.mindtrip.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 参数配置")
@RestController
@RequestMapping("/infra/config")
@Validated
public class AppConfigController {

    private static final String KEY_ENABLE_PASSWORD_LOGIN = "student.enablePasswordLogin";

    @Resource
    private ConfigApi configApi;

    @GetMapping("/enable-password-login")
    @Operation(summary = "是否启用密码登录（匿名）")
    @PermitAll
    public CommonResult<AppEnablePasswordLoginRespVO> getEnablePasswordLogin() {
        String value = configApi.getConfigValueByKey(KEY_ENABLE_PASSWORD_LOGIN);
        boolean enabled = Boolean.parseBoolean(value);
        AppEnablePasswordLoginRespVO respVO = new AppEnablePasswordLoginRespVO();
        respVO.setEnablePasswordLogin(enabled);
        return success(respVO);
    }
}


