package cn.iocoder.yudao.module.psychology.controller.admin.common;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherImportExcelVO;
import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherProfileImportRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentClassVO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.service.profile.TeacherService;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-21
 * @Description:教师端-通用功能
 * @Version: 1.0
 */
@Tag(name = "教师端 - 通用功能")
@RestController
@RequestMapping("/common")
@Validated
public class AdminCommonController {

    @Resource
    private DeptService deptService;

    @Resource
    private TeacherService teacherService;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @GetMapping("/teacher/get-import-template")
    @Operation(summary = "获得导入教师模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<TeacherImportExcelVO> list = Arrays.asList(
                TeacherImportExcelVO.builder().jobNo("123456").name("小明").role("普通老师").mobile("13800013800")
                        .className("一年级(1)班,一年级(2)班").headTeacherClassName("一年级(1)班")
                        .manageGradeName("一年级").build());
        // 输出
        ExcelUtils.write(response, "教师导入模板.xls", "教师列表", TeacherImportExcelVO.class, list);
    }

    @PostMapping("/teacher/import")
    @Operation(summary = "批量导入教师")
    public CommonResult<TeacherProfileImportRespVO> importStudentProfile(@RequestParam("file") MultipartFile file,
                                                                         @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        List<TeacherImportExcelVO> list = ExcelUtils.read(file, TeacherImportExcelVO.class);
        return success(teacherService.importTeacher(list, updateSupport));
    }

    @GetMapping(value = {"/dept/list-all-simple", "/dept/simple-list"})
    @Operation(summary = "获取部门精简信息列表（教师端）", description = "只包含被开启的部门，主要用于前端的下拉选项")
    public CommonResult<List<DeptSimpleRespVO>> getSimpleDeptList() {
        List<DeptDO> list = deptService.getDeptList(
                new DeptListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus()));
        List<StudentClassVO> classCountList = studentProfileMapper.selectClassStudentCount();
        List<StudentClassVO> gradeCountList = studentProfileMapper.selectGradeStudentCount();
        //设置班级人数
        for (StudentClassVO classVO : classCountList) {
            list.forEach(deptDO -> {
                if (deptDO.getId().equals(classVO.getDeptId()))
                    deptDO.setCount(classVO.getCount());
            });
        }
        //设置年级人数
        for (StudentClassVO gradeVO : gradeCountList) {
            list.forEach(deptDO -> {
                if (deptDO.getId().equals(gradeVO.getDeptId()))
                    deptDO.setCount(gradeVO.getCount());
            });
        }
        return success(BeanUtils.toBean(list, DeptSimpleRespVO.class));
    }

}
