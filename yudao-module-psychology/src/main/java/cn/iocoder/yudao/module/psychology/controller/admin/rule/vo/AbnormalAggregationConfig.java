package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 异常因子聚合规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "异常因子聚合规则配置")
public class AbnormalAggregationConfig extends RuleConfigBaseVO.RuleDetailConfig {

    @Schema(description = "数据来源", allowableValues = {"dimensionResults", "moduleResults"})
    private String source = "dimensionResults";

    @Schema(description = "过滤条件")
    @Valid
    private AggregationFilters filters;

    @Schema(description = "阈值配置")
    @NotEmpty(message = "阈值配置不能为空")
    @Valid
    private List<AggregationThreshold> thresholds;

    @Override
    public String getType() {
        return "abnormal_aggregation";
    }

    /**
     * 聚合过滤条件
     */
    @Data
    @Schema(description = "聚合过滤条件")
    public static class AggregationFilters {
        @Schema(description = "是否参与测评计算")
        private Boolean participateAssessmentCalc;

        @Schema(description = "是否参与模块计算")
        private Boolean participateModuleCalc;

        @Schema(description = "指定维度ID列表")
        private List<Long> dimensionIds;

        @Schema(description = "指定维度编码列表")
        private List<String> dimensionCodes;

        @Schema(description = "排除维度ID列表")
        private List<Long> excludeDimensionIds;
    }

    /**
     * 聚合阈值
     */
    @Data
    @Schema(description = "聚合阈值")
    public static class AggregationThreshold {
        @Schema(description = "阈值条件类型", allowableValues = {"le", "lt", "ge", "gt", "eq"})
        @NotNull(message = "阈值条件类型不能为空")
        private String conditionType;

        @Schema(description = "阈值")
        @NotNull(message = "阈值不能为空")
        private Integer threshold;

        @Schema(description = "输出等级")
        private String level;

        @Schema(description = "输出消息")
        private String message;

        @Schema(description = "优先级（数字越小优先级越高）")
        private Integer priority = 100;
    }
}
