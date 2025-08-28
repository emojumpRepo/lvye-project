package cn.iocoder.yudao.module.psychology.controller.admin.profile;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.timeline.StudentTimelineDO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.psychology.service.profile.StudentTimelineService;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 学生档案")
@RestController
@RequestMapping("/psychology/student-profile")
@Validated
@Slf4j
public class StudentProfileController {

    @Resource
    private StudentProfileService studentProfileService;

    @Resource
    private StudentTimelineService studentTimelineService;

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建学生档案")
    @DataPermission(enable = false)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:create')")
    public CommonResult<Long> createStudentProfile(@Valid @RequestBody StudentProfileSaveReqVO createReqVO) {
        return success(studentProfileService.createStudentProfile(createReqVO));
    }

    @PostMapping("/import-single")
    @Operation(summary = "导入单个学生档案")
    @DataPermission(enable = false)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:import')")
    public CommonResult<StudentProfileImportSingleRespVO> importOne(@Valid @RequestBody StudentProfileSaveReqVO reqVO) {
        try {
            Long id = studentProfileService.createStudentProfile(reqVO);
            return success(StudentProfileImportSingleRespVO.builder()
                    .success(true)
                    .message("导入成功")
                    .id(id)
                    .build());
        } catch (ServiceException e) {
            return success(StudentProfileImportSingleRespVO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .id(null)
                    .build());
        } catch (Exception e) {
            return success(StudentProfileImportSingleRespVO.builder()
                    .success(false)
                    .message("导入失败")
                    .id(null)
                    .build());
        }
    }

    @PostMapping("/update")
    @Operation(summary = "更新学生档案")
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateStudentProfile(@Valid @RequestBody StudentProfileSaveReqVO updateReqVO) {
        studentProfileService.updateStudentProfile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete/{studentProfileId}")
    @Operation(summary = "删除学生档案")
    @Parameter(name = "studentProfileId", description = "学生编号", required = true)
    @DataPermission(enable = false)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:delete')")
    public CommonResult<Boolean> deleteStudentProfile(@PathVariable("studentProfileId") Long studentProfileId) {
        studentProfileService.deleteStudentProfile(studentProfileId);
        return success(true);
    }

    @GetMapping("/get/{studentProfileId}")
    @Operation(summary = "获得学生档案")
    @Parameter(name = "studentProfileId", description = "学生编号", required = true, example = "1024")
    @DataPermission(enable = false)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:query')")
    public CommonResult<StudentProfileVO> getStudentProfile(@PathVariable("studentProfileId") Long studentProfileId) {
        StudentProfileVO studentProfile = studentProfileService.getStudentProfile(studentProfileId);
        return success(BeanUtils.toBean(studentProfile, StudentProfileVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生档案")
    @Parameter(name = "id", description = "学生编号", required = true, example = "1024")
    @DataPermission(enable = false)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:query')")
    public CommonResult<StudentProfileVO> getStudentProfileById(@RequestParam("id") Long id) {
        StudentProfileVO studentProfile = studentProfileService.getStudentProfile(id);
        return success(BeanUtils.toBean(studentProfile, StudentProfileVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生档案分页")
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:query')")
    @DataPermission(enable = false)
    public CommonResult<PageResult<StudentProfileVO>> getStudentProfilePage(@Valid StudentProfilePageReqVO pageReqVO) {
        PageResult<StudentProfileVO> pageResult = studentProfileService.getStudentProfilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StudentProfileVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得学生档案精简列表", description = "不分页，主要用于前端的下拉选项")
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:query')")
    @DataPermission(enable = false)
    public CommonResult<List<StudentProfileVO>> getStudentProfileSimpleList(StudentProfilePageReqVO reqVO) {
        List<StudentProfileVO> list = studentProfileService.getStudentProfileList(reqVO);
        return success(list);
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
    public CommonResult<StudentProfileImportRespVO> importStudentProfile(@RequestParam("file") MultipartFile file,
                                                                         @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        List<StudentImportExcelVO> list = ExcelUtils.read(file, StudentImportExcelVO.class);
        System.out.println("获取学生档案列表:"+list);
        return success(studentProfileService.importStudentProfile(list, updateSupport));
    }

    @PutMapping("/graduate/{studentProfileId}")
    @Operation(summary = "设置学生毕业状态")
    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:graduate')")
    public CommonResult<Boolean> graduateStudent(@PathVariable("studentProfileId") Long studentProfileId) {
        studentProfileService.graduateStudent(studentProfileId);
        return success(true);
    }

    @PutMapping("/psychological-status/{studentProfileId}")
    @Operation(summary = "更新学生心理状态")
    @DataPermission(enable = false)
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:update')")
    public CommonResult<Boolean> updatePsychologicalStatus(@PathVariable("studentProfileId") Long studentProfileId,
                                                           @RequestParam("psychologicalStatus") Integer psychologicalStatus,
                                                           @RequestParam("riskLevel") Integer riskLevel) {
        studentProfileService.updatePsychologicalStatus(studentProfileId, psychologicalStatus, riskLevel);
        return success(true);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入学生档案模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<StudentImportExcelVO> list = Arrays.asList(
                StudentImportExcelVO.builder().studentNo("123456").name("小明").sex(SexEnum.MALE.name())
                        .birthDate("2008-05-20").homeAddress("广东省广州市").mobile("13800013800")
                        .gradeName("一年级").className("一年级(1)班").build());
        // 输出
        ExcelUtils.write(response, "学生档案导入模板.xls", "学生列表", StudentImportExcelVO.class, list);
    }

    @GetMapping("/timeline-list")
    @Operation(summary = "获得学生时间线列表")
//    @PreAuthorize("@ss.hasPermission('psychology:student-profile:query')")
    @DataPermission(enable = false)
    public CommonResult<List<StudentTimelineDO>> getStudentTimeListList(@RequestParam String studentProfileId) {
        List<StudentTimelineDO> list = studentTimelineService.selectListByStudentProfileId(studentProfileId);
        return success(list);
    }

    @GetMapping("/student-task-list")
    @Operation(summary = "获得学生测评任务历史")
    @DataPermission(enable = false)
    public CommonResult<List<StudentAssessmentTaskHisVO>> getStudentAssessmentTaskList(@RequestParam Long studentProfileId) {
        List<StudentAssessmentTaskHisVO> result = assessmentTaskService.selectStudentAssessmentTaskList(studentProfileId);
        return success(result);
    }

    @GetMapping("/student-task-detail")
    @Operation(summary = "获得学生测评任务详情")
    @DataPermission(enable = false)
    public CommonResult<StudentAssessmentTaskDetailVO> getStudentAssessmentDetail(@RequestParam String taskNo, @RequestParam Long studentProfileId) {
        StudentAssessmentTaskDetailVO result = assessmentTaskService.selectStudentAssessmentTaskDetail(taskNo, studentProfileId);
        return success(result);
    }


}