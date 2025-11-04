package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventRemoveRelativeEventReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepBatchUpdateSortReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepCreateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepUpdateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventUpdateRelativeEventsReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventUpdateTitleReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanCreateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanRespVO;
import cn.iocoder.yudao.module.psychology.service.interventionplan.InterventionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理系统- 危机干预计划")
@RestController
@RequestMapping("/psychology/intervention-plan")
@Validated
@Slf4j
public class InterventionPlanController {

    @Resource
    private InterventionPlanService interventionPlanService;

    @PostMapping("/create")
    @Operation(summary = "创建危机干预计划")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:create')")
    public CommonResult<Long> createInterventionPlan(@Valid @RequestBody InterventionPlanCreateReqVO createReqVO) {
        Long eventId = interventionPlanService.createInterventionPlan(createReqVO);
        return success(eventId);
    }

    @GetMapping("/get")
    @Operation(summary = "获取干预计划详情")
    @Parameter(name = "id", description = "干预计划ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:query')")
    public CommonResult<InterventionPlanRespVO> getInterventionPlan(@RequestParam("id") Long id) {
        InterventionPlanRespVO plan = interventionPlanService.getInterventionPlan(id);
        return success(plan);
    }

    @PutMapping("/update-title")
    @Operation(summary = "更新干预事件标题")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:update')")
    public CommonResult<Boolean> updateEventTitle(@Valid @RequestBody InterventionEventUpdateTitleReqVO updateReqVO) {
        interventionPlanService.updateEventTitle(updateReqVO.getId(), updateReqVO.getTitle());
        return success(true);
    }

    @PutMapping("/remove-relative-event")
    @Operation(summary = "移除干预计划的关联事件")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:update')")
    public CommonResult<Boolean> removeRelativeEvent(@Valid @RequestBody InterventionEventRemoveRelativeEventReqVO removeReqVO) {
        interventionPlanService.removeRelativeEvent(removeReqVO.getId(), removeReqVO.getRelativeEventId());
        return success(true);
    }

    @PutMapping("/update-relative-events")
    @Operation(summary = "更新干预计划的关联事件列表")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:update')")
    public CommonResult<Boolean> updateRelativeEvents(@Valid @RequestBody InterventionEventUpdateRelativeEventsReqVO updateReqVO) {
        interventionPlanService.updateRelativeEvents(updateReqVO.getId(), updateReqVO.getRelativeEventIds());
        return success(true);
    }

    @PostMapping("/create-step")
    @Operation(summary = "新增干预事件步骤")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:create')")
    public CommonResult<Long> createEventStep(@Valid @RequestBody InterventionEventStepCreateReqVO createReqVO) {
        Long stepId = interventionPlanService.createEventStep(createReqVO);
        return success(stepId);
    }

    @PutMapping("/update-step")
    @Operation(summary = "更新干预事件步骤")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:update')")
    public CommonResult<Boolean> updateEventStep(@Valid @RequestBody InterventionEventStepUpdateReqVO updateReqVO) {
        interventionPlanService.updateEventStep(updateReqVO);
        return success(true);
    }

    @PutMapping("/batch-update-step-sort")
    @Operation(summary = "批量更新干预事件步骤排序")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:update')")
    public CommonResult<Boolean> batchUpdateStepSort(@Valid @RequestBody InterventionEventStepBatchUpdateSortReqVO updateReqVO) {
        interventionPlanService.batchUpdateStepSort(updateReqVO.getInterventionId(), updateReqVO.getSteps());
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "完成干预事件")
    @Parameter(name = "id", description = "干预事件ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-plan:update')")
    public CommonResult<Boolean> completeInterventionEvent(@RequestParam("id") Long id) {
        interventionPlanService.completeInterventionEvent(id);
        return success(true);
    }

}
