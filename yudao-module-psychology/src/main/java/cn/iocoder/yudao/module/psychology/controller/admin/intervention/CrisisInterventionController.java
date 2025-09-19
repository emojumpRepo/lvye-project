package cn.iocoder.yudao.module.psychology.controller.admin.intervention;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.*;
import cn.iocoder.yudao.module.psychology.service.intervention.CrisisInterventionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 危机干预")
@RestController
@RequestMapping("/psychology/intervention")
@Validated
@Slf4j
public class CrisisInterventionController {

    @Resource
    private CrisisInterventionService interventionService;

    // ========== 五级干预看板 ==========

    @GetMapping("/dashboard/summary")
    @Operation(summary = "获取五级干预看板统计数据")
    @DataPermission(enable = false)
    public CommonResult<List<InterventionDashboardLevelVO>> getDashboardSummary(
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "counselorUserId", required = false) Long counselorUserId,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return success(interventionService.getDashboardLevels(classId, counselorUserId, pageSize));
    }

    @PostMapping("/dashboard/summary/page")
    @Operation(summary = "获取五级干预看板统计数据")
    @DataPermission(enable = false)
    public CommonResult<InterventionDashboardSummaryVO> getDashboardSummaryWithPage(
            @Valid @RequestBody InterventionDashboardReqVO reqVO) {
        return success(interventionService.getDashboardSummaryWithPage(reqVO));
    }

    @GetMapping("/dashboard/students/page")
    @Operation(summary = "获取指定干预等级的学生列表")
    @DataPermission(enable = false)
    public CommonResult<PageResult<InterventionStudentRespVO>> getStudentsByLevel(
            @RequestParam("level") String level,
            @Valid InterventionStudentPageReqVO pageReqVO) {
        return success(interventionService.getStudentsByLevel(level, pageReqVO));
    }

    @PutMapping("/student/{studentProfileId}/level")
    @Operation(summary = "调整学生的心理健康风险等级")
    @DataPermission(enable = false)
    public CommonResult<Boolean> adjustStudentLevel(
            @PathVariable("studentProfileId") Long studentProfileId,
            @Valid @RequestBody StudentLevelAdjustReqVO adjustReqVO) {
        interventionService.adjustStudentLevel(studentProfileId, adjustReqVO);
        return success(true);
    }

    // ========== 危机事件管理 ==========

    @PostMapping("/event/create")
    @Operation(summary = "上报危机事件")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:create')")
    @DataPermission(enable = false)
    public CommonResult<Long> createCrisisEvent(@Valid @RequestBody CrisisEventCreateReqVO createReqVO) {
        return success(interventionService.createCrisisEvent(createReqVO));
    }

    @GetMapping("/event/page")
    @Operation(summary = "获取危机事件分页")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:query')")
    @DataPermission(enable = false)
    public CommonResult<PageResult<CrisisEventRespVO>> getCrisisEventPage(@Valid CrisisEventPageReqVO pageReqVO) {
        return success(interventionService.getCrisisEventPage(pageReqVO));
    }

    @GetMapping("/event/statistics")
    @Operation(summary = "获取危机事件统计")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:query')")
    @DataPermission(enable = false)
    public CommonResult<Map<String, Long>> getCrisisEventStatistics() {
        return success(interventionService.getCrisisEventStatistics());
    }

    @GetMapping("/event/{id}")
    @Operation(summary = "获取危机事件详情")
    @Parameter(name = "id", description = "事件ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:intervention:query')")
    @DataPermission(enable = false)
    public CommonResult<CrisisEventRespVO> getCrisisEvent(@PathVariable("id") Long id) {
        return success(interventionService.getCrisisEvent(id));
    }

    @PutMapping("/event/{id}/assign")
    @Operation(summary = "分配事件负责人")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> assignHandler(
            @PathVariable("id") Long id,
            @Valid @RequestBody CrisisEventAssignReqVO assignReqVO) {
        interventionService.assignHandler(id, assignReqVO);
        return success(true);
    }

    @PutMapping("/event/{id}/reassign")
    @Operation(summary = "更改事件负责人")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> reassignHandler(
            @PathVariable("id") Long id,
            @Valid @RequestBody CrisisEventReassignReqVO reassignReqVO) {
        interventionService.reassignHandler(id, reassignReqVO);
        return success(true);
    }

    @PutMapping("/event/{id}/process")
    @Operation(summary = "选择事件处理方式")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> processCrisisEvent(
            @PathVariable("id") Long id,
            @Valid @RequestBody CrisisEventProcessReqVO processReqVO) {
        interventionService.processCrisisEvent(id, processReqVO);
        return success(true);
    }

    @PutMapping("/event/{id}/close")
    @Operation(summary = "结案危机事件")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> closeCrisisEvent(
            @PathVariable("id") Long id,
            @Valid @RequestBody CrisisEventCloseReqVO closeReqVO) {
        interventionService.closeCrisisEvent(id, closeReqVO);
        return success(true);
    }

    @PostMapping("/event/{id}/stage-assessment")
    @Operation(summary = "提交阶段性评估")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> submitStageAssessment(
            @PathVariable("id") Long id,
            @Valid @RequestBody CrisisEventAssessmentReqVO assessmentReqVO) {
        interventionService.submitStageAssessment(id, assessmentReqVO);
        return success(true);
    }

    @GetMapping("/event/{id}/process-history")
    @Operation(summary = "获取事件处理历史")
    @DataPermission(enable = false)
    public CommonResult<PageResult<CrisisEventProcessHistoryVO>> getProcessHistory(
            @PathVariable("id") Long id,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return success(interventionService.getProcessHistory(id, pageNo, pageSize));
    }

    @GetMapping("/event/check-duplicate")
    @Operation(summary = "检测重复上报")
    @Parameter(name = "studentProfileId", description = "学生档案ID", required = true)
    @DataPermission(enable = false)
    public CommonResult<Boolean> checkDuplicateEvent(@RequestParam("studentProfileId") Long studentProfileId) {
        return success(interventionService.checkDuplicateEvent(studentProfileId));
    }

    // ========== 系统设置 ==========

    @GetMapping("/admin/settings/intervention-assignment-mode")
    @Operation(summary = "获取危机事件分配模式")
    @PreAuthorize("@ss.hasPermission('psychology:settings:query')")
    public CommonResult<InterventionAssignmentSettingVO> getAssignmentMode() {
        return success(interventionService.getAssignmentSettings());
    }

    @PutMapping("/admin/settings/intervention-assignment-mode")
    @Operation(summary = "设置危机事件分配模式")
    @PreAuthorize("@ss.hasPermission('psychology:settings:update')")
    public CommonResult<Boolean> setAssignmentMode(@Valid @RequestBody InterventionAssignmentSettingVO settingVO) {
        interventionService.setAssignmentSettings(settingVO);
        return success(true);
    }

    @PutMapping("/event/{id}/description")
    @Operation(summary = "更新危机事件描述")
    @PreAuthorize("@ss.hasPermission('psychology:intervention:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateCrisisEventDescription(
            @PathVariable("id") Long id,
            @Valid @RequestBody CrisisEventUpdateDescriptionReqVO updateReqVO) {
        interventionService.updateCrisisEventDescription(id, updateReqVO.getDescription());
        return success(true);
    }
}