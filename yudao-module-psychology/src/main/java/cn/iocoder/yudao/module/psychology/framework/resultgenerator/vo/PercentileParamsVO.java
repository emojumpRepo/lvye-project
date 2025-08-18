package cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 百分位转换参数VO
 *
 * @author 芋道源码
 */
@Data
public class PercentileParamsVO {

    /**
     * 百分位参考数据
     */
    private List<PercentileReferenceVO> referenceData;

    /**
     * 百分位参考数据VO
     */
    @Data
    public static class PercentileReferenceVO {
        
        /**
         * 分数
         */
        private BigDecimal score;
        
        /**
         * 对应的百分位
         */
        private BigDecimal percentile;
    }

}