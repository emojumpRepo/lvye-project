package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateCreateReqVO;
import cn.iocoder.yudao.module.psychology.service.intervention.InterventionTemplateService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


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

    @GetMapping("/get-template-list")
    @Operation(summary = "查询所有模板和步骤")
    @PreAuthorize("@ss.hasPermission('psychology:intervention-template:query')")
    public CommonResult<List<InterventionTemplateCreateReqVO>> getTemplateList() {
        List<InterventionTemplateCreateReqVO> templateList = templateService.getTemplateList();
        return success(templateList);
    }

}
