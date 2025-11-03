package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanCreateReqVO;
import cn.iocoder.yudao.module.psychology.service.interventionplan.InterventionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
