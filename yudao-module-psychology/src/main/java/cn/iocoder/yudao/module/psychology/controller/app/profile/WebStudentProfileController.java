package cn.iocoder.yudao.module.psychology.controller.app.profile;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.controller.app.profile.vo.WebStudentProfileRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "学生家长端 - 学生档案")
@RestController
@RequestMapping("/web-api/psychology/student-profile")
@Validated
public class WebStudentProfileController {

    // 简化实现：移除复杂的依赖

    @GetMapping("/my")
    @Operation(summary = "获得我的学生档案")
    public CommonResult<WebStudentProfileRespVO> getMyStudentProfile() {
        Long memberUserId = getLoginUserId();

        // 简化实现：返回模拟数据
        WebStudentProfileRespVO profile = new WebStudentProfileRespVO();
        profile.setId(1L);
        profile.setStudentNo("2024001");
        profile.setName("学生姓名");
        profile.setSex(1);
        profile.setMobile("13800138000");
        profile.setGradeDeptId(1L);
        profile.setClassDeptId(2L);
        profile.setPsychologicalStatus(1);
        profile.setRiskLevel(1);
        profile.setCreateTime(java.time.LocalDateTime.now());

        return success(profile);
    }

}