package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 标准分转换参数VO
 *
 * @author 芋道源码
 */
@Data
public class StandardScoreParamsVO {

    /**
     * 均值
     */
    private BigDecimal mean;

    /**
     * 标准差
     */
    private BigDecimal standardDeviation;

    /**
     * 目标均值（通常为50或100）
     */
    private BigDecimal targetMean;

    /**
     * 目标标准差（通常为10或15）
     */
    private BigDecimal targetStandardDeviation;

}