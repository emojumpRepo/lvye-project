package cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 干预配置VO
 *
 * @author 芋道源码
 */
@Data
public class InterventionConfigVO {

    /**
     * 风险等级对应的干预策略
     */
    private Map<Integer, List<InterventionStrategyVO>> riskLevelStrategies;

    /**
     * 干预策略VO
     */
    @Data
    public static class InterventionStrategyVO {
        
        /**
         * 策略类型
         */
        private String strategyType;
        
        /**
         * 策略名称
         */
        private String strategyName;
        
        /**
         * 策略描述
         */
        private String description;
        
        /**
         * 优先级
         */
        private Integer priority;
    }

}