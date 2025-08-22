package cn.iocoder.yudao.module.psychology.controller.admin.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultExtensionService;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultStatisticsService;
import cn.iocoder.yudao.module.psychology.service.assessment.CombinedAssessmentResultGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Tag(name = "管理后台 - 测评结果管理扩展")
@RestController
@RequestMapping("/psychology/assessment-result-extension")
@Validated
@Slf4j
public class AssessmentResultExtensionController {

    @Resource
    private AssessmentResultExtensionService assessmentResultExtensionService;

    @Resource
    private AssessmentResultStatisticsService assessmentResultStatisticsService;

    @Resource
    private CombinedAssessmentResultGenerationService combinedAssessmentResultGenerationService;

    // 组合测评结果生成相关接口

    @PostMapping("/generate-combined-result")
    @Operation(summary = "生成组合测评结果")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:generate')")
    public CommonResult<Long> generateCombinedAssessmentResult(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam("studentProfileId") Long studentProfileId,
            @RequestBody List<Long> questionnaireResultIds) {
        
        Long resultId = assessmentResultExtensionService.generateCombinedAssessmentResult(
                assessmentId, studentProfileId, questionnaireResultIds);
        
        return success(resultId);
    }

    @GetMapping("/completion-status")
    @Operation(summary = "检查测评完成状态")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<AssessmentResultExtensionService.AssessmentCompletionStatus> checkCompletionStatus(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam("studentProfileId") Long studentProfileId) {
        
        AssessmentResultExtensionService.AssessmentCompletionStatus status = 
                assessmentResultExtensionService.checkAssessmentCompletionStatus(assessmentId, studentProfileId);
        
        return success(status);
    }

    @GetMapping("/user-records")
    @Operation(summary = "获取用户测评记录")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<PageResult<Object>> getUserAssessmentRecords(
            @RequestParam("studentProfileId") Long studentProfileId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        // TODO: 创建分页请求VO
        Object pageReqVO = new Object();
        
        PageResult<Object> pageResult = assessmentResultExtensionService.getUserAssessmentRecords(studentProfileId, pageReqVO);
        
        return success(pageResult);
    }

    @GetMapping("/history-comparison")
    @Operation(summary = "获取历史测评结果对比")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<List<AssessmentResultExtensionService.AssessmentResultComparison>> getHistoryComparison(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam("studentProfileId") Long studentProfileId,
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        
        List<AssessmentResultExtensionService.AssessmentResultComparison> comparisons = 
                assessmentResultExtensionService.getHistoryAssessmentComparison(assessmentId, studentProfileId, limit);
        
        return success(comparisons);
    }

    @GetMapping("/trend-analysis")
    @Operation(summary = "获取测评结果趋势分析")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<AssessmentResultExtensionService.AssessmentTrendAnalysis> getTrendAnalysis(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam("studentProfileId") Long studentProfileId,
            @RequestParam("startTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        
        AssessmentResultExtensionService.AssessmentTrendAnalysis analysis = 
                assessmentResultExtensionService.getAssessmentTrendAnalysis(assessmentId, studentProfileId, startTime, endTime);
        
        return success(analysis);
    }

    @PostMapping("/batch-generate")
    @Operation(summary = "批量生成测评结果")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:generate')")
    public CommonResult<AssessmentResultExtensionService.BatchAssessmentGenerationResult> batchGenerateResults(
            @RequestBody Object batchGenerationReqVO) {
        
        AssessmentResultExtensionService.BatchAssessmentGenerationResult result = 
                assessmentResultExtensionService.batchGenerateAssessmentResults(batchGenerationReqVO);
        
        return success(result);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取测评结果统计")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<AssessmentResultExtensionService.AssessmentResultStatistics> getAssessmentStatistics(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam(value = "timeRange", defaultValue = "30") Integer timeRange) {
        
        AssessmentResultExtensionService.AssessmentResultStatistics statistics = 
                assessmentResultExtensionService.getAssessmentResultStatistics(assessmentId, timeRange);
        
        return success(statistics);
    }

    // 统计分析相关接口

    @GetMapping("/detailed-trend-analysis")
    @Operation(summary = "获取详细趋势分析")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<AssessmentResultStatisticsService.TrendAnalysisResult> getDetailedTrendAnalysis(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam("startTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        
        AssessmentResultStatisticsService.TrendAnalysisResult result = 
                assessmentResultStatisticsService.getAssessmentTrendAnalysis(assessmentId, startTime, endTime);
        
        return success(result);
    }

    @GetMapping("/risk-distribution")
    @Operation(summary = "获取风险等级分布统计")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<AssessmentResultStatisticsService.RiskLevelDistribution> getRiskDistribution(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam(value = "timeRange", defaultValue = "30") Integer timeRange) {
        
        AssessmentResultStatisticsService.RiskLevelDistribution distribution = 
                assessmentResultStatisticsService.getRiskLevelDistribution(assessmentId, timeRange);
        
        return success(distribution);
    }

    @PostMapping("/comparison-analysis")
    @Operation(summary = "获取测评对比分析")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<AssessmentResultStatisticsService.ComparisonAnalysisResult> getComparisonAnalysis(
            @RequestBody List<Long> assessmentIds,
            @RequestParam(value = "timeRange", defaultValue = "30") Integer timeRange) {
        
        AssessmentResultStatisticsService.ComparisonAnalysisResult result = 
                assessmentResultStatisticsService.getAssessmentComparisonAnalysis(assessmentIds, timeRange);
        
        return success(result);
    }

    @GetMapping("/effectiveness-evaluation")
    @Operation(summary = "获取测评效果评估")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<AssessmentResultStatisticsService.EffectivenessEvaluation> getEffectivenessEvaluation(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam("studentProfileId") Long studentProfileId,
            @RequestParam(value = "evaluationPeriod", defaultValue = "30") Integer evaluationPeriod) {
        
        AssessmentResultStatisticsService.EffectivenessEvaluation evaluation = 
                assessmentResultStatisticsService.getAssessmentEffectivenessEvaluation(assessmentId, studentProfileId, evaluationPeriod);
        
        return success(evaluation);
    }

    @GetMapping("/visualization-data")
    @Operation(summary = "获取可视化数据")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<AssessmentResultStatisticsService.VisualizationData> getVisualizationData(
            @RequestParam(value = "assessmentId", required = false) Long assessmentId,
            @RequestParam(value = "chartType", defaultValue = "all") String chartType,
            @RequestParam(value = "timeRange", defaultValue = "30") Integer timeRange) {
        
        // TODO: 创建可视化请求VO
        Object visualizationReqVO = new Object();
        
        AssessmentResultStatisticsService.VisualizationData data = 
                assessmentResultStatisticsService.getAssessmentVisualizationData(visualizationReqVO);
        
        return success(data);
    }

    @PostMapping("/generate-report")
    @Operation(summary = "生成统计报告")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:export')")
    public CommonResult<AssessmentResultStatisticsService.StatisticalReport> generateStatisticalReport(
            @RequestBody Object reportReqVO) {
        
        AssessmentResultStatisticsService.StatisticalReport report = 
                assessmentResultStatisticsService.generateStatisticalReport(reportReqVO);
        
        return success(report);
    }

    // 组合测评结果生成详细接口

    @PostMapping("/detailed-combined-generation")
    @Operation(summary = "详细组合测评结果生成")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:generate')")
    public CommonResult<CombinedAssessmentResultGenerationService.CombinedAssessmentResult> generateDetailedCombinedResult(
            @RequestParam("assessmentId") Long assessmentId,
            @RequestParam("studentProfileId") Long studentProfileId,
            @RequestBody List<Long> questionnaireResultIds) {
        
        try {
            // 获取问卷结果
            List<cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO> questionnaireResults = 
                    getQuestionnaireResults(questionnaireResultIds);
            
            // 生成组合测评结果
            CombinedAssessmentResultGenerationService.CombinedAssessmentResult result = 
                    combinedAssessmentResultGenerationService.generateCombinedResult(assessmentId, studentProfileId, questionnaireResults);
            
            return success(result);
            
        } catch (Exception e) {
            log.error("生成详细组合测评结果失败", e);
            throw new RuntimeException("生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/aggregate-results")
    @Operation(summary = "聚合问卷结果")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:generate')")
    public CommonResult<CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult> aggregateResults(
            @RequestBody List<Long> questionnaireResultIds) {
        
        try {
            List<cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO> questionnaireResults = 
                    getQuestionnaireResults(questionnaireResultIds);
            
            CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult result = 
                    combinedAssessmentResultGenerationService.aggregateQuestionnaireResults(questionnaireResults);
            
            return success(result);
            
        } catch (Exception e) {
            log.error("聚合问卷结果失败", e);
            throw new RuntimeException("聚合失败: " + e.getMessage());
        }
    }

    @PostMapping("/comprehensive-analysis")
    @Operation(summary = "综合分析")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult> performComprehensiveAnalysis(
            @RequestBody CombinedAssessmentResultGenerationService.AggregatedQuestionnaireResult aggregatedResult) {
        
        CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult result = 
                combinedAssessmentResultGenerationService.performComprehensiveAnalysis(aggregatedResult);
        
        return success(result);
    }

    @PostMapping("/risk-assessment")
    @Operation(summary = "风险评估")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<CombinedAssessmentResultGenerationService.RiskAssessmentResult> assessRisk(
            @RequestBody CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult analysisResult) {
        
        CombinedAssessmentResultGenerationService.RiskAssessmentResult result = 
                combinedAssessmentResultGenerationService.assessRisk(analysisResult);
        
        return success(result);
    }

    @PostMapping("/intervention-recommendations")
    @Operation(summary = "生成干预建议")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:query')")
    public CommonResult<CombinedAssessmentResultGenerationService.InterventionRecommendations> generateInterventionRecommendations(
            @RequestParam("riskAssessment") String riskAssessmentJson,
            @RequestParam("analysisResult") String analysisResultJson) {
        
        try {
            // TODO: 解析JSON参数
            CombinedAssessmentResultGenerationService.RiskAssessmentResult riskAssessment = 
                    new CombinedAssessmentResultGenerationService.RiskAssessmentResult();
            CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult analysisResult = 
                    new CombinedAssessmentResultGenerationService.ComprehensiveAnalysisResult();
            
            CombinedAssessmentResultGenerationService.InterventionRecommendations recommendations = 
                    combinedAssessmentResultGenerationService.generateInterventionRecommendations(riskAssessment, analysisResult);
            
            return success(recommendations);
            
        } catch (Exception e) {
            log.error("生成干预建议失败", e);
            throw new RuntimeException("生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/comprehensive-report")
    @Operation(summary = "生成综合报告")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-result:export')")
    public CommonResult<String> generateComprehensiveReport(
            @RequestBody CombinedAssessmentResultGenerationService.CombinedAssessmentResult combinedResult) {
        
        String report = combinedAssessmentResultGenerationService.generateComprehensiveReport(combinedResult);
        
        return success(report);
    }

    // 辅助方法

    private List<cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO> getQuestionnaireResults(List<Long> questionnaireResultIds) {
        // TODO: 实现获取问卷结果的逻辑
        // 这里返回模拟数据
        return questionnaireResultIds.stream()
                .map(this::createMockQuestionnaireResult)
                .collect(java.util.stream.Collectors.toList());
    }

    private cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO createMockQuestionnaireResult(Long id) {
        cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO result = 
                new cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO();
        result.setId(id);
        result.setQuestionnaireId(id);
        result.setUserId(100L);
        result.setAnswers("{\"q1\":\"A\",\"q2\":\"B\"}");
        result.setStandardScore(java.math.BigDecimal.valueOf(85.0));
        result.setDimensionScores("{\"anxiety\":80,\"depression\":90}");
        result.setRiskLevel(2);
        result.setReportContent("测试结果内容");
        result.setSuggestions("测试建议");
        result.setGenerationStatus(2);
        result.setCreateTime(LocalDateTime.now());
        result.setGenerationTime(LocalDateTime.now());
        return result;
    }

}