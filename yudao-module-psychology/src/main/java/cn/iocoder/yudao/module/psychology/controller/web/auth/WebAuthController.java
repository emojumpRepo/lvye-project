package cn.iocoder.yudao.module.psychology.controller.web.auth;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.controller.web.auth.vo.WebAuthLoginReqVO;
import cn.iocoder.yudao.module.psychology.controller.web.auth.vo.WebAuthLoginRespVO;
import cn.iocoder.yudao.module.psychology.service.auth.WebAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getUserAgent;

@Tag(name = "学生家长端 - 认证")
@RestController
@RequestMapping("/web-api/psychology/auth")
@Validated
public class WebAuthController {

    @Resource
    private WebAuthService webAuthService;

    @PostMapping("/login")
    @Operation(summary = "学生/家长登录")
    public CommonResult<WebAuthLoginRespVO> login(@RequestBody @Valid WebAuthLoginReqVO reqVO,
                                                  HttpServletRequest request) {
        String token = webAuthService.login(reqVO, getClientIP(request), getUserAgent(request));
        return success(WebAuthLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            webAuthService.logout(token);
        }
        return success(true);
    }

}