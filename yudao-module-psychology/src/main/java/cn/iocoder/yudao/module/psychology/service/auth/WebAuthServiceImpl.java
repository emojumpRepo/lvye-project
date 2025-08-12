package cn.iocoder.yudao.module.psychology.service.auth;

import cn.iocoder.yudao.module.psychology.controller.web.auth.vo.WebAuthLoginReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 学生家长端认证 Service 实现类
 */
@Service
@Validated
@Slf4j
public class WebAuthServiceImpl implements WebAuthService {

    @Resource
    private StudentProfileService studentProfileService;

    @Override
    public String login(WebAuthLoginReqVO reqVO, String userIp, String userAgent) {
        // 根据学号查找学生档案
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByNo(reqVO.getAccount());
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }

        // TODO: 这里应该调用member模块的登录服务
        // 暂时返回模拟token
        // 实际应该: 
        // 1. 验证密码
        // 2. 生成JWT token
        // 3. 记录登录日志
        return "psychology_token_" + studentProfile.getId();
    }

    @Override
    public void logout(String token) {
        // TODO: 实现登出逻辑
        // 1. 清除token缓存
        // 2. 记录登出日志
        log.info("用户登出, token: {}", token);
    }

}