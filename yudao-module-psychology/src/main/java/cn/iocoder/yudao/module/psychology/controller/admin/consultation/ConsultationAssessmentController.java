package cn.iocoder.yudao.module.psychology.controller.admin.consultation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.assessment.ConsultationAssessmentRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.assessment.ConsultationAssessmentSaveReqVO;
import cn.iocoder.yudao.module.psychology.service.consultation.ConsultationAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 心理咨询评估")
@RestController
@RequestMapping("/psychology/consultation/assessment")
@Validated
@Slf4j
public class ConsultationAssessmentController {

    @Resource
    private ConsultationAssessmentService assessmentService;

    @GetMapping("/{appointmentId}")
    @Operation(summary = "获取咨询评估信息")
    @Parameter(name = "appointmentId", description = "咨询预约ID", required = true)
    @DataPermission(enable = false)
    public CommonResult<ConsultationAssessmentRespVO> getAssessment(@PathVariable("appointmentId") Long appointmentId) {
        return success(assessmentService.getAssessmentByAppointmentId(appointmentId));
    }

    @PostMapping("/save")
    @Operation(summary = "提交评估报告")
    @DataPermission(enable = false)
    public CommonResult<Long> saveAssessment(@Valid @RequestBody ConsultationAssessmentSaveReqVO saveReqVO) {
        return success(assessmentService.saveAssessment(saveReqVO));
    }

    @PostMapping("/draft")
    @Operation(summary = "保存评估草稿")
    @DataPermission(enable = false)
    public CommonResult<Long> saveDraft(@Valid @RequestBody ConsultationAssessmentSaveReqVO saveReqVO) {
        saveReqVO.setDraft(true);
        return success(assessmentService.saveDraft(saveReqVO));
    }

    @GetMapping("/admin/settings/assessment-overdue-time")
    @Operation(summary = "获取评估报告逾期时间阈值")
    @PreAuthorize("@ss.hasPermission('psychology:settings:query')")
    public CommonResult<Integer> getAssessmentOverdueTime() {
        return success(assessmentService.getAssessmentOverdueTime());
    }

    @PutMapping("/admin/settings/assessment-overdue-time")
    @Operation(summary = "设置评估报告逾期时间阈值")
    @PreAuthorize("@ss.hasPermission('psychology:settings:update')")
    public CommonResult<Boolean> setAssessmentOverdueTime(@RequestParam("hours") Integer hours) {
        assessmentService.setAssessmentOverdueTime(hours);
        return success(true);
    }
}