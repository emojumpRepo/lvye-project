package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 问卷结果 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class QuestionnaireResultBaseVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "答案数据", example = "{\"q1\":\"A\",\"q2\":\"B\"}")
    private String answerData;

    @Schema(description = "原始总分(rawScore)", example = "85.5")
    private Double totalScore;

    @Schema(description = "标准分(standardScore)", example = "72.30")
    private Double standardScore;

    @Schema(description = "百分位排名(percentileRank)", example = "65.50")
    private Double percentileRank;

    @Schema(description = "各维度得分", example = "{\"anxiety\":80,\"depression\":90}")
    private String dimensionScores;

    @Schema(description = "风险等级", example = "2")
    private Integer riskLevel;

    @Schema(description = "报告内容(reportContent)")
    private String reportContent;

    @Schema(description = "建议内容")
    private String suggestions;

    @Schema(description = "生成状态", example = "1")
    private Integer generationStatus;

    @Schema(description = "提交时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime submitTime;

    @Schema(description = "生成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime generationTime;

    @Schema(description = "生成错误信息(generationError)")
    private String generationError;

}