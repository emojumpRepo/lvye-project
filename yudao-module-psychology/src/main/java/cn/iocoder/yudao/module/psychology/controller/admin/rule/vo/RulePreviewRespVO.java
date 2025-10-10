package cn.iocoder.yudao.module.psychology.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 规则预览响应VO
 */
@Data
@Schema(description = "规则预览响应")
public class RulePreviewRespVO {

    @Schema(description = "是否命中规则")
    private Boolean matched;

    @Schema(description = "命中的规则名称")
    private String ruleName;

    @Schema(description = "执行结果")
    private PreviewResult result;

    @Schema(description = "生成的JSON表达式")
    private Object jsonExpression;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;

    @Schema(description = "调试信息")
    private Map<String, Object> debugInfo;

    /**
     * 预览结果
     */
    @Data
    @Schema(description = "预览结果")
    public static class PreviewResult {
        @Schema(description = "结果等级")
        private String level;

        @Schema(description = "是否异常")
        private Boolean isAbnormal;

        @Schema(description = "结果消息")
        private String message;

        @Schema(description = "描述信息")
        private String description;

        @Schema(description = "建议内容")
        private String suggestions;

        @Schema(description = "教师评语")
        private String teacherComment;

        @Schema(description = "学生评语")
        private Object studentComments;

        @Schema(description = "收集到的数据")
        private Map<String, Object> collectedData;

        @Schema(description = "计算得分")
        private Object calculatedScore;
    }
}
