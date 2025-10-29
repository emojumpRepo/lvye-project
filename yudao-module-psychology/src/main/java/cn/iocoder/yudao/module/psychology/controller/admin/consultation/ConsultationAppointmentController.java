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

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 心理咨询预约")
@RestController
@RequestMapping("/psychology/consultation")
@Validated
@Slf4j
public class ConsultationAppointmentController {

    @Resource
    private ConsultationAppointmentService appointmentService;

    @GetMapping("/statistics")
    @Operation(summary = "获取咨询预约统计数据")
    @DataPermission(enable = false)
    public CommonResult<ConsultationStatisticsRespVO> getStatistics() {
        return success(appointmentService.getStatistics());
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

    @PostMapping("/appointment/check-time-conflict")
    @Operation(summary = "校验预约时间冲突")
    @DataPermission(enable = false)
    public CommonResult<ConsultationAppointmentCheckTimeConflictRespVO> checkTimeConflict(
            @Valid @RequestBody ConsultationAppointmentCheckTimeConflictReqVO reqVO) {
        return success(appointmentService.checkTimeConflict(reqVO));
    }

    @GetMapping("/appointment/weekly")
    @Operation(summary = "获取周预约数据")
    @Parameter(name = "weekOffset", description = "周偏移量（0-当前周，-1-上一周，1-下一周）", example = "0")
    @DataPermission(enable = false)
    public CommonResult<ConsultationAppointmentWeeklyRespVO> getWeeklyAppointments(@Valid ConsultationAppointmentWeeklyReqVO reqVO) {
        return success(appointmentService.getWeeklyAppointments(reqVO));
    }

    @GetMapping("/appointment/time-range-data")
    @Operation(summary = "获取时间范围内的咨询数据")
    @DataPermission(enable = false)
    public CommonResult<ConsultationAppointmentTimeRangeRespVO> getTimeRangeData(@Valid ConsultationAppointmentTimeRangeReqVO reqVO) {
        return success(appointmentService.getTimeRangeData(reqVO));
    }

    @GetMapping("/appointment/by-date")
    @Operation(summary = "根据日期查询咨询预约数据")
    @DataPermission(enable = false)
    public CommonResult<ConsultationAppointmentDateQueryRespVO> getAppointmentsByDate(@Valid ConsultationAppointmentDateQueryReqVO reqVO) {
        return success(appointmentService.getAppointmentsByDate(reqVO));
    }

    @GetMapping("/appointment/list-by-student")
    @Operation(summary = "根据学生档案ID查询咨询记录列表")
    @Parameter(name = "studentProfileId", description = "学生档案ID", required = true)
    @DataPermission(enable = false)
    public CommonResult<List<ConsultationAppointmentRespVO>> getAppointmentsByStudentProfileId(@RequestParam("studentProfileId") Long studentProfileId) {
        List<ConsultationAppointmentRespVO> list = appointmentService.getAppointmentsByStudentProfileId(studentProfileId);
        return success(list);
    }

    @PutMapping("/appointment/{id}/save-summary")
    @Operation(summary = "保存咨询纪要")
    @DataPermission(enable = false)
    public CommonResult<Boolean> saveSummary(
            @PathVariable("id") Long id,
            @Valid @RequestBody ConsultationAppointmentSaveSummaryReqVO reqVO) {
        appointmentService.saveSummary(id, reqVO);
        return success(true);
    }
}