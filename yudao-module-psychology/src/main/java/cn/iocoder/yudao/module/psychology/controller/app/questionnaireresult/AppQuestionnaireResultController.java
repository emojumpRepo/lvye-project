package cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireRespVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "学生端 - 问卷结果管理")
@RestController
@RequestMapping("/psychology/app/questionnaire-result")
@Validated
@Slf4j
public class AppQuestionnaireResultController {

    @Resource
    private QuestionnaireResultService questionnaireResultService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private QuestionnaireAccessService questionnaireAccessService;

    @PostMapping("/submit-answers")
    @Operation(summary = "提交问卷答案")
    public CommonResult<AppQuestionnaireAnswerSubmitRespVO> submitQuestionnaireAnswers(
            @Valid @RequestBody AppQuestionnaireAnswerSubmitReqVO submitReqVO,
            HttpServletRequest request) {
        
        try {
            // 验证提交前置条件
            AppQuestionnaireAnswerSubmitRespVO validationResult = validateSubmissionPreconditions(submitReqVO);
            if (validationResult != null) {
                return success(validationResult);
            }
            
            // 获取问卷信息
            QuestionnaireRespVO questionnaire = getAndValidateQuestionnaire(submitReqVO.getQuestionnaireId());
            if (questionnaire == null) {
                return success(createErrorResponse("问卷不存在"));
            }
            
            // 提交答案并生成结果
            Long resultId = questionnaireResultService.submitQuestionnaireAnswers(submitReqVO);
            
            // 构建成功响应
            return success(createSuccessResponse(resultId, questionnaire));
            
        } catch (Exception e) {
            log.error("提交问卷答案失败", e);
            return success(createErrorResponse("提交失败: " + e.getMessage()));
        }
    }

    /**
     * 验证提交前置条件
     */
    private AppQuestionnaireAnswerSubmitRespVO validateSubmissionPreconditions(AppQuestionnaireAnswerSubmitReqVO submitReqVO) {
        // 验证问卷访问权限
        boolean hasAccess = questionnaireAccessService.checkQuestionnaireAccess(
                submitReqVO.getQuestionnaireId(), submitReqVO.getUserId());
        
        if (!hasAccess) {
            return createErrorResponse("无权限提交该问卷答案");
        }
        
        // 检查是否已经提交过
        boolean hasCompleted = questionnaireResultService.hasUserCompletedQuestionnaire(
                submitReqVO.getQuestionnaireId(), submitReqVO.getUserId());
        
        if (hasCompleted) {
            AppQuestionnaireAnswerSubmitRespVO respVO = new AppQuestionnaireAnswerSubmitRespVO();
            respVO.setResultStatus(2); // 已完成
            respVO.setStatusMessage("您已经完成过该问卷，无需重复提交");
            respVO.setNeedWaitResult(false);
            return respVO;
        }
        
        return null; // 验证通过
    }

    /**
     * 获取并验证问卷信息
     * TODO: 考虑添加缓存以提高性能
     */
    private QuestionnaireRespVO getAndValidateQuestionnaire(Long questionnaireId) {
        QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(questionnaireId);
        if (questionnaire == null) {
            log.warn("问卷不存在，ID: {}", questionnaireId);
        }
        return questionnaire;
    }

    /**
     * 创建成功响应
     */
    private AppQuestionnaireAnswerSubmitRespVO createSuccessResponse(Long resultId, QuestionnaireRespVO questionnaire) {
        AppQuestionnaireAnswerSubmitRespVO respVO = new AppQuestionnaireAnswerSubmitRespVO();
        respVO.setResultId(resultId);
        respVO.setQuestionnaireId(questionnaire.getId());
        respVO.setQuestionnaireTitle(questionnaire.getTitle());
        respVO.setSubmitTime(LocalDateTime.now());
        respVO.setResultStatus(1); // 生成中
        respVO.setResultProgress(0);
        respVO.setStatusMessage("答案提交成功，正在生成结果...");
        respVO.setNeedWaitResult(true);
        respVO.setEstimatedCompleteTime(LocalDateTime.now().plusMinutes(5));
        respVO.setResultViewUrl("/psychology/app/questionnaire-result/" + resultId);
        return respVO;
    }

    /**
     * 创建错误响应
     */
    private AppQuestionnaireAnswerSubmitRespVO createErrorResponse(String message) {
        AppQuestionnaireAnswerSubmitRespVO respVO = new AppQuestionnaireAnswerSubmitRespVO();
        respVO.setResultStatus(3); // 生成失败
        respVO.setStatusMessage(message);
        respVO.setNeedWaitResult(false);
        return respVO;
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取问卷结果详情")
    @Parameter(name = "id", description = "结果编号", required = true, example = "2001")
    public CommonResult<AppQuestionnaireResultRespVO> getQuestionnaireResult(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId) {
        
        try {
            // 获取问卷结果
            QuestionnaireResultDO result = questionnaireResultService.getQuestionnaireResult(id);
            if (result == null) {
                return success(null);
            }
            
            // 验证数据权限
            if (!result.getUserId().equals(userId)) {
                log.warn("用户尝试访问非本人的问卷结果，用户ID: {}, 结果ID: {}", userId, id);
                return success(null);
            }
            
            // 获取问卷信息
            QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(result.getQuestionnaireId());

            // 转换为响应VO
            AppQuestionnaireResultRespVO respVO = convertToResultRespVO(result, questionnaire);
            
            return success(respVO);
            
        } catch (Exception e) {
            log.error("获取问卷结果失败，结果ID: {}", id, e);
            return success(null);
        }
    }

    @GetMapping("/progress/{resultId}")
    @Operation(summary = "获取结果生成进度")
    @Parameter(name = "resultId", description = "结果编号", required = true, example = "2001")
    public CommonResult<AppQuestionnaireResultProgressRespVO> getResultGenerationProgress(
            @PathVariable("resultId") Long resultId,
            @RequestParam("studentProfileId") Long studentProfileId) {
        
        try {
            // 获取问卷结果
            QuestionnaireResultDO result = questionnaireResultService.getQuestionnaireResult(resultId);
            if (result == null) {
                return success(null);
            }
            
            // 验证数据权限
            if (!result.getUserId().equals(studentProfileId)) {
                return success(null);
            }
            
            // 获取问卷信息
            QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(result.getQuestionnaireId());
            
            // 构建进度响应（基于生成状态推断）
            AppQuestionnaireResultProgressRespVO progressVO = new AppQuestionnaireResultProgressRespVO();
            progressVO.setResultId(result.getId());
            progressVO.setQuestionnaireId(result.getQuestionnaireId());
            progressVO.setQuestionnaireTitle(questionnaire != null ? questionnaire.getTitle() : "");
            Integer generationStatus = result.getGenerationStatus();
            Integer resultStatus = (generationStatus != null && generationStatus == 2) ? 2
                    : (generationStatus != null && generationStatus == 3) ? 3 : 1;
            progressVO.setResultStatus(resultStatus);
            progressVO.setResultStatusDesc(getResultStatusDesc(resultStatus));
            int progress = (resultStatus == 2) ? 100 : (resultStatus == 1 ? 50 : 0);
            progressVO.setProgress(progress);
            progressVO.setCurrentStep(resultStatus == 2 ? "已完成" : resultStatus == 3 ? "失败" : "生成中");
            progressVO.setStartTime(result.getCreateTime());
            progressVO.setEstimatedCompleteTime(null);
            progressVO.setActualCompleteTime(result.getGenerationTime());
            progressVO.setErrorMessage(result.getGenerationError());
            progressVO.setIsCompleted(resultStatus != 1);
            progressVO.setIsSuccess(resultStatus == 2);
            progressVO.setResultViewUrl("/psychology/app/questionnaire-result/" + result.getId());
            
            return success(progressVO);
            
        } catch (Exception e) {
            log.error("获取结果生成进度失败，结果ID: {}", resultId, e);
            return success(null);
        }
    }

    @GetMapping("/my-results")
    @Operation(summary = "获取我的问卷结果列表")
    public CommonResult<List<AppQuestionnaireResultRespVO>> getMyQuestionnaireResults(
            @RequestParam("studentProfileId") Long studentProfileId,
            @RequestParam(value = "questionnaireId", required = false) Long questionnaireId,
            @RequestParam(value = "resultStatus", required = false) Integer resultStatus,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        
        try {
            // 构建查询条件
            cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo.QuestionnaireResultPageReqVO pageReqVO =
                    new cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo.QuestionnaireResultPageReqVO();
            pageReqVO.setUserId(studentProfileId);
            pageReqVO.setQuestionnaireId(questionnaireId);
            // 注意：QuestionnaireResultPageReqVO 没有 resultStatus 字段，这里注释掉
            // pageReqVO.setResultStatus(resultStatus);
            pageReqVO.setPageNo(1);
            pageReqVO.setPageSize(limit);
            
            // 查询结果
            PageResult<QuestionnaireResultDO> pageResult = questionnaireResultService.getQuestionnaireResultPage(pageReqVO);
            
            // 批量获取问卷信息以避免N+1查询问题
            List<Long> questionnaireIds = pageResult.getList().stream()
                    .map(QuestionnaireResultDO::getQuestionnaireId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Long, QuestionnaireRespVO> questionnaireMap = questionnaireIds.stream()
                    .collect(Collectors.toMap(
                            id -> id,
                            id -> questionnaireService.getQuestionnaire(id),
                            (existing, replacement) -> existing
                    ));
            
            // 转换为响应VO
            List<AppQuestionnaireResultRespVO> results = pageResult.getList().stream()
                    .map(result -> convertToResultRespVO(result, questionnaireMap.get(result.getQuestionnaireId())))
                    .collect(Collectors.toList());
            
            return success(results);
            
        } catch (Exception e) {
            log.error("获取我的问卷结果列表失败，学生档案ID: {}", studentProfileId, e);
            return success(java.util.Collections.emptyList());
        }
    }

    @GetMapping("/my-statistics")
    @Operation(summary = "获取我的问卷结果统计")
    public CommonResult<java.util.Map<String, Object>> getMyResultStatistics(
            @RequestParam("studentProfileId") Long studentProfileId) {
        
        try {
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            
            // 构建查询条件
            cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo.QuestionnaireResultPageReqVO pageReqVO = 
                    new cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo.QuestionnaireResultPageReqVO();
            pageReqVO.setUserId(studentProfileId);
            pageReqVO.setPageNo(1);
            pageReqVO.setPageSize(1000); // 获取所有结果用于统计
            
            PageResult<QuestionnaireResultDO> pageResult = questionnaireResultService.getQuestionnaireResultPage(pageReqVO);
            List<QuestionnaireResultDO> allResults = pageResult.getList();
            
            // 统计总数
            statistics.put("totalResults", allResults.size());
            
            // 按状态统计
            long completedCount = allResults.stream().mapToLong(r -> r.getGenerationStatus() != null && r.getGenerationStatus() == 2 ? 1 : 0).sum();
            long processingCount = allResults.stream().mapToLong(r -> r.getGenerationStatus() != null && r.getGenerationStatus() == 1 ? 1 : 0).sum();
            long failedCount = allResults.stream().mapToLong(r -> r.getGenerationStatus() != null && r.getGenerationStatus() == 3 ? 1 : 0).sum();
            
            statistics.put("completed", completedCount);
            statistics.put("processing", processingCount);
            statistics.put("failed", failedCount);
            
            // 按风险等级统计
            long lowRiskCount = allResults.stream().mapToLong(r -> r.getRiskLevel() != null && r.getRiskLevel() == 1 ? 1 : 0).sum();
            long mediumRiskCount = allResults.stream().mapToLong(r -> r.getRiskLevel() != null && r.getRiskLevel() == 2 ? 1 : 0).sum();
            long highRiskCount = allResults.stream().mapToLong(r -> r.getRiskLevel() != null && r.getRiskLevel() == 3 ? 1 : 0).sum();
            
            statistics.put("lowRisk", lowRiskCount);
            statistics.put("mediumRisk", mediumRiskCount);
            statistics.put("highRisk", highRiskCount);
            
            // 计算平均标准分
            double avgScore = allResults.stream()
                    .filter(r -> r.getStandardScore() != null)
                    .mapToDouble(r -> r.getStandardScore().doubleValue())
                    .average().orElse(0.0);
            statistics.put("averageStandardScore", avgScore);
            
            // 最近完成时间
            LocalDateTime lastCompleteTime = allResults.stream()
                    .filter(r -> r.getCompletedTime() != null)
                    .map(QuestionnaireResultDO::getCompletedTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            statistics.put("lastCompleteTime", lastCompleteTime);
            
            return success(statistics);
            
        } catch (Exception e) {
            log.error("获取问卷结果统计失败，学生档案ID: {}", studentProfileId, e);
            return success(new java.util.HashMap<>());
        }
    }

    @PostMapping("/retry-generation/{resultId}")
    @Operation(summary = "重试结果生成")
    @Parameter(name = "resultId", description = "结果编号", required = true, example = "2001")
    public CommonResult<Boolean> retryResultGeneration(
            @PathVariable("resultId") Long resultId,
            @RequestParam("studentProfileId") Long studentProfileId) {
        
        try {
            // 获取问卷结果
            QuestionnaireResultDO result = questionnaireResultService.getQuestionnaireResult(resultId);
            if (result == null) {
                return success(false);
            }
            
            // 验证数据权限
            if (!result.getUserId().equals(studentProfileId)) {
                return success(false);
            }
            
            // 只有失败的结果才能重试
            if (result.getGenerationStatus() == null || result.getGenerationStatus() != 3) {
                return success(false);
            }

            // 重试生成结果（简化实现）
            log.info("重试生成结果，结果ID: {}", resultId);
            // TODO: 实现具体的重试逻辑
            
            return success(true);
            
        } catch (Exception e) {
            log.error("重试结果生成失败，结果ID: {}", resultId, e);
            return success(false);
        }
    }

    @GetMapping("/can-retake/{questionnaireId}")
    @Operation(summary = "检查是否可以重新测试")
    @Parameter(name = "questionnaireId", description = "问卷编号", required = true, example = "1024")
    public CommonResult<java.util.Map<String, Object>> checkCanRetake(
            @PathVariable("questionnaireId") Long questionnaireId,
            @RequestParam("studentProfileId") Long studentProfileId) {
        
        try {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            
            // 检查是否已完成
            boolean hasCompleted = questionnaireResultService.hasUserCompletedQuestionnaire(questionnaireId, studentProfileId);
            result.put("hasCompleted", hasCompleted);
            
            if (!hasCompleted) {
                result.put("canRetake", true);
                result.put("reason", "尚未完成测试");
                return success(result);
            }
            
            // 获取最近的结果
            QuestionnaireResultDO latestResult = questionnaireResultService.getLatestUserQuestionnaireResult(questionnaireId, studentProfileId);
            if (latestResult == null) {
                result.put("canRetake", true);
                result.put("reason", "未找到测试记录");
                return success(result);
            }
            
            // 检查重测间隔（假设30天可以重测一次）
            LocalDateTime nextRetakeTime = latestResult.getCompletedTime() != null ?
                    latestResult.getCompletedTime().plusDays(30) :
                    latestResult.getCreateTime().plusDays(30);
            
            boolean canRetake = LocalDateTime.now().isAfter(nextRetakeTime);
            result.put("canRetake", canRetake);
            result.put("nextRetakeTime", nextRetakeTime);
            result.put("reason", canRetake ? "可以重新测试" : "距离上次测试时间过短");
            
            return success(result);
            
        } catch (Exception e) {
            log.error("检查重测权限失败，问卷ID: {}, 学生ID: {}", questionnaireId, studentProfileId, e);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("canRetake", false);
            result.put("reason", "检查失败");
            return success(result);
        }
    }

    // 私有辅助方法

    private AppQuestionnaireResultRespVO convertToResultRespVO(QuestionnaireResultDO result, QuestionnaireRespVO questionnaire) {
        AppQuestionnaireResultRespVO respVO = new AppQuestionnaireResultRespVO();

        respVO.setId(result.getId());
        respVO.setQuestionnaireId(result.getQuestionnaireId());
        respVO.setQuestionnaireTitle(questionnaire != null ? questionnaire.getTitle() : "");
        respVO.setQuestionnaireDescription(questionnaire != null ? questionnaire.getDescription() : "");
        respVO.setUserId(result.getUserId());
        Integer totalScoreInt = result.getRawScore() != null ? result.getRawScore().intValue() : null;
        respVO.setTotalScore(totalScoreInt);
        respVO.setMaxScore(null);
        respVO.setScoreRate(null);
        respVO.setRiskLevel(result.getRiskLevel());
        respVO.setRiskLevelDesc(getRiskLevelDesc(result.getRiskLevel()));
        respVO.setResultInterpretation(result.getReportContent());
        respVO.setSuggestions(result.getSuggestions());
        respVO.setDetailedReport(result.getReportContent());
        Integer generationStatus = result.getGenerationStatus();
        Integer resultStatus = (generationStatus != null && generationStatus == 2) ? 2
                : (generationStatus != null && generationStatus == 3) ? 3 : 1;
        respVO.setResultStatus(resultStatus);
        respVO.setGenerateTime(result.getGenerationTime());
        respVO.setCompleteTime(result.getGenerationTime());
        respVO.setAnswerDuration(null);
        
        // 设置维度得分（如果有的话）
        if (result.getDimensionScores() != null && !result.getDimensionScores().isEmpty()) {
            // 这里需要根据实际的数据结构来解析维度得分
            // 假设维度得分存储为JSON格式
            respVO.setDimensionScores(parseDimensionScores(result.getDimensionScores()));
        }
        
        // 检查是否可以重测
        LocalDateTime nextRetakeTime = result.getCompletedTime() != null ?
                result.getCompletedTime().plusDays(30) :
                result.getCreateTime().plusDays(30);
        respVO.setCanRetake(LocalDateTime.now().isAfter(nextRetakeTime));
        respVO.setNextRetakeTime(nextRetakeTime);
        
        return respVO;
    }

    private List<AppQuestionnaireResultRespVO.DimensionScoreVO> parseDimensionScores(String dimensionScoresJson) {
        if (dimensionScoresJson == null || dimensionScoresJson.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = cn.iocoder.yudao.framework.common.util.json.JsonUtils.parseObject(dimensionScoresJson, java.util.Map.class);
            if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
            return map.entrySet().stream().map(e -> {
                AppQuestionnaireResultRespVO.DimensionScoreVO vo = new AppQuestionnaireResultRespVO.DimensionScoreVO();
                vo.setDimensionCode(e.getKey());
                Object v = e.getValue();
                if (v instanceof Number) {
                    vo.setScore(((Number) v).intValue());
                } else {
                    try {
                        vo.setScore(Integer.parseInt(String.valueOf(v)));
                    } catch (Exception ignored) {
                        vo.setScore(null);
                    }
                }
                return vo;
            }).collect(java.util.stream.Collectors.toList());
        } catch (Exception ex) {
            return java.util.Collections.emptyList();
        }
    }

    private String getRiskLevelDesc(Integer riskLevel) {
        if (riskLevel == null) return "";
        switch (riskLevel) {
            case 1: return "低风险";
            case 2: return "中等风险";
            case 3: return "高风险";
            default: return "未知";
        }
    }

    private String getResultStatusDesc(Integer resultStatus) {
        if (resultStatus == null) return "";
        switch (resultStatus) {
            case 1: return "正在生成结果";
            case 2: return "结果生成完成";
            case 3: return "结果生成失败";
            default: return "未知状态";
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

}