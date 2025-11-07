package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 问卷结果 Response VO")
@Data
public class QuestionnaireResultRespVO {

    @Schema(description = "结果ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long questionnaireId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "关联的测评任务编号", example = "TASK_001")
    private String assessmentTaskNo;

    @Schema(description = "答题详情", example = "{\"question1\": \"A\", \"question2\": \"B\"}")
    private String answers;

    @Schema(description = "原始得分", example = "85.5")
    private BigDecimal score;

    @Schema(description = "风险等级：1-正常，2-关注，3-预警，4-高危", example = "1")
    private Integer riskLevel;

    @Schema(description = "评价", example = "心理健康状况良好")
    private String evaluate;

    @Schema(description = "建议内容", example = "建议继续保持良好的生活习惯")
    private String suggestions;

    @Schema(description = "各维度得分", example = "{\"emotional\": 80, \"social\": 85}")
    private String dimensionScores;

    @Schema(description = "详细结果数据", example = "{\"analysis\": \"详细分析内容\"}")
    private String resultData;

    @Schema(description = "维度数据", example = "{\"analysis\": \"详细分析内容\"}")
    private  List<DimensionVO> dimensions;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "生成状态：0-待生成，1-生成中，2-已生成，3-生成失败", example = "2")
    private Integer generationStatus;

    @Schema(description = "结果生成时间")
    private LocalDateTime generationTime;

    @Schema(description = "生成错误信息")
    private String generationError;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Data
    public static class DimensionVO {
    @Schema(description = "维度Id", example = "1024")
    private Long dimensionId;

    @Schema(description = "维度编号", example = "behavior_self_evaluation")
    private String dimensionCode;

    @Schema(description = "维度名称", example = "学习行为")
    private String name;

    @Schema(description = "分数", example = "15.00")
    private BigDecimal score;

    @Schema(description = "是否异常", example = "1")
    private Integer isAbnormal;

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "等级描述", example = "描述")
    private String level;

    @Schema(description = "教师评价", example = "教师评价")
    private String teacherComment;

    @Schema(description = "学生评价", example = "学生评价")
    private String studentComment;

    @Schema(description = "维度描述", example = "描述")
    private String description;
    }
}
