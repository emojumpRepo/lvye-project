package cn.iocoder.yudao.module.psychology.controller.web.profile;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.psychology.controller.web.profile.vo.WebStudentProfileRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "学生家长端 - 学生档案")
@RestController
@RequestMapping("/web-api/psychology/student-profile")
@Validated
public class WebStudentProfileController {

    @Resource
    private StudentProfileService studentProfileService;

    @GetMapping("/my")
    @Operation(summary = "获得我的学生档案")
    public CommonResult<WebStudentProfileRespVO> getMyStudentProfile() {
        Long memberUserId = getLoginUserId();
        StudentProfileDO studentProfile = studentProfileService.getStudentProfileByMemberUserId(memberUserId);
        return success(BeanUtils.toBean(studentProfile, WebStudentProfileRespVO.class));
    }

}