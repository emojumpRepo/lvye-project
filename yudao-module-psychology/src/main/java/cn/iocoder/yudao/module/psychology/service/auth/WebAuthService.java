package cn.iocoder.yudao.module.psychology.service.auth;

import cn.iocoder.yudao.module.psychology.controller.app.auth.vo.WebAuthLoginReqVO;
import cn.iocoder.yudao.module.psychology.controller.app.auth.vo.WebAuthLoginRespVO;
import cn.iocoder.yudao.module.psychology.controller.app.auth.vo.WebAuthSmsLoginReqVO;
import cn.iocoder.yudao.module.psychology.controller.app.auth.vo.WebAuthSmsSendReqVO;

/**
 * 学生家长端认证 Service 接口
 */
public interface WebAuthService {

    /**
     * 学生/家长登录
     *
     * @param reqVO 登录请求
     * @return 访问令牌
     */
    WebAuthLoginRespVO login(WebAuthLoginReqVO reqVO);

    /**
     * 手机 + 验证码登录
     *
     * @param reqVO 登录信息
     * @return 登录结果
     */
    WebAuthLoginRespVO smsLogin(WebAuthSmsLoginReqVO reqVO);

    /**
     * 发送手机验证码
     *
     * @param reqVO 发送信息
     */
    void sendSmsCode(WebAuthSmsSendReqVO reqVO);

    /**
     * 登出
     *
     * @param token 访问令牌
     */
    void logout(String token, Integer logType);

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 登录结果
     */
    WebAuthLoginRespVO  refreshToken(String refreshToken);

}