package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireDimensionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 问卷维度管理控制器
 */
@Tag(name = "管理后台 - 问卷维度管理")
@RestController
@RequestMapping("/psychology/questionnaire-dimension")
@Validated
@Slf4j
public class QuestionnaireDimensionController {

    @Resource
    private QuestionnaireDimensionService questionnaireDimensionService;

    @PostMapping("/create")
    @Operation(summary = "创建问卷维度")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:create')")
    public CommonResult<Long> createDimension(@Valid @RequestBody QuestionnaireDimensionCreateReqVO createReqVO) {
        Long dimensionId = questionnaireDimensionService.createDimension(createReqVO);
        return CommonResult.success(dimensionId);
    }

    @PutMapping("/update")
    @Operation(summary = "更新问卷维度")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:update')")
    public CommonResult<Boolean> updateDimension(@Valid @RequestBody QuestionnaireDimensionUpdateReqVO updateReqVO) {
        questionnaireDimensionService.updateDimension(updateReqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除问卷维度")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:delete')")
    public CommonResult<Boolean> deleteDimension(@RequestParam("id") Long id) {
        questionnaireDimensionService.deleteDimension(id);
        return CommonResult.success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得问卷维度")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:query')")
    public CommonResult<QuestionnaireDimensionRespVO> getDimension(@RequestParam("id") Long id) {
        QuestionnaireDimensionRespVO dimension = questionnaireDimensionService.getDimension(id);
        return CommonResult.success(dimension);
    }

    @GetMapping("/page")
    @Operation(summary = "获得问卷维度分页")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:query')")
    public CommonResult<PageResult<QuestionnaireDimensionRespVO>> getDimensionPage(@Valid QuestionnaireDimensionPageReqVO pageReqVO) {
        PageResult<QuestionnaireDimensionRespVO> pageResult = questionnaireDimensionService.getDimensionPage(pageReqVO);
        return CommonResult.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获得问卷维度列表")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:query')")
    public CommonResult<List<QuestionnaireDimensionRespVO>> getDimensionList() {
        List<QuestionnaireDimensionRespVO> list = questionnaireDimensionService.getDimensionList();
        return CommonResult.success(list);
    }

    @GetMapping("/list-by-questionnaire")
    @Operation(summary = "根据问卷ID获得维度列表")
    @Parameter(name = "questionnaireId", description = "问卷ID", required = true, example = "12")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:query')")
    public CommonResult<List<QuestionnaireDimensionRespVO>> getDimensionListByQuestionnaire(@RequestParam("questionnaireId") Long questionnaireId) {
        List<QuestionnaireDimensionRespVO> list = questionnaireDimensionService.getDimensionListByQuestionnaire(questionnaireId);
        return CommonResult.success(list);
    }

    @PostMapping("/batch-create")
    @Operation(summary = "批量创建问卷维度")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:create')")
    public CommonResult<List<Long>> batchCreateDimensions(@Valid @RequestBody List<QuestionnaireDimensionCreateReqVO> createReqVOList) {
        List<Long> dimensionIds = questionnaireDimensionService.batchCreateDimensions(createReqVOList);
        return CommonResult.success(dimensionIds);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新问卷维度状态")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:update')")
    public CommonResult<Boolean> updateDimensionStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        questionnaireDimensionService.updateDimensionStatus(id, status);
        return CommonResult.success(true);
    }

    @PutMapping("/update-participate-settings")
    @Operation(summary = "批量更新维度参与设置")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-dimension:update')")
    public CommonResult<Boolean> updateParticipateSettings(@Valid @RequestBody QuestionnaireDimensionParticipateUpdateReqVO updateReqVO) {
        questionnaireDimensionService.updateParticipateSettings(updateReqVO);
        return CommonResult.success(true);
    }
}
