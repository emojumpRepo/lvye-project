package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测评任务中的问卷信息 VO
 *
 * @author 芋道源码
 */
@Schema(description = "测评任务 - 问卷信息 VO")
@Data
public class QuestionnaireInfoVO {

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "问卷标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "心理健康评估问卷")
    private String title;

    @Schema(description = "问卷描述", example = "用于评估学生心理健康状况的专业问卷")
    private String description;

    @Schema(description = "问卷类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer questionnaireType;

    @Schema(description = "目标对象", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer targetAudience;

    @Schema(description = "题目数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
    private Integer questionCount;

    @Schema(description = "预计用时（分钟）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Integer estimatedDuration;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

}
