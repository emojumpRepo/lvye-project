package cn.iocoder.yudao.module.psychology.controller.admin.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTemplateDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTemplateMapper;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
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

@Tag(name = "管理后台 - 测评任务")
@RestController
@RequestMapping("/psychology/assessment-task")
@Validated
public class AssessmentTaskController {

    @Resource
    private AssessmentTemplateMapper templateMapper;

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @GetMapping("/get-exam-template")
    @Operation(summary = "获得测评任务")
    @DataPermission(enable = false)
    public CommonResult<List<AssessmentTemplateDO>> getAssessmentTemplate() {
        List<AssessmentTemplateDO> result = templateMapper.selectTempalteList();
        return success(result);
    }

    @PostMapping("/create")
    @Operation(summary = "创建测评任务")
    @DataPermission(enable = false)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:create')")
    public CommonResult<Long> createAssessmentTask(@Valid @RequestBody AssessmentTaskSaveReqVO createReqVO) {
        return success(assessmentTaskService.createAssessmentTask(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新测评任务")
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateAssessmentTask(@Valid @RequestBody AssessmentTaskSaveReqVO updateReqVO) {
        assessmentTaskService.updateAssessmentTask(updateReqVO);
        return success(true);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除测评任务")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:delete')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> deleteAssessmentTask(@RequestParam("taskNo") String taskNo) {
        assessmentTaskService.deleteAssessmentTask(taskNo);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得测评任务")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    @DataPermission(enable = false)
    public CommonResult<AssessmentTaskRespVO> getAssessmentTask(@RequestParam("taskNo") String taskNo) {
        AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTask(taskNo);
        return success(BeanUtils.toBean(assessmentTask, AssessmentTaskRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得测评任务分页")
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    @DataPermission(enable = false)
    public CommonResult<PageResult<AssessmentTaskVO>> getAssessmentTaskPage(@Valid AssessmentTaskPageReqVO pageReqVO) {
        PageResult<AssessmentTaskVO> pageResult = assessmentTaskService.getAssessmentTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AssessmentTaskVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出测评任务 Excel")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:export')")
    public void exportAssessmentTaskExcel(@Valid AssessmentTaskPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AssessmentTaskVO> list = assessmentTaskService.getAssessmentTaskPage(pageReqVO).getList();
        ExcelUtils.write(response, "测评任务.xls", "数据", AssessmentTaskRespVO.class,
                BeanUtils.toBean(list, AssessmentTaskRespVO.class));
    }

    @PostMapping("/publish")
    @Operation(summary = "发布测评任务")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:publish')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> publishAssessmentTask(@RequestParam("taskNo") String taskNo) {
        assessmentTaskService.publishAssessmentTask(taskNo);
        return success(true);
    }

    @PostMapping("/close")
    @Operation(summary = "关闭测评任务")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:close')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> closeAssessmentTask(@RequestParam("taskNo") String taskNo) {
        assessmentTaskService.closeAssessmentTask(taskNo);
        return success(true);
    }

    @PostMapping("/add-participants")
    @Operation(summary = "添加参与者")
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:manage-participants')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> addParticipants(@RequestBody AssessmentTaskParticipantsReqVO reqVO) {
        assessmentTaskService.addParticipants(reqVO);
        return success(true);
    }

    @PostMapping("/remove-participants")
    @Operation(summary = "移除参与者")
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:manage-participants')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> removeParticipants(@RequestBody AssessmentTaskParticipantsReqVO reqVO) {
        assessmentTaskService.removeParticipants(reqVO);
        return success(true);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计信息")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:statistics')")
    @DataPermission(enable = false)
    public CommonResult<AssessmentTaskStatisticsRespVO> getTaskStatistics(@RequestParam("taskNo") String taskNo) {
        return success(assessmentTaskService.getTaskStatistics(taskNo));
    }

    @GetMapping("/participants-list")
    @Operation(summary = "获得测评任务人员列表")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    @DataPermission(enable = false)
    public CommonResult<List<AssessmentTaskUserVO>> getAssessmentTaskUserList(@RequestParam("taskNo") String taskNo) {
        List<AssessmentTaskUserVO> assessmentTaskUserList = assessmentTaskService.selectListByTaskNo(taskNo);
        return success(BeanUtils.toBean(assessmentTaskUserList, AssessmentTaskUserVO.class));
    }

    @PostMapping("/check-by-name")
    @Operation(summary = "检查测评任务名是否存在")
    @Parameter(name = "taskName", description = "任务名称", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> checkTaskName(@RequestParam("taskName") String taskName) {
        assessmentTaskService.validateTaskNameUnique(null, taskName);
        return success(true);
    }


}