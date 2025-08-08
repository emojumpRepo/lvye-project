package cn.iocoder.yudao.module.psychology.controller.app.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.AppAssessmentTaskRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "学生家长端 - 测评任务")
@RestController
@RequestMapping("/web-api/psychology/assessment-task")
@Validated
public class WebAssessmentTaskController {

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @GetMapping("/my-tasks")
    @Operation(summary = "获得我的测评任务列表")
    @PreAuthenticated
    public CommonResult<List<WebAssessmentTaskRespVO>> getMyAssessmentTasks() {
        // TODO: 根据当前用户获取相关测评任务
        // 这里需要根据学生档案获取参与的测评任务
        return success(null);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获得测评任务详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthenticated
    public CommonResult<WebAssessmentTaskRespVO> getAssessmentTask(@PathVariable("id") Long id) {
        AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTask(id);
        return success(BeanUtils.toBean(assessmentTask, WebAssessmentTaskRespVO.class));
    }

}