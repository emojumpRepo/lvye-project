package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigSaveReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigRespVO;
import cn.iocoder.yudao.module.psychology.convert.questionnaire.QuestionnaireResultConfigConvert;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 问卷结果配置")
@RestController
@RequestMapping("/psychology/questionnaire-result-config")
@Validated
public class QuestionnaireResultConfigController {

    @Resource
    private QuestionnaireResultConfigService questionnaireResultConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建问卷结果配置")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result-config:create')")
    public CommonResult<Long> createQuestionnaireResultConfig(@Valid @RequestBody QuestionnaireResultConfigSaveReqVO createReqVO) {
        return success(questionnaireResultConfigService.createQuestionnaireResultConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新问卷结果配置")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result-config:update')")
    public CommonResult<Boolean> updateQuestionnaireResultConfig(@Valid @RequestBody QuestionnaireResultConfigSaveReqVO updateReqVO) {
        questionnaireResultConfigService.updateQuestionnaireResultConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除问卷结果配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result-config:delete')")
    public CommonResult<Boolean> deleteQuestionnaireResultConfig(@RequestParam("id") Long id) {
        questionnaireResultConfigService.deleteQuestionnaireResultConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得问卷结果配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result-config:query')")
    public CommonResult<QuestionnaireResultConfigRespVO> getQuestionnaireResultConfig(@RequestParam("id") Long id) {
        QuestionnaireResultConfigRespVO questionnaireResultConfig = questionnaireResultConfigService.getQuestionnaireResultConfig(id);
        return success(questionnaireResultConfig);
    }

    @GetMapping("/page")
    @Operation(summary = "获得问卷结果配置分页")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result-config:query')")
    public CommonResult<PageResult<QuestionnaireResultConfigRespVO>> getQuestionnaireResultConfigPage(@Valid QuestionnaireResultConfigPageReqVO pageVO) {
        PageResult<QuestionnaireResultConfigRespVO> pageResult = questionnaireResultConfigService.getQuestionnaireResultConfigPage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-questionnaire")
    @Operation(summary = "根据问卷ID获取结果配置分页列表")
    @Parameter(name = "questionnaireId", description = "问卷ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result-config:query')")
    public CommonResult<PageResult<QuestionnaireResultConfigRespVO>> getQuestionnaireResultConfigPageByQuestionnaireId(
            @RequestParam("questionnaireId") Long questionnaireId,
            @Valid QuestionnaireResultConfigPageReqVO pageReqVO) {
        pageReqVO.setQuestionnaireId(questionnaireId);
        PageResult<QuestionnaireResultConfigRespVO> pageResult = questionnaireResultConfigService.getQuestionnaireResultConfigPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get-by-questionnaire-and-dimension")
    @Operation(summary = "根据问卷ID和维度名称获取结果配置")
    @Parameter(name = "questionnaireId", description = "问卷ID", required = true, example = "1024")
    @Parameter(name = "dimensionName", description = "维度名称", required = true, example = "睡眠质量")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result-config:query')")
    public CommonResult<QuestionnaireResultConfigRespVO> getQuestionnaireResultConfigByQuestionnaireIdAndDimensionName(
            @RequestParam("questionnaireId") Long questionnaireId,
            @RequestParam("dimensionName") String dimensionName) {
        QuestionnaireResultConfigRespVO config = QuestionnaireResultConfigConvert.INSTANCE.convert(
                questionnaireResultConfigService.getQuestionnaireResultConfigByQuestionnaireIdAndDimensionName(questionnaireId, dimensionName));
        return success(config);
    }

    @DeleteMapping("/delete-by-questionnaire")
    @Operation(summary = "根据问卷ID删除结果配置")
    @Parameter(name = "questionnaireId", description = "问卷ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result-config:delete')")
    public CommonResult<Boolean> deleteQuestionnaireResultConfigByQuestionnaireId(@RequestParam("questionnaireId") Long questionnaireId) {
        questionnaireResultConfigService.deleteQuestionnaireResultConfigByQuestionnaireId(questionnaireId);
        return success(true);
    }

}
