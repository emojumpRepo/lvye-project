package cn.iocoder.yudao.module.psychology.service.resultgenerator.impl;

import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.enums.RiskLevelEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorStrategy;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单问卷结果生成器
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class SingleQuestionnaireResultGenerator implements ResultGeneratorStrategy {

    @Override
    public ResultGeneratorTypeEnum getGeneratorType() {
        return ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE;
    }

    @Override
    public boolean supports(Long questionnaireId, ResultGeneratorTypeEnum type) {
        return ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE.equals(type) 
               && isQuestionnaireSupported(questionnaireId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T generateResult(ResultGenerationContext context) {
        log.info("开始生成单问卷结果，问卷ID: {}", context.getQuestionnaireId());
        
        // 1. 获取问卷配置和答题数据
        QuestionnaireConfigVO config = getQuestionnaireConfig(context.getQuestionnaireId());
        List<AnswerVO> answers = context.getAnswers();
        
        // 2. 计算原始得分
        BigDecimal rawScore = calculateRawScore(answers, config.getScoringRules());
        
        // 3. 计算标准分和百分位
        BigDecimal standardScore = calculateStandardScore(rawScore, config);
        BigDecimal percentileRank = calculatePercentileRank(standardScore, config);
        
        // 4. 确定风险等级
        RiskLevelEnum riskLevel = determineRiskLevel(standardScore, config.getRiskLevelRules());
        
        // 5. 计算各维度得分
        Map<String, BigDecimal> dimensionScores = calculateDimensionScores(answers, config);
        
        // 6. 生成结果报告
        String reportContent = generateReportContent(config.getReportTemplate(), 
                                                   rawScore, standardScore, riskLevel, dimensionScores);
        
        // 7. 生成建议内容
        String suggestions = generateSuggestions(riskLevel, dimensionScores, config);
        
        QuestionnaireResultVO result = QuestionnaireResultVO.builder()
                .questionnaireId(context.getQuestionnaireId())
                .rawScore(rawScore)
                .standardScore(standardScore)
                .percentileRank(percentileRank)
                .riskLevel(riskLevel.getLevel())
                .levelDescription(riskLevel.getDescription())
                .dimensionScores(dimensionScores)
                .reportContent(reportContent)
                .suggestions(suggestions)
                .build();
        
        log.info("单问卷结果生成完成，风险等级: {}", riskLevel.getName());
        return (T) result;
    }

    @Override
    public void validateGenerationParams(ResultGenerationContext context) {
        if (context.getQuestionnaireId() == null) {
            throw new IllegalArgumentException("问卷ID不能为空");
        }
        if (context.getAnswers() == null || context.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("答题数据不能为空");
        }
        if (context.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
    }

    /**
     * 检查问卷是否支持结果生成
     */
    private boolean isQuestionnaireSupported(Long questionnaireId) {
        // TODO: 实现问卷支持性检查逻辑
        return questionnaireId != null && questionnaireId > 0;
    }

    /**
     * 获取问卷配置
     */
    private QuestionnaireConfigVO getQuestionnaireConfig(Long questionnaireId) {
        // TODO: 从数据库或缓存中获取问卷配置
        // 这里返回一个模拟的配置
        QuestionnaireConfigVO config = new QuestionnaireConfigVO();
        config.setQuestionnaireId(questionnaireId);
        
        // 设置默认的评分规则
        ScoringRulesVO scoringRules = new ScoringRulesVO();
        scoringRules.setAlgorithmType("WEIGHTED_SUM");
        config.setScoringRules(scoringRules);
        
        return config;
    }

    /**
     * 计算原始得分
     */
    private BigDecimal calculateRawScore(List<AnswerVO> answers, ScoringRulesVO scoringRules) {
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (AnswerVO answer : answers) {
            if (answer.getAnswerScore() != null) {
                totalScore = totalScore.add(new BigDecimal(answer.getAnswerScore()));
            }
        }
        
        return totalScore;
    }

    /**
     * 计算标准分
     */
    private BigDecimal calculateStandardScore(BigDecimal rawScore, QuestionnaireConfigVO config) {
        // TODO: 实现标准分计算逻辑
        // 这里使用简单的线性转换
        return rawScore.multiply(new BigDecimal("1.2")).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算百分位排名
     */
    private BigDecimal calculatePercentileRank(BigDecimal standardScore, QuestionnaireConfigVO config) {
        // TODO: 实现百分位计算逻辑
        // 这里使用简单的转换
        BigDecimal percentile = standardScore.multiply(new BigDecimal("0.8"));
        return percentile.min(new BigDecimal("99.9")).max(new BigDecimal("0.1"));
    }

    /**
     * 确定风险等级
     */
    private RiskLevelEnum determineRiskLevel(BigDecimal score, RiskLevelRulesVO rules) {
        // TODO: 根据配置的规则确定风险等级
        // 这里使用简单的阈值判断
        if (score.compareTo(new BigDecimal("80")) >= 0) {
            return RiskLevelEnum.NORMAL;
        } else if (score.compareTo(new BigDecimal("60")) >= 0) {
            return RiskLevelEnum.ATTENTION;
        } else if (score.compareTo(new BigDecimal("40")) >= 0) {
            return RiskLevelEnum.WARNING;
        } else {
            return RiskLevelEnum.HIGH_RISK;
        }
    }

    /**
     * 计算各维度得分
     */
    private Map<String, BigDecimal> calculateDimensionScores(List<AnswerVO> answers, QuestionnaireConfigVO config) {
        Map<String, BigDecimal> dimensionScores = new HashMap<>();
        Map<String, Integer> dimensionCounts = new HashMap<>();
        
        for (AnswerVO answer : answers) {
            String dimension = answer.getDimensionCode();
            if (dimension != null && answer.getAnswerScore() != null) {
                BigDecimal currentScore = dimensionScores.getOrDefault(dimension, BigDecimal.ZERO);
                dimensionScores.put(dimension, currentScore.add(new BigDecimal(answer.getAnswerScore())));
                dimensionCounts.put(dimension, dimensionCounts.getOrDefault(dimension, 0) + 1);
            }
        }
        
        // 计算平均分
        for (Map.Entry<String, BigDecimal> entry : dimensionScores.entrySet()) {
            String dimension = entry.getKey();
            BigDecimal totalScore = entry.getValue();
            Integer count = dimensionCounts.get(dimension);
            if (count > 0) {
                BigDecimal averageScore = totalScore.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
                dimensionScores.put(dimension, averageScore);
            }
        }
        
        return dimensionScores;
    }

    /**
     * 生成结果报告内容
     */
    private String generateReportContent(ReportTemplateVO template, BigDecimal rawScore, 
                                       BigDecimal standardScore, RiskLevelEnum riskLevel, 
                                       Map<String, BigDecimal> dimensionScores) {
        // TODO: 使用模板引擎生成报告内容
        StringBuilder report = new StringBuilder();
        report.append("问卷结果报告\n");
        report.append("原始得分: ").append(rawScore).append("\n");
        report.append("标准分: ").append(standardScore).append("\n");
        report.append("风险等级: ").append(riskLevel.getName()).append("\n");
        report.append("各维度得分:\n");
        
        for (Map.Entry<String, BigDecimal> entry : dimensionScores.entrySet()) {
            report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        return report.toString();
    }

    /**
     * 生成建议内容
     */
    private String generateSuggestions(RiskLevelEnum riskLevel, Map<String, BigDecimal> dimensionScores, 
                                     QuestionnaireConfigVO config) {
        // TODO: 根据风险等级和维度得分生成个性化建议
        StringBuilder suggestions = new StringBuilder();
        
        switch (riskLevel) {
            case NORMAL:
                suggestions.append("您的心理状态良好，请继续保持。");
                break;
            case ATTENTION:
                suggestions.append("建议适当关注心理健康，可以通过运动、阅读等方式调节。");
                break;
            case WARNING:
                suggestions.append("建议及时寻求专业心理咨询师的帮助。");
                break;
            case HIGH_RISK:
                suggestions.append("强烈建议立即寻求专业心理医生的帮助。");
                break;
        }
        
        return suggestions.toString();
    }

}