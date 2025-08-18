package cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 维度配置VO
 *
 * @author 芋道源码
 */
@Data
public class DimensionConfigVO {

    /**
     * 维度定义列表
     */
    private List<DimensionDefinitionVO> dimensions;

    /**
     * 题目与维度的映射关系
     */
    private Map<String, String> questionDimensionMap;

    /**
     * 维度定义VO
     */
    @Data
    public static class DimensionDefinitionVO {
        
        /**
         * 维度代码
         */
        private String dimensionCode;
        
        /**
         * 维度名称
         */
        private String dimensionName;
        
        /**
         * 维度描述
         */
        private String description;
        
        /**
         * 维度权重
         */
        private Double weight;
    }

}