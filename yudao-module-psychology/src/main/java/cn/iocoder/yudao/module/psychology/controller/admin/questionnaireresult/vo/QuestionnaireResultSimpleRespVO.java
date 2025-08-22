package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 问卷结果简单响应 VO
 */
@Schema(description = "管理后台 - 问卷结果简单响应 VO")
@Data
public class QuestionnaireResultSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long questionnaireId;

    @Schema(description = "问卷标题", example = "心理健康测评问卷")
    private String questionnaireTitle;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3072")
    private Long userId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "原始分数", example = "85.5")
    private Double totalScore;

    @Schema(description = "标准分", example = "90.2")
    private Double standardScore;

    @Schema(description = "百分位", example = "75.8")
    private Double percentileRank;

    @Schema(description = "得分率", example = "85.5")
    private Double scoreRate;

    @Schema(description = "风险等级描述", example = "低风险")
    private String riskLevelDesc;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
