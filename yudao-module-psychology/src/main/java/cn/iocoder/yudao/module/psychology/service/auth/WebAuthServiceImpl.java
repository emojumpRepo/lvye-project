package cn.iocoder.yudao.module.psychology.service.auth;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.psychology.controller.app.auth.vo.WebAuthLoginReqVO;
import cn.iocoder.yudao.module.psychology.controller.app.auth.vo.WebAuthLoginRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.CaptchaVerificationReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.enums.oauth2.OAuth2ClientConstants;
import cn.iocoder.yudao.module.system.service.auth.AdminAuthService;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.social.SocialUserService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AUTH_LOGIN_CAPTCHA_CODE_ERROR;

/**
 * 学生家长端认证 Service 实现类
 */
@Service
@Validated
@Slf4j
public class WebAuthServiceImpl implements WebAuthService {

    /**
     * 验证码的开关，默认为 true
     */
    @Value("${yudao.captcha.enable:true}")
    @Setter // 为了单测：开启或者关闭验证码
    private Boolean captchaEnable;

    @Resource
    private CaptchaService captchaService;

    @Resource
    private Validator validator;

    @Resource
    private AdminUserMapper userMapper;

    @Resource
    private AdminUserService userService;

    @Resource
    private LoginLogService loginLogService;

    @Resource
    private SocialUserService socialUserService;

    @Resource
    AdminAuthService authService;

    @Resource
    private OAuth2TokenService oauth2TokenService;

    @Resource
    private StudentProfileService studentProfileService;

    @Resource
    private cn.iocoder.yudao.module.infra.api.config.ConfigApi configApi;

    private static final String KEY_ENABLE_PASSWORD_LOGIN = "student.enablePasswordLogin";


    @Override
    public WebAuthLoginRespVO login(WebAuthLoginReqVO reqVO) {
        // 根据学号查找学生档案
        log.info("尝试登录，学号: {}, 当前租户ID: {}", reqVO.getUsername(), TenantContextHolder.getTenantId());
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByNo(reqVO.getUsername());
        if (studentProfile == null) {
            log.warn("学生档案不存在，学号: {}, 租户ID: {}", reqVO.getUsername(), TenantContextHolder.getTenantId());
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
        // 校验学生姓名是否匹配：请求姓名需同时匹配档案姓名与账号昵称
        AdminUserDO account = userMapper.selectByUsername(reqVO.getUsername());
        String reqName = reqVO.getStudentName();
        boolean accountMatch = account != null && reqName != null && reqName.equals(account.getNickname());
        if (!accountMatch) {
            log.warn("学生姓名不匹配，学号: {}, 请求姓名: {}, 档案姓名: {}, 账号昵称: {}",
                    reqVO.getUsername(), reqName, studentProfile.getName(), account == null ? null : account.getNickname());
            throw exception(ErrorCodeConstants.STUDENT_NAME_NOT_MATCH);
        }
        log.info("找到学生档案: {}", studentProfile.getId());
        // 校验验证码
        validateCaptcha(reqVO);
        // 根据配置决定是否使用密码登录
        boolean enablePwdLogin = Boolean.parseBoolean(configApi.getConfigValueByKey(KEY_ENABLE_PASSWORD_LOGIN));
        AdminUserDO user;
        if (enablePwdLogin) {
            // 要求密码，进行账号密码认证
            user = authService.authenticate(reqVO.getUsername(), reqVO.getPassword());
        } else {
            // 不要求密码，仅校验用户存在和状态
            user = userMapper.selectByUsername(reqVO.getUsername());
            if (user == null) {
                throw exception(cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS);
            }
            if (cn.iocoder.yudao.framework.common.enums.CommonStatusEnum.DISABLE.getStatus().equals(user.getStatus())) {
                throw exception(cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_IS_DISABLE, user.getNickname());
            }
        }
        // 如果 socialType 非空，说明需要绑定社交用户
        if (reqVO.getSocialType() != null) {
            socialUserService.bindSocialUser(new SocialUserBindReqDTO(user.getId(), getUserType().getValue(),
                    reqVO.getSocialType(), reqVO.getSocialCode(), reqVO.getSocialState()));
        }
        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user.getId(), reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME, reqVO.getIsParent());
    }

    @Override
    public void logout(String token, Integer logType) {
        // 删除访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.removeAccessToken(token);
        if (accessTokenDO == null) {
            return;
        }
        // 删除成功，则记录登出日志
        createLogoutLog(accessTokenDO.getUserId(), accessTokenDO.getUserType(), logType);
    }

    @Override
    public WebAuthLoginRespVO refreshToken(String refreshToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, OAuth2ClientConstants.CLIENT_ID_DEFAULT);
        return WebAuthLoginRespVO.builder()
                .accessToken(accessTokenDO.getAccessToken())
                .refreshToken(accessTokenDO.getRefreshToken())
                .expiresTime(accessTokenDO.getExpiresTime())
                .userId(accessTokenDO.getUserId())
                .isParent(accessTokenDO.getIsParent())
                .build();
    }

    void validateCaptcha(WebAuthLoginReqVO reqVO) {
        ResponseModel response = doValidateCaptcha(reqVO);
        // 校验验证码
        if (!response.isSuccess()) {
            // 创建登录失败日志（验证码不正确)
            createLoginLog(null, reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME, LoginResultEnum.CAPTCHA_CODE_ERROR);
            throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR, response.getRepMsg());
        }
    }

    private ResponseModel doValidateCaptcha(CaptchaVerificationReqVO reqVO) {
        // 如果验证码关闭，则不进行校验
        if (!captchaEnable) {
            return ResponseModel.success();
        }
        ValidationUtils.validate(validator, reqVO, CaptchaVerificationReqVO.CodeEnableGroup.class);
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(reqVO.getCaptchaVerification());
        return captchaService.verification(captchaVO);
    }

    private void createLoginLog(Long userId, String username,
                                LoginLogTypeEnum logTypeEnum, LoginResultEnum loginResult) {
        // 插入登录日志
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logTypeEnum.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(getUserType().getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqDTO);
        // 更新最后登录时间
        if (userId != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            userService.updateUserLogin(userId, ServletUtils.getClientIP());
        }
    }

    private UserTypeEnum getUserType() {
        return UserTypeEnum.MEMBER;
    }

    private WebAuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String username, LoginLogTypeEnum logType, Integer isParent) {
        // 插入登陆日志
        createLoginLog(userId, username, logType, LoginResultEnum.SUCCESS);
        // 创建访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(userId, getUserType().getValue(),
                OAuth2ClientConstants.CLIENT_ID_DEFAULT, null, isParent);
        // 构建返回结果
        WebAuthLoginRespVO.WebAuthLoginRespVOBuilder authLoginRespVO = WebAuthLoginRespVO.builder();
        authLoginRespVO.userId(accessTokenDO.getUserId());
        authLoginRespVO.accessToken(accessTokenDO.getAccessToken());
        authLoginRespVO.refreshToken(accessTokenDO.getRefreshToken());
        authLoginRespVO.expiresTime(accessTokenDO.getExpiresTime());
        authLoginRespVO.isParent(accessTokenDO.getIsParent());
        return authLoginRespVO.build();
    }

    private void createLogoutLog(Long userId, Integer userType, Integer logType) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logType);
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(userType);
        reqDTO.setUsername(getUsername(userId));
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    private String getUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        AdminUserDO user = userService.getUser(userId);
        return user != null ? user.getUsername() : null;
    }


}