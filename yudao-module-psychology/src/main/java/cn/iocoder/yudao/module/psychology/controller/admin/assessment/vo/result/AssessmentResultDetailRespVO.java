package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 测评结果详情 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 测评结果详情 Response VO")
@Data
public class AssessmentResultDetailRespVO {

    @Schema(description = "测评结果ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "参与者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long participantId;

    @Schema(description = "测评任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TASK_20250904174506_906195")
    private String taskNo;

    @Schema(description = "维度代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "total")
    private String dimensionCode;

    @Schema(description = "得分", example = "75")
    private Integer score;

    @Schema(description = "建议", example = "建议加强心理健康关注")
    private String suggestion;

    @Schema(description = "综合风险等级", example = "2")
    private Integer combinedRiskLevel;

    @Schema(description = "风险等级描述", example = "关注")
    private String riskLevelDescription;

    @Schema(description = "风险因素", example = "{\"factors\": []}")
    private String riskFactors;

    @Schema(description = "干预建议", example = "{\"suggestions\": []}")
    private String interventionSuggestions;

    @Schema(description = "生成配置版本", example = "v1.0")
    private String generationConfigVersion;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "问卷结果列表")
    private List<QuestionnaireResultDetailVO> questionnaireResults;

    /**
     * 问卷结果详情VO
     */
    @Schema(description = "问卷结果详情")
    @Data
    public static class QuestionnaireResultDetailVO {

        @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
        private Long questionnaireId;

        @Schema(description = "问卷名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "PHCSS中学生心理健康量表")
        private String questionnaireName;

        @Schema(description = "原始得分", example = "47.0")
        private BigDecimal rawScore;

        @Schema(description = "标准得分", example = "47.0")
        private BigDecimal standardScore;

        @Schema(description = "风险等级", example = "3")
        private Integer riskLevel;

        @Schema(description = "风险等级描述", example = "预警")
        private String levelDescription;

        @Schema(description = "建议", example = "建议多与家长、老师、同伴交流...")
        private String suggestions;

        @Schema(description = "报告内容", example = "[{\"dimensionName\":\"行为自我评价\",\"score\":8}]")
        private String reportContent;

        @Schema(description = "维度得分", example = "{\"dimension1\":45,\"dimension2\":40}")
        private String dimensionScores;

        @Schema(description = "百分位排名", example = "75.5")
        private BigDecimal percentileRank;
    }
}
