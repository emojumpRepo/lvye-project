package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 问卷结果配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author MinGoo
 */
@Data
public class QuestionnaireResultConfigBaseVO {

    @Schema(description = "维度ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "维度ID不能为空")
    private Long dimensionId;

    @Schema(description = "题目索引", example = "1,2,3")
    private String questionIndex;

    @Schema(description = "计算类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计算类型不能为空")
    private Integer calculateType;

    @Schema(description = "计算公式", example = "SUM(score)")
    private String calculateFormula;

    @Schema(description = "规则匹配排序（升序）", example = "1")
    private Integer matchOrder;

    @Schema(description = "教师端评语", example = "该生在睡眠质量方面表现良好")
    private String teacherComment;

    @Schema(description = "学生端评语JSON数组", example = "[\"你的睡眠质量很好\", \"继续保持！\"]")
    private String studentComment;

    @Schema(description = "是否异常：0-正常，1-异常", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "是否异常不能为空")
    private Integer isAbnormal;

    @Schema(description = "风险等级：1-无/低风险，2-轻度风险，3-中度风险，4-重度风险", example = "1")
    private Integer riskLevel;

    @Schema(description = "等级：优秀、良好、一般、较差、很差", example = "优秀")
    private String level;

    @Schema(description = "描述", example = "用于说明该维度的解释与建议补充")
    private String description;

    @Schema(description = "状态（0：禁用，1：启用）", example = "1")
    private Integer status;

    @Schema(description = "是否可多命中（0：否，1：是）", example = "0")
    private Integer isMultiHit;

}
