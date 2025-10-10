package cn.iocoder.yudao.module.psychology.controller.admin.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultConfigDO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 测评结果配置")
@RestController
@RequestMapping("/psychology/assessment-result-config")
@Validated
public class AssessmentResultConfigController {

    @Resource
    private AssessmentResultConfigService assessmentResultConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建测评结果配置")
    public CommonResult<Long> create(@RequestBody AssessmentResultConfigDO req) {
        Long id = assessmentResultConfigService.createAssessmentResultConfig(req);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新测评结果配置")
    public CommonResult<Boolean> update(@RequestBody AssessmentResultConfigDO req) {
        assessmentResultConfigService.updateAssessmentResultConfig(req);
        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除测评结果配置")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        assessmentResultConfigService.deleteAssessmentResultConfig(id);
        return success(true);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取测评结果配置详情")
    public CommonResult<AssessmentResultConfigDO> get(@PathVariable Long id) {
        return success(assessmentResultConfigService.getAssessmentResultConfig(id));
    }

    @GetMapping("/list-by-scenario/{scenarioId}")
    @Operation(summary = "根据场景ID获取配置列表")
    public CommonResult<List<AssessmentResultConfigDO>> listByScenario(@PathVariable Long scenarioId) {
        return success(assessmentResultConfigService.getAssessmentResultConfigsByScenarioId(scenarioId));
    }

    @GetMapping("/list-by-scenario-and-type")
    @Operation(summary = "根据场景ID和规则类型获取配置列表")
    public CommonResult<List<AssessmentResultConfigDO>> listByScenarioAndType(@RequestParam Long scenarioId,
                                                                              @RequestParam Integer ruleType) {
        return success(assessmentResultConfigService.getAssessmentResultConfigsByScenarioIdAndRuleType(scenarioId, ruleType));
    }
}


