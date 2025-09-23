package cn.iocoder.yudao.module.psychology.util;

import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.psychology.enums.RiskLevelEnum;

import java.math.BigDecimal;

/**
 * 风险等级工具类
 */
public class RiskLevelUtils {
    
    /**
     * 问卷结果风险等级字典类型
     */
    private static final String DICT_TYPE_QUESTIONNAIRE_RESULT_RISK_LEVEL = "questionnaire_result_risk_level";
    
    /**
     * 获取场景特定的风险等级名称
     * @param riskLevel 风险等级枚举
     * @param scenarioCode 场景编码
     * @return 场景特定的风险等级名称
     */
    public static String getScenarioSpecificRiskLevelName(RiskLevelEnum riskLevel, String scenarioCode) {
        if (riskLevel == null) {
            return "未知";
        }
        
        // CAMPUS_TRIP场景使用特定的风险等级名称
        if ("CAMPUS_TRIP".equals(scenarioCode)) {
            switch (riskLevel) {
                case NORMAL:
                    return "无/低风险";
                case ATTENTION:
                    return "轻度风险";
                case WARNING:
                    return "中度风险";
                case HIGH_RISK:
                    return "重度风险";
                default:
                    return riskLevel.getName();
            }
        }
        
        // 其他场景使用字典配置的名称
        String dictLabel = DictFrameworkUtils.parseDictDataLabel(
            DICT_TYPE_QUESTIONNAIRE_RESULT_RISK_LEVEL, 
            String.valueOf(riskLevel.getLevel())
        );
        
        return dictLabel != null ? dictLabel : riskLevel.getName();
    }
    
    /**
     * 根据风险等级值获取名称（使用字典）
     * @param riskLevel 风险等级值
     * @return 风险等级名称
     */
    public static String getRiskLevelNameFromDict(Integer riskLevel) {
        if (riskLevel == null) {
            return "未知";
        }
        
        String dictLabel = DictFrameworkUtils.parseDictDataLabel(
            DICT_TYPE_QUESTIONNAIRE_RESULT_RISK_LEVEL,
            String.valueOf(riskLevel)
        );
        
        if (dictLabel != null) {
            return dictLabel;
        }
        
        // 如果字典中没有，使用枚举默认值
        RiskLevelEnum levelEnum = RiskLevelEnum.fromLevel(riskLevel);
        return levelEnum != null ? levelEnum.getName() : "未知";
    }
    
    /**
     * 根据分数判断风险等级（委托给策略管理器）
     * @param score 分数
     * @param scenarioCode 场景编码
     * @return 风险等级
     */
    public static RiskLevelEnum determineRiskLevelByScore(BigDecimal score, String scenarioCode) {
        return RiskLevelStrategyManager.determineRiskLevelByScore(score, scenarioCode);
    }
    
    /**
     * 根据异常维度数判断风险等级（委托给策略管理器）
     * @param abnormalCount 异常维度数
     * @return 风险等级
     */
    public static RiskLevelEnum determineRiskLevelByAbnormalCount(Integer abnormalCount) {
        return RiskLevelStrategyManager.determineRiskLevelByAbnormalCount(abnormalCount);
    }
    
    /**
     * 判断是否为高风险等级（委托给策略管理器）
     * @param riskLevel 风险等级
     * @return 是否为高风险
     */
    public static boolean isHighRisk(Integer riskLevel) {
        return RiskLevelStrategyManager.isHighRisk(riskLevel);
    }
    
    /**
     * 判断是否需要关注（委托给策略管理器）
     * @param riskLevel 风险等级
     * @return 是否需要关注
     */
    public static boolean needsAttention(Integer riskLevel) {
        return RiskLevelStrategyManager.needsAttention(riskLevel);
    }
}