package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 规则校验响应VO
 */
@Data
@Schema(description = "规则校验响应")
public class RuleValidationRespVO {

    @Schema(description = "是否校验通过")
    private Boolean valid;

    @Schema(description = "错误信息列表")
    private List<String> errors;

    @Schema(description = "警告信息列表")
    private List<String> warnings;

    @Schema(description = "校验结果详情")
    private ValidationDetail detail;

    /**
     * 校验结果详情
     */
    @Data
    @Schema(description = "校验结果详情")
    public static class ValidationDetail {
        @Schema(description = "JSON表达式是否有效")
        private Boolean jsonValid;

        @Schema(description = "规则优先级是否冲突")
        private Boolean priorityConflict;

        @Schema(description = "规则条件是否重叠")
        private Boolean conditionOverlap;

        @Schema(description = "冲突的规则ID列表")
        private List<Long> conflictRuleIds;

        @Schema(description = "建议优先级")
        private Integer suggestedPriority;
    }
}
