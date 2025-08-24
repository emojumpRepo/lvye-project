package cn.iocoder.yudao.module.psychology.controller.app.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskRespVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "学生家长端 - 测评任务")
@RestController
@RequestMapping("/psychology/assessment-task")
@Validated
public class WebAssessmentTaskController {

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @GetMapping("/my-tasks")
    @Operation(summary = "获得我的测评任务列表")
    @DataPermission(enable = false)
    public CommonResult<List<WebAssessmentTaskRespVO>> getMyAssessmentTasks() {
        List<WebAssessmentTaskVO> list = assessmentTaskService.selectListByUserId();
        return success(BeanUtils.toBean(list, WebAssessmentTaskRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得测评任务详情")
    @Parameter(name = "taskNo", description = "任务编号", required = true, example = "1024")
    @DataPermission(enable = false)
    public CommonResult<WebAssessmentTaskRespVO> getAssessmentTask(@RequestParam String taskNo) {
        AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTask(taskNo);
        return success(BeanUtils.toBean(assessmentTask, WebAssessmentTaskRespVO.class));
    }

}