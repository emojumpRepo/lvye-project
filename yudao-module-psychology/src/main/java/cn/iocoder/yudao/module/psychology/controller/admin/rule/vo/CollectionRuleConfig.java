package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

/**
 * 收集规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "收集规则配置")
public class CollectionRuleConfig extends RuleConfigBaseVO.RuleDetailConfig {

    @Schema(description = "来源题目列表")
    @NotEmpty(message = "来源题目不能为空")
    private List<String> sourceQuestions;

    @Schema(description = "收集条件")
    private CollectCondition collectCondition;

    @Schema(description = "收集目标", allowableValues = {"option_text", "score", "custom"})
    @NotBlank(message = "收集目标不能为空")
    private String collectTarget;

    @Schema(description = "输出字段名")
    @NotBlank(message = "输出字段名不能为空")
    private String outputField;

    @Override
    public String getType() {
        return "collection_rule";
    }

    /**
     * 收集条件
     */
    @Data
    @Schema(description = "收集条件")
    public static class CollectCondition {
        @Schema(description = "分数阈值")
        private BigDecimal scoreThreshold;

        @Schema(description = "选项过滤")
        private String optionFilter;
    }
}
