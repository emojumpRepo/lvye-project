package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 管理后台 - MTUI大学结果 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MTUI大学结果 Response VO")
@Data
public class MtuiUniversityResultRespVO {

    /**
     * 整体测评结果信息
     */
    @Schema(description = "整体测评结果信息")
    private AssessmentResultInfo assessmentResult;

    /**
     * 问卷结果列表
     */
    @Schema(description = "问卷结果列表")
    private List<QuestionnaireResultVO> questionnaireResults;

    /**
     * 测评结果信息VO
     */
    @Schema(description = "测评结果信息")
    @Data
    public static class AssessmentResultInfo {

        @Schema(description = "测评结果ID", example = "1024")
        private Long assessmentResultId;

        @Schema(description = "综合风险等级", example = "2")
        private Integer combinedRiskLevel;

        @Schema(description = "风险等级描述", example = "关注")
        private String riskLevelDescription;

        @Schema(description = "综合得分", example = "75")
        private Integer score;

        @Schema(description = "建议/结论摘要")
        private String suggestion;

        @Schema(description = "风险因素分析(JSON格式)")
        private String riskFactors;

        @Schema(description = "干预建议(JSON格式)")
        private String interventionSuggestions;

        @Schema(description = "生成规则版本", example = "v1.0")
        private String generationConfigVersion;
    }

    /**
     * 问卷结果VO
     */
    @Schema(description = "问卷结果")
    @Data
    public static class QuestionnaireResultVO {

        @Schema(description = "问卷结果ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long questionnaireResultId;

        @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
        private Long questionnaireId;

        @Schema(description = "问卷名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "MTUI大学生心理测评")
        private String questionnaireName;

        @Schema(description = "问卷描述", example = "用于评估大学生心理健康状况")
        private String questionnaireDescription;

        @Schema(description = "问卷类型", example = "1")
        private Integer questionnaireType;

        @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
        private Long userId;

        @Schema(description = "测评任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TASK_20250904174506_906195")
        private String assessmentTaskNo;

        @Schema(description = "答题数据(JSON格式)", example = "[{\"questionId\":1,\"answer\":\"A\"}]")
        private String answers;

        @Schema(description = "完成时间")
        private Date completedTime;

        @Schema(description = "维度结果列表")
        private List<MtuiDimensionResultVO> dimensionResults;
    }

    /**
     * MTUI维度结果VO
     */
    @Schema(description = "MTUI维度结果")
    @Data
    public static class MtuiDimensionResultVO {

        @Schema(description = "维度结果ID", example = "512")
        private Long dimensionResultId;

        @Schema(description = "维度ID", example = "10")
        private Long dimensionId;

        @Schema(description = "维度名称", example = "焦虑")
        private String dimensionName;

        @Schema(description = "维度编码", example = "anxiety")
        private String dimensionCode;

        @Schema(description = "维度描述", example = "评估学生的焦虑水平")
        private String dimensionDescription;

        @Schema(description = "维度得分", example = "45.5")
        private BigDecimal score;

        @Schema(description = "是否异常", example = "0")
        private Integer isAbnormal;

        @Schema(description = "风险等级", example = "2")
        private Integer riskLevel;

        @Schema(description = "等级描述", example = "轻度")
        private String level;

        @Schema(description = "教师评语", example = "该学生焦虑水平处于正常范围")
        private String teacherComment;

        @Schema(description = "学生评语", example = "你的焦虑水平在正常范围内")
        private String studentComment;

        @Schema(description = "维度排序", example = "1")
        private Integer sortOrder;

        @Schema(description = "是否参与模块计算", example = "1")
        private Integer participateModuleCalc;

        @Schema(description = "是否参与测评计算", example = "1")
        private Integer participateAssessmentCalc;

        @Schema(description = "是否参与心理问题排行", example = "1")
        private Integer participateRanking;
    }
}
