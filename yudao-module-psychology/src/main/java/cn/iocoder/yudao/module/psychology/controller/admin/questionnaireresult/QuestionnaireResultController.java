package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.convert.questionnaireresult.QuestionnaireResultConvert;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.AsyncResultGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 问卷结果管理")
@RestController
@RequestMapping("/psychology/questionnaire-result")
@Validated
@Slf4j
public class QuestionnaireResultController {

    @Resource
    private QuestionnaireResultService questionnaireResultService;

    @Resource
    private AsyncResultGenerationService asyncResultGenerationService;

    @PostMapping("/submit-answers")
    @Operation(summary = "提交问卷答案")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:create')")
    public CommonResult<Long> submitQuestionnaireAnswers(@Valid @RequestBody QuestionnaireAnswerSubmitReqVO submitReqVO) {
        Long resultId = questionnaireResultService.submitQuestionnaireAnswers(submitReqVO);
        
        // 更新会话时长
        if (submitReqVO.getAccessId() != null && submitReqVO.getSessionDuration() != null) {
            // TODO: 调用访问服务更新会话时长
        }
        
        return success(resultId);
    }

    @GetMapping("/get")
    @Operation(summary = "获得问卷结果")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<QuestionnaireResultRespVO> getQuestionnaireResult(@RequestParam("id") Long id) {
        QuestionnaireResultDO result = questionnaireResultService.getQuestionnaireResult(id);
        QuestionnaireResultRespVO respVO = QuestionnaireResultConvert.INSTANCE.convert(result);
        if (respVO != null) {
            respVO.setRiskLevelDesc(getRiskLevelDescription(respVO.getRiskLevel()));
            respVO.setGenerationStatusDesc(getGenerationStatusDescription(respVO.getGenerationStatus()));
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得问卷结果分页")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<PageResult<QuestionnaireResultRespVO>> getQuestionnaireResultPage(@Valid QuestionnaireResultPageReqVO pageReqVO) {
        PageResult<QuestionnaireResultDO> pageResult = questionnaireResultService.queryQuestionnaireResults(pageReqVO);
        PageResult<QuestionnaireResultRespVO> respPage = QuestionnaireResultConvert.INSTANCE.convertPage(pageResult);
        if (respPage != null && respPage.getList() != null) {
            for (QuestionnaireResultRespVO item : respPage.getList()) {
                item.setRiskLevelDesc(getRiskLevelDescription(item.getRiskLevel()));
                item.setGenerationStatusDesc(getGenerationStatusDescription(item.getGenerationStatus()));
            }
        }
        return success(respPage);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除问卷结果")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:delete')")
    public CommonResult<Boolean> deleteQuestionnaireResult(@RequestParam("id") Long id) {
        questionnaireResultService.deleteQuestionnaireResult(id);
        return success(true);
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除问卷结果")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:delete')")
    public CommonResult<Boolean> batchDeleteQuestionnaireResults(@RequestBody List<Long> ids) {
        questionnaireResultService.deleteQuestionnaireResults(ids);
        return success(true);
    }

    @PostMapping("/export")
    @Operation(summary = "导出问卷结果")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:export')")
    public ResponseEntity<byte[]> exportQuestionnaireResults(@Valid @RequestBody QuestionnaireResultExportReqVO exportReqVO) {
        try {
            byte[] excelData = questionnaireResultService.exportQuestionnaireResultsToExcel(exportReqVO);
            
            if (excelData == null || excelData.length == 0) {
                log.warn("导出的Excel数据为空");
                return ResponseEntity.noContent().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(excelData.length);
            headers.setContentDispositionFormData("attachment", 
                    "questionnaire_results_" + System.currentTimeMillis() + ".xlsx");
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
                    
        } catch (Exception e) {
            log.error("导出问卷结果失败", e);
            throw new cn.iocoder.yudao.framework.common.exception.ServiceException(
                    cn.iocoder.yudao.module.psychology.enums.QuestionnaireResultErrorCodeConstants.QUESTIONNAIRE_RESULT_GENERATION_FAILED);
        }
    }

    // 结果生成相关接口

    @PostMapping("/generate")
    @Operation(summary = "生成问卷结果")
    @Parameter(name = "id", description = "结果编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:generate')")
    public CommonResult<Boolean> generateQuestionnaireResult(@RequestParam("id") Long id) {
        boolean success = questionnaireResultService.generateResultAsync(id);
        return success(success);
    }

    @PostMapping("/regenerate")
    @Operation(summary = "重新生成问卷结果")
    @Parameter(name = "id", description = "结果编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:generate')")
    public CommonResult<Boolean> regenerateQuestionnaireResult(@RequestParam("id") Long id) {
        boolean success = questionnaireResultService.regenerateResult(id);
        return success(success);
    }

    @PostMapping("/batch-generate")
    @Operation(summary = "批量生成问卷结果")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:generate')")
    public CommonResult<QuestionnaireResultService.BatchGenerationResult> batchGenerateQuestionnaireResults(@RequestBody List<Long> ids) {
        QuestionnaireResultService.BatchGenerationResult result = questionnaireResultService.batchGenerateResults(ids);
        return success(result);
    }

    @GetMapping("/generation-status")
    @Operation(summary = "获取结果生成状态")
    @Parameter(name = "id", description = "结果编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<QuestionnaireResultService.ResultGenerationStatus> getGenerationStatus(@RequestParam("id") Long id) {
        QuestionnaireResultService.ResultGenerationStatus status = questionnaireResultService.getResultGenerationStatus(id);
        return success(status);
    }

    // 统计分析相关接口

    @GetMapping("/statistics")
    @Operation(summary = "获取问卷结果统计分析")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<QuestionnaireResultService.QuestionnaireResultAnalysis> getQuestionnaireResultAnalysis(
            @RequestParam(value = "questionnaireId", required = false) Long questionnaireId,
            @RequestParam(value = "timeRange", defaultValue = "30") Integer timeRange) {
        
        // TODO: 创建分析请求VO
        Object analysisReqVO = new Object();
        QuestionnaireResultService.QuestionnaireResultAnalysis analysis = 
                questionnaireResultService.getQuestionnaireResultAnalysis(analysisReqVO);
        
        return success(analysis);
    }

    @GetMapping("/completion-stats")
    @Operation(summary = "获取问卷完成统计")
    @Parameter(name = "questionnaireId", description = "问卷ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<QuestionnaireResultService.QuestionnaireCompletionStats> getCompletionStats(
            @RequestParam("questionnaireId") Long questionnaireId) {
        
        QuestionnaireResultService.QuestionnaireCompletionStats stats = 
                questionnaireResultService.getQuestionnaireCompletionStats(questionnaireId);
        
        return success(stats);
    }

    @GetMapping("/supportability-check")
    @Operation(summary = "检查问卷支持性")
    @Parameter(name = "questionnaireId", description = "问卷ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<QuestionnaireResultService.QuestionnaireSupportabilityResult> checkSupportability(
            @RequestParam("questionnaireId") Long questionnaireId) {
        
        QuestionnaireResultService.QuestionnaireSupportabilityResult result = 
                questionnaireResultService.checkQuestionnaireSupportability(questionnaireId);
        
        return success(result);
    }

    // 用户相关查询接口

    @GetMapping("/user-results")
    @Operation(summary = "获取用户问卷结果列表")
    @Parameter(name = "studentProfileId", description = "学生档案ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<List<QuestionnaireResultRespVO>> getUserQuestionnaireResults(
            @RequestParam("studentProfileId") Long studentProfileId,
            @RequestParam(value = "questionnaireId", required = false) Long questionnaireId) {
        
        List<QuestionnaireResultDO> results = questionnaireResultService.getUserQuestionnaireResults(studentProfileId, questionnaireId);
        List<QuestionnaireResultRespVO> list = QuestionnaireResultConvert.INSTANCE.convertList(results);
        if (list != null) {
            for (QuestionnaireResultRespVO item : list) {
                item.setRiskLevelDesc(getRiskLevelDescription(item.getRiskLevel()));
                item.setGenerationStatusDesc(getGenerationStatusDescription(item.getGenerationStatus()));
            }
        }
        return success(list);
    }

    @GetMapping("/latest-user-result")
    @Operation(summary = "获取用户最新问卷结果")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<QuestionnaireResultRespVO> getLatestUserResult(
            @RequestParam("questionnaireId") Long questionnaireId,
            @RequestParam("studentProfileId") Long studentProfileId) {
        
        QuestionnaireResultDO result = questionnaireResultService.getLatestUserQuestionnaireResult(questionnaireId, studentProfileId);
        return success(QuestionnaireResultConvert.INSTANCE.convert(result));
    }

    @GetMapping("/questionnaire-results")
    @Operation(summary = "获取问卷的所有结果")
    @Parameter(name = "questionnaireId", description = "问卷ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<List<QuestionnaireResultRespVO>> getQuestionnaireResults(
            @RequestParam("questionnaireId") Long questionnaireId) {
        
        List<QuestionnaireResultDO> results = questionnaireResultService.getQuestionnaireResults(questionnaireId);
        List<QuestionnaireResultRespVO> list = QuestionnaireResultConvert.INSTANCE.convertList(results);
        if (list != null) {
            for (QuestionnaireResultRespVO item : list) {
                item.setRiskLevelDesc(getRiskLevelDescription(item.getRiskLevel()));
                item.setGenerationStatusDesc(getGenerationStatusDescription(item.getGenerationStatus()));
            }
        }
        return success(list);
    }

    @GetMapping("/completion-check")
    @Operation(summary = "检查用户是否已完成问卷")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<Boolean> checkUserCompletion(
            @RequestParam("questionnaireId") Long questionnaireId,
            @RequestParam("studentProfileId") Long studentProfileId) {
        
        boolean completed = questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
        return success(completed);
    }

    // 异步任务管理接口

    @GetMapping("/task-status")
    @Operation(summary = "获取异步任务状态")
    @Parameter(name = "resultId", description = "结果ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:query')")
    public CommonResult<AsyncResultGenerationService.TaskStatus> getTaskStatus(@RequestParam("resultId") Long resultId) {
        AsyncResultGenerationService.TaskStatus status = asyncResultGenerationService.checkTaskStatus(resultId);
        return success(status);
    }

    @PostMapping("/cancel-task")
    @Operation(summary = "取消异步任务")
    @Parameter(name = "resultId", description = "结果ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:generate')")
    public CommonResult<Boolean> cancelTask(@RequestParam("resultId") Long resultId) {
        boolean success = asyncResultGenerationService.cancelTask(resultId);
        return success(success);
    }

    @PostMapping("/retry-task")
    @Operation(summary = "重试异步任务")
    @Parameter(name = "resultId", description = "结果ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire-result:generate')")
    public CommonResult<Boolean> retryTask(@RequestParam("resultId") Long resultId) {
        boolean success = asyncResultGenerationService.retryTask(resultId);
        return success(success);
    }

    // 私有辅助方法

    private String getRiskLevelDescription(Integer riskLevel) {
        if (riskLevel == null) return "未知";
        
        switch (riskLevel) {
            case 1: return "低风险";
            case 2: return "中低风险";
            case 3: return "中等风险";
            case 4: return "高风险";
            case 5: return "极高风险";
            default: return "未知";
        }
    }

    private String getGenerationStatusDescription(Integer generationStatus) {
        if (generationStatus == null) return "未知";
        
        switch (generationStatus) {
            case 1: return "待处理";
            case 2: return "生成中";
            case 3: return "已完成";
            case 4: return "失败";
            default: return "未知";
        }
    }

}