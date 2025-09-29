package cn.iocoder.yudao.module.psychology.util;

import cn.iocoder.yudao.module.psychology.enums.RiskLevelEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 风险等级策略管理器
 * 统一管理不同场景下的风险等级判断逻辑
 */
@Slf4j
public class RiskLevelStrategyManager {
    
    /**
     * 风险等级阈值配置
     * 可以从配置文件或数据库读取，这里先使用默认值
     */
    private static final Map<String, RiskLevelThresholds> SCENARIO_THRESHOLDS = new HashMap<>();
    
    static {
        // 默认场景阈值
        SCENARIO_THRESHOLDS.put("DEFAULT", new RiskLevelThresholds(70, 50, 30));
        
        // CAMPUS_TRIP场景特定阈值
        SCENARIO_THRESHOLDS.put("CAMPUS_TRIP", new RiskLevelThresholds(75, 55, 35));
        
        // 其他场景可以继续添加
    }
    
    /**
     * 根据总分判断风险等级
     * @param totalScore 总分
     * @param scenarioCode 场景编码
     * @return 风险等级
     */
    public static RiskLevelEnum determineRiskLevelByScore(BigDecimal totalScore, String scenarioCode) {
        if (totalScore == null) {
            log.warn("总分为空，返回默认风险等级：正常");
            return RiskLevelEnum.NORMAL;
        }
        
        RiskLevelThresholds thresholds = getThresholds(scenarioCode);
        
        if (totalScore.compareTo(BigDecimal.valueOf(thresholds.normalThreshold)) >= 0) {
            return RiskLevelEnum.NORMAL;
        } else if (totalScore.compareTo(BigDecimal.valueOf(thresholds.attentionThreshold)) >= 0) {
            return RiskLevelEnum.ATTENTION;
        } else if (totalScore.compareTo(BigDecimal.valueOf(thresholds.warningThreshold)) >= 0) {
            return RiskLevelEnum.WARNING;
        } else {
            return RiskLevelEnum.HIGH_RISK;
        }
    }
    
    /**
     * 根据异常维度数判断风险等级（适用于PHCSS等问卷）
     * @param abnormalCount 异常维度数
     * @return 风险等级
     */
    public static RiskLevelEnum determineRiskLevelByAbnormalCount(Integer abnormalCount) {
        if (abnormalCount == null || abnormalCount == 0) {
            return RiskLevelEnum.NORMAL;
        } else if (abnormalCount <= 2) {
            return RiskLevelEnum.ATTENTION;
        } else if (abnormalCount <= 4) {
            return RiskLevelEnum.WARNING;
        } else {
            return RiskLevelEnum.HIGH_RISK;
        }
    }
    
    /**
     * 根据百分比判断风险等级
     * @param percentage 百分比（0-100）
     * @param scenarioCode 场景编码
     * @return 风险等级
     */
    public static RiskLevelEnum determineRiskLevelByPercentage(BigDecimal percentage, String scenarioCode) {
        if (percentage == null) {
            return RiskLevelEnum.NORMAL;
        }
        
        // 将百分比转换为分数（假设满分100）
        return determineRiskLevelByScore(percentage, scenarioCode);
    }
    
    /**
     * 计算加权平均风险等级
     * @param riskLevels 风险等级数组
     * @param weights 对应的权重数组
     * @return 加权平均后的风险等级
     */
    public static RiskLevelEnum calculateWeightedAverageRiskLevel(Integer[] riskLevels, BigDecimal[] weights) {
        if (riskLevels == null || weights == null || riskLevels.length != weights.length) {
            log.warn("风险等级或权重数组无效");
            return RiskLevelEnum.NORMAL;
        }
        
        BigDecimal totalWeightedRisk = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (int i = 0; i < riskLevels.length; i++) {
            if (riskLevels[i] == null || weights[i] == null) {
                continue;
            }
            BigDecimal riskValue = new BigDecimal(riskLevels[i]);
            totalWeightedRisk = totalWeightedRisk.add(riskValue.multiply(weights[i]));
            totalWeight = totalWeight.add(weights[i]);
        }
        
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal averageRisk = totalWeightedRisk.divide(totalWeight, 0, RoundingMode.HALF_UP);
            return RiskLevelEnum.fromLevel(averageRisk.intValue());
        }
        
        return RiskLevelEnum.NORMAL;
    }
    
    /**
     * 获取场景对应的阈值配置
     * @param scenarioCode 场景编码
     * @return 阈值配置
     */
    private static RiskLevelThresholds getThresholds(String scenarioCode) {
        return SCENARIO_THRESHOLDS.getOrDefault(
            scenarioCode != null ? scenarioCode : "DEFAULT", 
            SCENARIO_THRESHOLDS.get("DEFAULT")
        );
    }
    
    /**
     * 风险等级阈值配置类
     */
    private static class RiskLevelThresholds {
        final double normalThreshold;     // 正常阈值（>=此值为正常）
        final double attentionThreshold;  // 关注阈值（>=此值为关注）
        final double warningThreshold;    // 预警阈值（>=此值为预警，<此值为高危）
        
        RiskLevelThresholds(double normal, double attention, double warning) {
            this.normalThreshold = normal;
            this.attentionThreshold = attention;
            this.warningThreshold = warning;
        }
    }
    
    /**
     * 判断是否为高风险等级
     * @param riskLevel 风险等级
     * @return 是否为高风险
     */
    public static boolean isHighRisk(Integer riskLevel) {
        return riskLevel != null && riskLevel >= RiskLevelEnum.WARNING.getLevel();
    }
    
    /**
     * 判断是否需要关注
     * @param riskLevel 风险等级
     * @return 是否需要关注
     */
    public static boolean needsAttention(Integer riskLevel) {
        return riskLevel != null && riskLevel >= RiskLevelEnum.ATTENTION.getLevel();
    }
}