package cn.iocoder.yudao.module.psychology.controller.admin.profile;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentParentProfileReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.ParentContactDO;
import cn.iocoder.yudao.module.psychology.service.profile.StudentParentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:管理后台 - 学生档案 - 监护人管理
 * @Version: 1.0
 */
@Tag(name = "管理后台 - 学生家长管理")
@RestController
@RequestMapping("/psychology/student-parent-profile")
@Validated
public class StudentParentProfileController {

    @Resource
    private StudentParentProfileService studentParentProfileService;

    @PostMapping("/create")
    @Operation(summary = "创建学生监护人档案")
    public CommonResult<Boolean> createStudentProfile(@Valid @RequestBody StudentParentProfileReqVO createReqVO) {
        studentParentProfileService.createStudentParentContact(createReqVO);
        return success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "更新学生监护人档案")
    public CommonResult<Boolean> updateStudentProfile(@Valid @RequestBody StudentParentProfileReqVO createReqVO) {
        studentParentProfileService.updateStudentParentContact(createReqVO);
        return success(true);
    }

    @PostMapping("/delete")
    @Operation(summary = "刪除学生家长档案-根据家长ID")
    public CommonResult<Boolean> deleteStudentProfile(@RequestParam Long id) {
        studentParentProfileService.deleteStudentParentContact(id);
        return success(true);
    }

    @PostMapping("/delete-by-student")
    @Operation(summary = "刪除学生家长档案-根据学生档案ID")
    public CommonResult<Boolean> deleteStudentProfileByStudentProfileId(@RequestParam Long studentProfileId) {
        studentParentProfileService.deleteStudentParentContactByStudentProfileId(studentProfileId);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "查询学生监护人档案")
    public CommonResult<List<ParentContactDO>> createStudentProfile(@RequestParam Long studentProfileId) {
        List<ParentContactDO> result = studentParentProfileService.selectStudentParentContactByStudentProfileId(studentProfileId);
        return success(result);
    }

}
