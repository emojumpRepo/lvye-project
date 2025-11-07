package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 风险等级规则VO
 *
 * @author 芋道源码
 */
@Data
public class RiskLevelRulesVO {

    /**
     * 风险等级阈值列表
     */
    private List<RiskLevelThresholdVO> thresholds;

    /**
     * 风险等级阈值VO
     */
    @Data
    public static class RiskLevelThresholdVO {
        
        /**
         * 风险等级
         */
        private Integer riskLevel;
        
        /**
         * 最小分数（包含）
         */
        private BigDecimal minScore;
        
        /**
         * 最大分数（不包含）
         */
        private BigDecimal maxScore;
        
        /**
         * 等级描述
         */
        private String description;
    }

}