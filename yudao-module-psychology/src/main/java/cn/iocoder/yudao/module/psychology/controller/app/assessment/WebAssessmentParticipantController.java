package cn.iocoder.yudao.module.psychology.controller.app.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
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
@RequestMapping("/app-api/psychology/assessment-participant")
@Validated
public class WebAssessmentParticipantController {

    @Resource
    private AssessmentParticipantService assessmentParticipantService;

    @PostMapping("/start")
    @Operation(summary = "开始参与测评")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
    public CommonResult<Boolean> startAssessment(@RequestParam("taskNo") String taskNo) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        assessmentParticipantService.startAssessment(taskNo, userId);
        return success(true);
    }

    @PostMapping("/submit/{taskNo}")
    @Operation(summary = "提交测评答案")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
    public CommonResult<Boolean> submitAssessment(@PathVariable("taskNo") String taskNo,
                                                  @Valid @RequestBody WebAssessmentParticipateReqVO participateReqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        assessmentParticipantService.submitAssessment(taskNo, userId, participateReqVO);
        return success(true);
    }

    @GetMapping("/status")
    @Operation(summary = "获取测评参与状态")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
    public CommonResult<Integer> getAssessmentStatus(@RequestParam("taskNo") String taskNo) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        Integer status = assessmentParticipantService.getParticipantStatus(taskNo, userId);
        return success(status);
    }

}