package cn.iocoder.yudao.module.psychology.controller.app.profile;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileBasicInfoUpdateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileCompletenessRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.controller.app.profile.vo.WebStudentProfileRespVO;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getIsParent;

@Tag(name = "学生家长端 - 学生档案")
@RestController
@RequestMapping("/psychology/student-profile")
@Validated
public class WebStudentProfileController {

    @Resource
    private StudentProfileService studentProfileService;

    @GetMapping("/my-profile")
    @Operation(summary = "获得我的学生档案")
    public CommonResult<WebStudentProfileRespVO> getMyStudentProfile() {
        Long userId = getLoginUserId();
        StudentProfileVO studentProfile = studentProfileService.getStudentProfileDetailByUserId(userId);
        return success(BeanUtils.toBean(studentProfile, WebStudentProfileRespVO.class));
    }

    // -------------------- APP端补充基本信息与检查完善度 --------------------

    @PutMapping("/update-basic-info")
    @Operation(summary = "APP - 补充学生基本信息（家长无需补充）")
    public CommonResult<Boolean> updateMyBasicInfo(@RequestBody StudentProfileBasicInfoUpdateReqVO updateReqVO) {
        // 家长无需补充，直接返回成功
        boolean isParent = Integer.valueOf(1).equals(getIsParent());
        if (isParent) {
            return success(true);
        }
        Long userId = getLoginUserId();
        // 获取当前用户的学生档案ID
        StudentProfileDO profile = studentProfileService.getStudentProfileByUserId(userId);
        if (profile != null) {
            // 若前端未传ID，则使用当前用户档案ID
            if (updateReqVO.getId() == null) {
                updateReqVO.setId(profile.getId());
            }
        }
        studentProfileService.updateStudentBasicInfo(updateReqVO);
        return success(true);
    }

    @GetMapping("/check-completeness")
    @Operation(summary = "APP - 检查我的档案信息完善情况（家长视为已完善）")
    public CommonResult<StudentProfileCompletenessRespVO> checkMyProfileCompleteness() {
        // 家长不需要完善，直接满分返回
        boolean isParent = Integer.valueOf(1).equals(getIsParent());
        if (isParent) {
            StudentProfileCompletenessRespVO resp = new StudentProfileCompletenessRespVO();
            resp.setIsComplete(true);
            resp.setCompletenessPercentage(100);
            return success(resp);
        }
        Long userId = getLoginUserId();
        StudentProfileDO profile = studentProfileService.getStudentProfileByUserId(userId);
        Long studentProfileId = profile != null ? profile.getId() : null;
        StudentProfileCompletenessRespVO result = studentProfileService.checkProfileCompleteness(studentProfileId);
        return success(result);
    }

}