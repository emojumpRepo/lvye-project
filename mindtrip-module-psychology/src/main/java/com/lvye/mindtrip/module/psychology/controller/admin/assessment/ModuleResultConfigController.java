package com.lvye.mindtrip.module.psychology.controller.admin.assessment;

import com.lvye.mindtrip.framework.common.pojo.CommonResult;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.resultconfig.ModuleResultConfigRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.resultconfig.ModuleResultConfigSaveReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;
import com.lvye.mindtrip.module.psychology.service.assessment.ModuleResultConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lvye.mindtrip.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 模块结果配置")
@RestController
@RequestMapping("/psychology/module-result-config")
@Validated
public class ModuleResultConfigController {

    @Resource
    private ModuleResultConfigService moduleResultConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建模块结果配置")
    public CommonResult<Long> create(@RequestBody ModuleResultConfigSaveReqVO req) {
        Long id = moduleResultConfigService.create(req);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新模块结果配置")
    public CommonResult<Boolean> update(@RequestBody ModuleResultConfigSaveReqVO req) {
        moduleResultConfigService.update(req);
        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除模块结果配置")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        moduleResultConfigService.delete(id);
        return success(true);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模块结果配置详情")
    public CommonResult<ModuleResultConfigRespVO> get(@PathVariable Long id) {
        return success(moduleResultConfigService.get(id));
    }

    @GetMapping("/list-by-slot/{scenarioSlotId}")
    @Operation(summary = "根据场景插槽ID获取配置列表")
    public CommonResult<List<ModuleResultConfigDO>> listBySlot(@PathVariable Long scenarioSlotId) {
        return success(moduleResultConfigService.listBySlot(scenarioSlotId));
    }
}


