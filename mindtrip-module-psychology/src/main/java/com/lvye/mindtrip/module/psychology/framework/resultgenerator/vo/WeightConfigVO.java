package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 权重配置VO
 *
 * @author 芋道源码
 */
@Data
public class WeightConfigVO {

    /**
     * 问卷权重映射
     */
    private Map<Long, BigDecimal> questionnaireWeights;

    /**
     * 维度权重映射
     */
    private Map<String, BigDecimal> dimensionWeights;

    /**
     * 获取问卷权重
     */
    public BigDecimal getWeight(Long questionnaireId) {
        return questionnaireWeights.getOrDefault(questionnaireId, BigDecimal.ONE);
    }

    /**
     * 获取维度权重
     */
    public BigDecimal getDimensionWeight(String dimensionCode) {
        return dimensionWeights.getOrDefault(dimensionCode, BigDecimal.ONE);
    }

}