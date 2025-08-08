package cn.iocoder.yudao.module.psychology.controller.admin.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

@Tag(name = "管理后台 - 测评任务")
@RestController
@RequestMapping("/admin-api/psychology/assessment-task")
@Validated
public class AssessmentTaskController {

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建测评任务")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:create')")
    public CommonResult<Long> createAssessmentTask(@Valid @RequestBody AssessmentTaskSaveReqVO createReqVO) {
        return success(assessmentTaskService.createAssessmentTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新测评任务")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:update')")
    public CommonResult<Boolean> updateAssessmentTask(@Valid @RequestBody AssessmentTaskSaveReqVO updateReqVO) {
        assessmentTaskService.updateAssessmentTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除测评任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:delete')")
    public CommonResult<Boolean> deleteAssessmentTask(@RequestParam("id") Long id) {
        assessmentTaskService.deleteAssessmentTask(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得测评任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    public CommonResult<AssessmentTaskRespVO> getAssessmentTask(@RequestParam("id") Long id) {
        AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTask(id);
        return success(BeanUtils.toBean(assessmentTask, AssessmentTaskRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得测评任务分页")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    public CommonResult<PageResult<AssessmentTaskRespVO>> getAssessmentTaskPage(@Valid AssessmentTaskPageReqVO pageReqVO) {
        PageResult<AssessmentTaskDO> pageResult = assessmentTaskService.getAssessmentTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AssessmentTaskRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出测评任务 Excel")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:export')")
    @OperateLog(type = EXPORT)
    public void exportAssessmentTaskExcel(@Valid AssessmentTaskPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AssessmentTaskDO> list = assessmentTaskService.getAssessmentTaskPage(pageReqVO).getList();
        ExcelUtils.write(response, "测评任务.xls", "数据", AssessmentTaskRespVO.class,
                BeanUtils.toBean(list, AssessmentTaskRespVO.class));
    }

    @PutMapping("/publish/{id}")
    @Operation(summary = "发布测评任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:publish')")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> publishAssessmentTask(@PathVariable("id") Long id) {
        assessmentTaskService.publishAssessmentTask(id);
        return success(true);
    }

    @PutMapping("/close/{id}")
    @Operation(summary = "关闭测评任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:close')")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> closeAssessmentTask(@PathVariable("id") Long id) {
        assessmentTaskService.closeAssessmentTask(id);
        return success(true);
    }

    @PostMapping("/add-participants/{id}")
    @Operation(summary = "添加参与者")
    @Parameter(name = "id", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:manage-participants')")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> addParticipants(@PathVariable("id") Long id,
                                                 @RequestBody List<Long> studentProfileIds) {
        assessmentTaskService.addParticipants(id, studentProfileIds);
        return success(true);
    }

    @DeleteMapping("/remove-participants/{id}")
    @Operation(summary = "移除参与者")
    @Parameter(name = "id", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:manage-participants')")
    @OperateLog(type = UPDATE)
    public CommonResult<Boolean> removeParticipants(@PathVariable("id") Long id,
                                                    @RequestBody List<Long> studentProfileIds) {
        assessmentTaskService.removeParticipants(id, studentProfileIds);
        return success(true);
    }

    @GetMapping("/statistics/{id}")
    @Operation(summary = "获取任务统计信息")
    @Parameter(name = "id", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:statistics')")
    public CommonResult<AssessmentTaskStatisticsRespVO> getTaskStatistics(@PathVariable("id") Long id) {
        return success(assessmentTaskService.getTaskStatistics(id));
    }

}