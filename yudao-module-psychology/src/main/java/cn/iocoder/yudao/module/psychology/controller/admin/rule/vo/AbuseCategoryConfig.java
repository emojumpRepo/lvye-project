package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 虐待分类规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "虐待分类规则配置")
public class AbuseCategoryConfig extends RuleConfigBaseVO.RuleDetailConfig {

    @Schema(description = "匹配逻辑", allowableValues = {"ANY", "ALL"})
    @NotBlank(message = "匹配逻辑不能为空")
    private String logic;

    @Schema(description = "虐待分类列表")
    @NotEmpty(message = "虐待分类不能为空")
    @Valid
    private List<AbuseCategoryItem> categories;

    @Override
    public String getType() {
        return "abuse_category";
    }

    /**
     * 虐待分类项
     */
    @Data
    @Schema(description = "虐待分类项")
    public static class AbuseCategoryItem {
        @Schema(description = "分类名称", example = "情感虐待")
        @NotBlank(message = "分类名称不能为空")
        private String name;

        @Schema(description = "题目列表")
        @NotEmpty(message = "题目列表不能为空")
        private List<String> questions;

        @Schema(description = "阈值")
        @NotNull(message = "阈值不能为空")
        private BigDecimal threshold;

        @Schema(description = "是否启用")
        @NotNull(message = "启用状态不能为空")
        private Boolean enabled;
    }
}
