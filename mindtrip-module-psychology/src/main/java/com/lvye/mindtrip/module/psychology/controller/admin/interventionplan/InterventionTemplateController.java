package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan;

import com.lvye.mindtrip.framework.common.pojo.CommonResult;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateCreateReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateSimpleRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateUpdateReqVO;
import com.lvye.mindtrip.module.psychology.service.interventionplan.InterventionTemplateService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.lvye.mindtrip.framework.common.pojo.CommonResult.success;


@Tag(name = "管理后台 - 干预计划")
@RestController
@RequestMapping("/psychology/intervention-template")
@Validated
@Slf4j
public class InterventionTemplateController {

    @Resource
    private InterventionTemplateService templateService;

    @PostMapping("/create")
    @Operation(summary = "创建干预模板")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-template:create')")
    public CommonResult<Long> createTemplate(@Valid @RequestBody InterventionTemplateCreateReqVO createReqVO) {
        Long templateId = templateService.createTemplate(createReqVO);
        return success(templateId);
    }

    @PutMapping("/update")
    @Operation(summary = "更新干预模板")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-template:update')")
    public CommonResult<Boolean> updateTemplate(@Valid @RequestBody InterventionTemplateUpdateReqVO updateReqVO) {
        templateService.updateTemplate(updateReqVO);
        return success(true);
    }

    @GetMapping("/get-template-list")
    @Operation(summary = "查询所有模板")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-template:query')")
    public CommonResult<List<InterventionTemplateRespVO>> getTemplateList() {
        List<InterventionTemplateRespVO> templateList = templateService.getTemplateList();
        return success(templateList);
    }

    @GetMapping("/get")
    @Operation(summary = "获取干预模板详情")
    @Parameter(name = "id", description = "模板ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-template:query')")
    public CommonResult<InterventionTemplateRespVO> getTemplate(@RequestParam("id") Long id) {
        InterventionTemplateRespVO template = templateService.getTemplateById(id);
        return success(template);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除干预模板")
    @Parameter(name = "id", description = "模板ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-template:delete')")
    public CommonResult<Boolean> deleteTemplate(@RequestParam("id") Long id) {
        templateService.deleteTemplate(id);
        return success(true);
    }

}
