package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

/**
 * 综合风险等级规则VO
 *
 * @author 芋道源码
 */
@Data
public class CombinedRiskLevelRulesVO {

    /**
     * 算法类型：MAX_RISK, WEIGHTED_AVERAGE, CUSTOM
     */
    private String algorithmType;

    /**
     * 自定义规则配置（JSON格式）
     */
    private String customRules;

}