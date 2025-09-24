package cn.iocoder.yudao.module.psychology.controller.app.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.CustomResponse;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentParticipantService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultCalculateService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "学生家长端 - 测评参与")
@RestController
@RequestMapping("/psychology/assessment-participant")
@Validated
public class WebAssessmentParticipantController {

    @Resource
    private AssessmentParticipantService assessmentParticipantService;

    @Resource
    private QuestionnaireResultCalculateService calculateService;

    @PostMapping("/start")
    @Operation(summary = "开始参与测评")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
    public CommonResult<Boolean> startAssessment(@RequestParam("taskNo") String taskNo) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        assessmentParticipantService.startAssessment(taskNo, userId);
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交测评答案")
    @PermitAll
    public CustomResponse<Boolean> submitAssessment(@Valid @RequestBody WebAssessmentParticipateReqVO participateReqVO) {
        assessmentParticipantService.submitAssessment(participateReqVO.getTaskNo(), participateReqVO.getUserId(), participateReqVO);
        return CustomResponse.success(true, "数据已接收");
    }

    /**
     * 提交测评答案 - 格式1：标准格式
     * 返回格式：{"code": 200, "message": "回调接收成功"}
     */
    @PostMapping("/submit-format1")
    @Operation(summary = "提交测评答案 - 格式1")
    @PermitAll
    public CustomResponse<Boolean> submitAssessmentFormat1(@Valid @RequestBody WebAssessmentParticipateReqVO participateReqVO) {
        assessmentParticipantService.submitAssessment(participateReqVO.getTaskNo(), participateReqVO.getUserId(), participateReqVO);
        return CustomResponse.success(200, "回调接收成功");
    }

    @GetMapping("/status")
    @Operation(summary = "获取测评参与状态")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
    public CommonResult<Integer> getAssessmentStatus(@RequestParam("taskNo") String taskNo) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        Integer status = assessmentParticipantService.getParticipantStatus(taskNo, userId);
        return success(status);
    }

    @PostMapping("/test")
    @Operation(summary = "提交测评答案")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
    public CommonResult<Object> test(@Valid @RequestBody WebAssessmentParticipateReqVO participateReqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        List<QuestionnaireResultVO> result =  calculateService.resultCalculate(participateReqVO.getQuestionnaireId(), userId, participateReqVO.getAnswers());
        return success(result);
    }


}