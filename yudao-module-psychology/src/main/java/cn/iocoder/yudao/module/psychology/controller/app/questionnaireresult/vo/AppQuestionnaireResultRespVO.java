package cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "学生端 - 问卷结果 Response VO")
@Data
public class AppQuestionnaireResultRespVO {

    @Schema(description = "结果编号", example = "2001")
    private Long id;

    @Schema(description = "问卷编号", example = "1024")
    private Long questionnaireId;

    @Schema(description = "问卷标题", example = "心理健康测评问卷")
    private String questionnaireTitle;

    @Schema(description = "问卷描述", example = "用于评估学生心理健康状况")
    private String questionnaireDescription;

    @Schema(description = "用户ID", example = "100")
    private Long userId;

    @Schema(description = "总分", example = "85")
    private Integer totalScore;

    @Schema(description = "最高分", example = "100")
    private Integer maxScore;

    @Schema(description = "得分率", example = "85.0")
    private Double scoreRate;

    @Schema(description = "风险等级", example = "2")
    private Integer riskLevel; // 1-低风险, 2-中风险, 3-高风险

    @Schema(description = "风险等级描述", example = "中等风险")
    private String riskLevelDesc;

    @Schema(description = "各维度得分")
    private List<DimensionScoreVO> dimensionScores;

    @Schema(description = "结果解读", example = "您的心理健康状况总体良好...")
    private String resultInterpretation;

    @Schema(description = "建议内容", example = "建议您保持良好的作息...")
    private String suggestions;

    @Schema(description = "详细报告内容")
    private String detailedReport;

    @Schema(description = "结果状态", example = "2")
    private Integer resultStatus; // 1-生成中, 2-生成成功, 3-生成失败

    @Schema(description = "生成时间", example = "2024-01-01 12:00:00")
    private LocalDateTime generateTime;

    @Schema(description = "完成时间", example = "2024-01-01 12:05:00")
    private LocalDateTime completeTime;

    @Schema(description = "答题时长（分钟）", example = "15")
    private Integer answerDuration;

    @Schema(description = "是否可以重新测试", example = "true")
    private Boolean canRetake;

    @Schema(description = "下次可测试时间", example = "2024-02-01 12:00:00")
    private LocalDateTime nextRetakeTime;

    @Schema(description = "维度得分")
    @Data
    public static class DimensionScoreVO {

        @Schema(description = "维度名称", example = "焦虑维度")
        private String dimensionName;

        @Schema(description = "维度代码", example = "ANXIETY")
        private String dimensionCode;

        @Schema(description = "得分", example = "25")
        private Integer score;

        @Schema(description = "最高分", example = "30")
        private Integer maxScore;

        @Schema(description = "得分率", example = "83.3")
        private Double scoreRate;

        @Schema(description = "等级", example = "2")
        private Integer level; // 1-低, 2-中, 3-高

        @Schema(description = "等级描述", example = "中等水平")
        private String levelDesc;

        @Schema(description = "解读", example = "您在焦虑维度表现为中等水平...")
        private String interpretation;

    }

}