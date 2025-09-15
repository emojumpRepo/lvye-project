package cn.iocoder.yudao.module.psychology.controller.admin.consultation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.*;
import cn.iocoder.yudao.module.psychology.service.consultation.ConsultationAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 心理咨询预约")
@RestController
@RequestMapping("/psychology/consultation")
@Validated
@Slf4j
public class ConsultationAppointmentController {

    @Resource
    private ConsultationAppointmentService appointmentService;

    @GetMapping("/today")
    @Operation(summary = "获取今日咨询列表和统计")
    @DataPermission(enable = false)
    public CommonResult<TodayConsultationRespVO> getTodayConsultations() {
        return success(appointmentService.getTodayConsultations());
    }

    @PostMapping("/appointment/create")
    @Operation(summary = "创建咨询预约")
    @DataPermission(enable = false)
    public CommonResult<Long> createAppointment(@Valid @RequestBody ConsultationAppointmentCreateReqVO createReqVO) {
        return success(appointmentService.createAppointment(createReqVO));
    }

    @PutMapping("/appointment/update")
    @Operation(summary = "更新咨询预约")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateAppointment(@Valid @RequestBody ConsultationAppointmentUpdateReqVO updateReqVO) {
        appointmentService.updateAppointment(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/appointment/delete")
    @Operation(summary = "删除咨询预约")
    @Parameter(name = "id", description = "预约ID", required = true)
    @DataPermission(enable = false)
    public CommonResult<Boolean> deleteAppointment(@RequestParam("id") Long id) {
        appointmentService.deleteAppointment(id);
        return success(true);
    }

    @GetMapping("/appointment/get")
    @Operation(summary = "获得咨询预约")
    @Parameter(name = "id", description = "预约ID", required = true, example = "1024")
    @DataPermission(enable = false)
    public CommonResult<ConsultationAppointmentRespVO> getAppointment(@RequestParam("id") Long id) {
        ConsultationAppointmentRespVO appointment = appointmentService.getAppointment(id);
        return success(appointment);
    }

    @GetMapping("/appointment/page")
    @Operation(summary = "获得咨询预约分页")
    @DataPermission(enable = false)
    public CommonResult<PageResult<ConsultationAppointmentRespVO>> getAppointmentPage(@Valid ConsultationAppointmentPageReqVO pageVO) {
        PageResult<ConsultationAppointmentRespVO> pageResult = appointmentService.getAppointmentPage(pageVO);
        return success(pageResult);
    }

    @PutMapping("/appointment/{id}/complete")
    @Operation(summary = "完成咨询")
    @DataPermission(enable = false)
    public CommonResult<Boolean> completeAppointment(
            @PathVariable("id") Long id,
            @RequestParam(value = "fillAssessmentNow", defaultValue = "false") Boolean fillAssessmentNow) {
        appointmentService.completeAppointment(id, fillAssessmentNow);
        return success(true);
    }

    @PutMapping("/appointment/{id}/adjust-time")
    @Operation(summary = "调整预约时间")
    @DataPermission(enable = false)
    public CommonResult<Boolean> adjustAppointmentTime(
            @PathVariable("id") Long id,
            @Valid @RequestBody ConsultationAppointmentAdjustTimeReqVO adjustReqVO) {
        appointmentService.adjustAppointmentTime(id, adjustReqVO);
        return success(true);
    }

    @PutMapping("/appointment/{id}/cancel")
    @Operation(summary = "取消预约")
    @DataPermission(enable = false)
    public CommonResult<Boolean> cancelAppointment(
            @PathVariable("id") Long id,
            @Valid @RequestBody ConsultationAppointmentCancelReqVO cancelReqVO) {
        appointmentService.cancelAppointment(id, cancelReqVO);
        return success(true);
    }

    @PutMapping("/appointment/{id}/supplement")
    @Operation(summary = "补录咨询记录")
    @DataPermission(enable = false)
    public CommonResult<Boolean> supplementAppointment(
            @PathVariable("id") Long id,
            @Valid @RequestBody ConsultationAppointmentSupplementReqVO supplementReqVO) {
        appointmentService.supplementAppointment(id, supplementReqVO);
        return success(true);
    }

    @PostMapping("/appointment/{id}/remind")
    @Operation(summary = "发送催办提醒")
    @DataPermission(enable = false)
    public CommonResult<Boolean> sendReminder(@PathVariable("id") Long id) {
        appointmentService.sendReminder(id);
        return success(true);
    }
}