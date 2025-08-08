package cn.iocoder.yudao.module.psychology.service.auth;

import cn.iocoder.yudao.module.psychology.controller.web.auth.vo.WebAuthLoginReqVO;

/**
 * 学生家长端认证 Service 接口
 */
public interface WebAuthService {

    /**
     * 学生/家长登录
     *
     * @param reqVO 登录请求
     * @param userIp 用户IP
     * @param userAgent 用户Agent
     * @return 访问令牌
     */
    String login(WebAuthLoginReqVO reqVO, String userIp, String userAgent);

    /**
     * 登出
     *
     * @param token 访问令牌
     */
    void logout(String token);

}