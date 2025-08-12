package cn.iocoder.yudao.module.psychology.controller.admin.profile;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 学生档案")
@RestController
@RequestMapping("/psychology/student-profile")
@Validated
public class StudentProfileController {

    @Resource
    private StudentProfileService studentProfileService;

    @PostMapping("/create")
    @Operation(summary = "创建学生档案")
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:create')")
    public CommonResult<Long> createStudentProfile(@Valid @RequestBody StudentProfileSaveReqVO createReqVO) {
        return success(studentProfileService.createStudentProfile(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新学生档案")
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:update')")
    public CommonResult<Boolean> updateStudentProfile(@Valid @RequestBody StudentProfileSaveReqVO updateReqVO) {
        studentProfileService.updateStudentProfile(updateReqVO);
        return success(true);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除学生档案")
    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:delete')")
    public CommonResult<Boolean> deleteStudentProfile(@RequestParam("id") Long id) {
        studentProfileService.deleteStudentProfile(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生档案")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:query')")
    public CommonResult<StudentProfileRespVO> getStudentProfile(@RequestParam("studentProfileId") Long studentProfileId) {
        StudentProfileDO studentProfile = studentProfileService.getStudentProfile(studentProfileId);
        return success(BeanUtils.toBean(studentProfile, StudentProfileRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生档案分页")
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:query')")
    @DataPermission(enable = false)
    public CommonResult<PageResult<StudentProfileVO>> getStudentProfilePage(@Valid StudentProfilePageReqVO pageReqVO) {
        PageResult<StudentProfileVO> pageResult = studentProfileService.getStudentProfilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StudentProfileVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学生档案 Excel")
    @PreAuthorize("@ss.hasPermission('psychology:student-profile:export')")
    public void exportStudentProfileExcel(@Valid StudentProfilePageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StudentProfileVO> list = studentProfileService.getStudentProfilePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "学生档案.xls", "数据", StudentProfileRespVO.class,
                BeanUtils.toBean(list, StudentProfileRespVO.class));
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入学生档案")
    @PreAuthorize("@ss.hasPermission('psychology:student-profile:import')")
    public CommonResult<StudentProfileImportRespVO> importStudentProfile(@Valid @RequestBody StudentProfileImportReqVO importReqVO) {
        return success(studentProfileService.importStudentProfile(importReqVO));
    }

    @PutMapping("/graduate/{id}")
    @Operation(summary = "设置学生毕业状态")
    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:graduate')")
    public CommonResult<Boolean> graduateStudent(@PathVariable("id") Long id) {
        studentProfileService.graduateStudent(id);
        return success(true);
    }

    @PutMapping("/psychological-status/{id}")
    @Operation(summary = "更新学生心理状态")
    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:update')")
    public CommonResult<Boolean> updatePsychologicalStatus(@PathVariable("id") Long id,
                                                           @RequestParam("psychologicalStatus") Integer psychologicalStatus,
                                                           @RequestParam("riskLevel") Integer riskLevel) {
        studentProfileService.updatePsychologicalStatus(id, psychologicalStatus, riskLevel);
        return success(true);
    }

}