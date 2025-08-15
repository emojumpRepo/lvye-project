package cn.iocoder.yudao.module.psychology.controller.app.auth;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.app.auth.vo.WebAuthLoginReqVO;
import cn.iocoder.yudao.module.psychology.controller.app.auth.vo.WebAuthLoginRespVO;
import cn.iocoder.yudao.module.psychology.service.auth.WebAuthService;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getUserAgent;

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

}