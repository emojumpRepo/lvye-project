package cn.iocoder.yudao.module.psychology.controller.web.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.controller.web.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "学生家长端 - 测评参与")
@RestController
@RequestMapping("/web-api/psychology/assessment-participant")
@Validated
public class WebAssessmentParticipantController {

    @Resource
    private AssessmentParticipantService assessmentParticipantService;

    @PostMapping("/start/{taskId}")
    @Operation(summary = "开始参与测评")
    @Parameter(name = "taskId", description = "任务编号", required = true)
    public CommonResult<Boolean> startAssessment(@PathVariable("taskNo") String taskNo,
                                                 @RequestParam(value = "isParent", defaultValue = "false") Boolean isParent) {
        Long memberUserId = getLoginUserId();
        assessmentParticipantService.startAssessment(taskNo, memberUserId, isParent);
        return success(true);
    }

    @PostMapping("/submit/{taskId}")
    @Operation(summary = "提交测评答案")
    @Parameter(name = "taskId", description = "任务编号", required = true)
    public CommonResult<Boolean> submitAssessment(@PathVariable("taskId") Long taskId,
                                                  @Valid @RequestBody WebAssessmentParticipateReqVO participateReqVO) {
        Long memberUserId = getLoginUserId();
        assessmentParticipantService.submitAssessment(taskId, memberUserId, participateReqVO);
        return success(true);
    }

    @GetMapping("/status/{taskId}")
    @Operation(summary = "获取测评参与状态")
    @Parameter(name = "taskId", description = "任务编号", required = true)
    public CommonResult<Integer> getAssessmentStatus(@PathVariable("taskId") Long taskId) {
        Long memberUserId = getLoginUserId();
        Integer status = assessmentParticipantService.getParticipantStatus(taskId, memberUserId);
        return success(status);
    }

}