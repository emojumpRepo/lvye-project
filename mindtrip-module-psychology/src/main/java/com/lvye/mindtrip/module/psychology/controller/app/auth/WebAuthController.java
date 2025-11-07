package com.lvye.mindtrip.module.psychology.controller.app.auth;

import cn.hutool.core.util.StrUtil;
import com.lvye.mindtrip.framework.common.pojo.CommonResult;
import com.lvye.mindtrip.framework.security.config.SecurityProperties;
import com.lvye.mindtrip.framework.security.core.util.SecurityFrameworkUtils;
import com.lvye.mindtrip.framework.web.core.util.WebFrameworkUtils;
import com.lvye.mindtrip.module.psychology.controller.app.auth.vo.WebAuthLoginReqVO;
import com.lvye.mindtrip.module.psychology.controller.app.auth.vo.WebAuthLoginRespVO;
import com.lvye.mindtrip.module.psychology.controller.app.auth.vo.WebAuthSmsLoginReqVO;
import com.lvye.mindtrip.module.psychology.controller.app.auth.vo.WebAuthSmsSendReqVO;
import com.lvye.mindtrip.module.psychology.service.auth.WebAuthService;
import com.lvye.mindtrip.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import com.lvye.mindtrip.module.system.enums.logger.LoginLogTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.lvye.mindtrip.framework.common.pojo.CommonResult.success;
import static com.lvye.mindtrip.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.lvye.mindtrip.framework.common.util.servlet.ServletUtils.getUserAgent;

@Tag(name = "学生家长端 - 认证")
@RestController
@RequestMapping("/psychology/auth")
@Validated
public class WebAuthController {

    @Resource
    private WebAuthService webAuthService;

    @Resource
    private SecurityProperties securityProperties;

    @PostMapping("/login")
    @Operation(summary = "学生/家长登录")
    @PermitAll
    public CommonResult<WebAuthLoginRespVO> login(@RequestBody @Valid WebAuthLoginReqVO reqVO) {
        return success(webAuthService.login(reqVO));
    }

    @PostMapping("/sms-login")
    @Operation(summary = "学生/家长手机验证码登录")
    @PermitAll
    public CommonResult<WebAuthLoginRespVO> smsLogin(@RequestBody @Valid WebAuthSmsLoginReqVO reqVO) {
        return success(webAuthService.smsLogin(reqVO));
    }

    @PostMapping("/send-sms-code")
    @Operation(summary = "发送手机验证码")
    @PermitAll
    public CommonResult<Boolean> sendSmsCode(@RequestBody @Valid WebAuthSmsSendReqVO reqVO) {
        webAuthService.sendSmsCode(reqVO);
        return success(true);
    }

    @PostMapping("/logout")
    @PermitAll
    @Operation(summary = "登出系统")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotBlank(token)) {
            webAuthService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @PermitAll
    @Operation(summary = "刷新令牌")
    @Parameter(name = "refreshToken", description = "刷新令牌", required = true)
    public CommonResult<WebAuthLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(webAuthService.refreshToken(refreshToken));
    }

    @PostMapping("/test")
    @Operation(summary = "学生/家长登录")
    @PermitAll
    public CommonResult<Object> test() {
        return success(WebFrameworkUtils.getIsParent());
    }

}