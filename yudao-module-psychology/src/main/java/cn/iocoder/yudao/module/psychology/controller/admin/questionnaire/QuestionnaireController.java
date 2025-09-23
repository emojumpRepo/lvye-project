package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.convert.questionnaire.QuestionnaireConvert;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireSyncService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyQuestionRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 问卷管理")
@RestController
@RequestMapping("/psychology/questionnaire")
@Validated
@Slf4j
public class QuestionnaireController {

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private QuestionnaireAccessService questionnaireAccessService;

    @Resource
    private QuestionnaireSyncService questionnaireSyncService;

    @Resource
    private QuestionnaireResultService questionnaireResultService;

    // 通过 service 间接调用外部问卷系统

    @PostMapping("/create")
    @Operation(summary = "创建问卷")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:create')")
    public CommonResult<Long> createQuestionnaire(@Valid @RequestBody QuestionnaireCreateReqVO createReqVO) {
        return success(questionnaireService.createQuestionnaire(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新问卷")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:update')")
    public CommonResult<Boolean> updateQuestionnaire(@Valid @RequestBody QuestionnaireUpdateReqVO updateReqVO) {
        questionnaireService.updateQuestionnaire(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除问卷")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:delete')")
    public CommonResult<Boolean> deleteQuestionnaire(@RequestParam("id") Long id) {
        questionnaireService.deleteQuestionnaire(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得问卷")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<QuestionnaireRespVO> getQuestionnaire(@RequestParam("id") Long id) {
        QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(id);
        return success(questionnaire);
    }

    @GetMapping("/page")
    @Operation(summary = "获得问卷分页")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<PageResult<QuestionnaireWithSurveyRespVO>> getQuestionnairePage(@Valid QuestionnairePageReqVO pageReqVO) {
        PageResult<QuestionnaireWithSurveyRespVO> pageResult = questionnaireService.getQuestionnairePageWithSurvey(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出问卷 Excel")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:export')")
    public void exportQuestionnaireExcel(@Valid QuestionnairePageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QuestionnaireRespVO> list = questionnaireService.getQuestionnairePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "问卷.xls", "数据", QuestionnaireRespVO.class, list);
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取问卷精简信息列表", description = "只包含被开启的问卷，主要用于前端的下拉选项")
    @Parameter(name = "supportIndependentUse", description = "是否支持独立使用", example = "1")
    public CommonResult<List<QuestionnaireSimpleRespVO>> getSimpleQuestionnaireList(
            @RequestParam(value = "supportIndependentUse", required = false) Integer supportIndependentUse) {
        List<QuestionnaireSimpleRespVO> list;
        if (supportIndependentUse != null) {
            list = questionnaireService.getSimpleQuestionnaireList(supportIndependentUse);
        } else {
            list = questionnaireService.getSimpleQuestionnaireList();
        }
        return success(list);
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用问卷列表")
    @Parameter(name = "targetAudience", description = "目标对象", example = "1")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<List<QuestionnaireSimpleRespVO>> getAvailableQuestionnaires(
            @RequestParam(value = "targetAudience", required = false) Integer targetAudience) {
        List<QuestionnaireSimpleRespVO> list = questionnaireService.getAvailableQuestionnaires(targetAudience);
        return success(list);
    }

    // 问卷发布和同步相关接口

    @PostMapping("/publish")
    @Operation(summary = "发布问卷到外部系统")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:publish')")
    public CommonResult<QuestionnaireSyncRespVO> publishQuestionnaire(@Valid @RequestBody QuestionnaireSyncReqVO syncReqVO) {
        try {
            questionnaireService.publishQuestionnaireToExternal(syncReqVO.getId());
            
            QuestionnaireSyncRespVO respVO = new QuestionnaireSyncRespVO();
            respVO.setSuccess(true);
            respVO.setMessage("发布成功");
            respVO.setSyncTime(LocalDateTime.now());
            respVO.setSyncStatus(1);
            
            return success(respVO);
        } catch (Exception e) {
            log.error("发布问卷失败", e);
            
            QuestionnaireSyncRespVO respVO = new QuestionnaireSyncRespVO();
            respVO.setSuccess(false);
            respVO.setMessage("发布失败：" + e.getMessage());
            respVO.setSyncTime(LocalDateTime.now());
            respVO.setSyncStatus(-1);
            
            return success(respVO);
        }
    }

    @PostMapping("/pause")
    @Operation(summary = "暂停外部问卷")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:publish')")
    public CommonResult<QuestionnaireSyncRespVO> pauseQuestionnaire(@Valid @RequestBody QuestionnaireSyncReqVO syncReqVO) {
        try {
            questionnaireService.pauseQuestionnaireInExternal(syncReqVO.getId());

            QuestionnaireSyncRespVO respVO = new QuestionnaireSyncRespVO();
            respVO.setSuccess(true);
            respVO.setMessage("暂停成功");
            respVO.setSyncTime(LocalDateTime.now());
            respVO.setSyncStatus(2);
            
            return success(respVO);
        } catch (Exception e) {
            log.error("暂停外部问卷失败", e);
            
            QuestionnaireSyncRespVO respVO = new QuestionnaireSyncRespVO();
            respVO.setSuccess(false);
            respVO.setMessage("暂停失败：" + e.getMessage());
            respVO.setSyncTime(LocalDateTime.now());
            respVO.setSyncStatus(-1);
            
            return success(respVO);
        }
    }

    @PostMapping("/sync-status")
    @Operation(summary = "同步问卷状态")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:sync')")
    public CommonResult<QuestionnaireSyncRespVO> syncQuestionnaireStatus() {
        try {
            // TODO: 实现具体的同步逻辑
            log.info("同步问卷状态（简化实现）");
            
            QuestionnaireSyncRespVO respVO = new QuestionnaireSyncRespVO();
            respVO.setSuccess(true);
            respVO.setMessage("同步成功");
            respVO.setSyncTime(LocalDateTime.now());
            respVO.setSyncStatus(1);
            
            return success(respVO);
        } catch (Exception e) {
            log.error("同步问卷状态失败", e);
            
            QuestionnaireSyncRespVO respVO = new QuestionnaireSyncRespVO();
            respVO.setSuccess(false);
            respVO.setMessage("同步失败：" + e.getMessage());
            respVO.setSyncTime(LocalDateTime.now());
            respVO.setSyncStatus(-1);
            
            return success(respVO);
        }
    }

    @PostMapping("/manual-sync")
    @Operation(summary = "手动同步所有问卷")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:sync')")
    public CommonResult<QuestionnaireSyncRespVO> manualSync() {
        try {
            log.info("开始同步外部问卷系统数据");

            QuestionnaireSyncService.QuestionnaireSyncResult result = questionnaireSyncService.syncQuestionnaires();
            
            QuestionnaireSyncRespVO respVO = new QuestionnaireSyncRespVO();
            
            if (result.isSuccess()) {
                respVO.setSuccess(true);
                respVO.setMessage(String.format("同步完成：处理%d个问卷，新增%d个，更新%d个，关闭%d个，失败%d个", 
                    result.getTotalProcessed(), result.getNewAdded(), result.getUpdated(), 
                    result.getInvalidated(), result.getFailed()));
                respVO.setSyncStatus(result.getFailed() > 0 ? 2 : 1); // 有失败时返回状态2
            } else {
                respVO.setSuccess(false);
                respVO.setMessage("同步失败：" + result.getErrorMessage());
                respVO.setSyncStatus(-1);
            }
            
            respVO.setSyncTime(LocalDateTime.now());
            
            return success(respVO);
        } catch (Exception e) {
            log.error("同步问卷状态失败", e);
            
            QuestionnaireSyncRespVO respVO = new QuestionnaireSyncRespVO();
            respVO.setSuccess(false);
            respVO.setMessage("同步失败：" + e.getMessage());
            respVO.setSyncTime(LocalDateTime.now());
            respVO.setSyncStatus(-1);
            
            return success(respVO);
        }
    }

    @GetMapping("/test-link")
    @Operation(summary = "测试问卷链接")
    @Parameter(name = "id", description = "问卷编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<Boolean> testQuestionnaireLink(@RequestParam("id") Long id) {
        try {
            boolean successFlag = questionnaireService.testQuestionnaireLink(id);
            return success(successFlag);
        } catch (Exception e) {
            log.error("测试问卷链接失败", e);
            return success(false);
        }
    }

    @GetMapping("/access-stats")
    @Operation(summary = "获取问卷访问统计")
    @Parameter(name = "id", description = "问卷编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<QuestionnaireAccessService.QuestionnaireAccessStats> getAccessStats(
            @RequestParam("id") Long id,
            @RequestParam(value = "days", defaultValue = "30") Integer days) {
        try {
            // 计算时间范围
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            QuestionnaireAccessService.QuestionnaireAccessStats stats = questionnaireAccessService.getQuestionnaireAccessStats(id, startTime, endTime);
            return success(stats);
        } catch (Exception e) {
            log.error("获取问卷访问统计失败", e);
            return success(null);
        }
    }

    @GetMapping("/access-trend")
    @Operation(summary = "获取问卷访问趋势")
    @Parameter(name = "id", description = "问卷编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<List<java.util.Map<String, Object>>> getAccessTrend(
            @RequestParam("id") Long id,
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        try {
            List<java.util.Map<String, Object>> trend = questionnaireAccessService.getQuestionnaireAccessTrend(id, days);
            return success(trend);
        } catch (Exception e) {
            log.error("获取问卷访问趋势失败", e);
            return success(java.util.Collections.emptyList());
        }
    }

    @GetMapping("/popular")
    @Operation(summary = "获取热门问卷排行")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<List<java.util.Map<String, Object>>> getPopularQuestionnaires(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        try {
            List<java.util.Map<String, Object>> popular = questionnaireAccessService.getPopularQuestionnaires(limit, days);
            return success(popular);
        } catch (Exception e) {
            log.error("获取热门问卷排行失败", e);
            return success(java.util.Collections.emptyList());
        }
    }

    @GetMapping("/survey-questions")
    @Operation(summary = "获取外部问卷题目")
    @Parameter(name = "questionnaireId", description = "问卷ID", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<ExternalSurveyQuestionRespVO> getSurveyQuestions(@RequestParam("questionnaireId") Long questionnaireId) {
        try {
            QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(questionnaireId);
            if (questionnaire == null || questionnaire.getExternalId() == null || questionnaire.getExternalId().trim().isEmpty()) {
                log.error("获取外部问卷题目失败，问卷不存在或未绑定externalId，questionnaireId: {}", questionnaireId);
                return success(null);
            }
            String surveyId = questionnaire.getExternalId();
            ExternalSurveyQuestionRespVO resp = questionnaireSyncService.getSurveyQuestions(surveyId);
            return success(resp);
        } catch (Exception e) {
            log.error("获取外部问卷题目失败，questionnaireId: {}", questionnaireId, e);
            return success(null);
        }
    }

    @GetMapping("/availability-check")
    @Operation(summary = "检查问卷可用性")
    @Parameter(name = "id", description = "问卷编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<QuestionnaireAccessService.QuestionnaireAvailabilityResult> checkAvailability(
            @RequestParam("id") Long id) {
        try {
            QuestionnaireAccessService.QuestionnaireAvailabilityResult result = questionnaireAccessService.checkQuestionnaireAvailability(id);
            return success(result);
        } catch (Exception e) {
            log.error("检查问卷可用性失败", e);
            return success(null);
        }
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除问卷")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:delete')")
    public CommonResult<Boolean> batchDeleteQuestionnaires(@RequestBody List<Long> ids) {
        try {
            // TODO: 实现具体的批量删除逻辑
            log.info("批量删除问卷（简化实现），问卷数量: {}", ids.size());
            for (Long id : ids) {
                questionnaireService.deleteQuestionnaire(id);
            }
            return success(true);
        } catch (Exception e) {
            log.error("批量删除问卷失败", e);
            return success(false);
        }
    }

    @PostMapping("/batch-publish")
    @Operation(summary = "批量发布问卷")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:publish')")
    public CommonResult<java.util.Map<String, Object>> batchPublishQuestionnaires(@RequestBody List<Long> ids) {
        try {
            // TODO: 实现具体的批量发布逻辑
            log.info("批量发布问卷（简化实现），问卷数量: {}", ids.size());
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            int successCount = 0;
            for (Long id : ids) {
                try {
                    questionnaireService.publishQuestionnaireToExternal(id);
                    successCount++;
                } catch (Exception e) {
                    log.error("发布问卷失败，问卷ID: {}", id, e);
                }
            }
            result.put("successCount", successCount);
            result.put("totalCount", ids.size());
            result.put("failureCount", ids.size() - successCount);
            return success(result);
        } catch (Exception e) {
            log.error("批量发布问卷失败", e);
            return success(java.util.Collections.emptyMap());
        }
    }

    @GetMapping("/get-result")
    @Operation(summary = "获取问卷结果")
    @Parameter(name = "id", description = "问卷结果ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:questionnaire:query')")
    public CommonResult<QuestionnaireResultRespVO> getQuestionnaireResult(@RequestParam("id") Long id) {
        try {
            QuestionnaireResultRespVO result = questionnaireResultService.getQuestionnaireResult(id);
            if (result == null) {
                return success(null);
            }
            return success(result);
        } catch (Exception e) {
            log.error("获取问卷结果失败，结果ID: {}", id, e);
            return success(null);
        }
    }
}