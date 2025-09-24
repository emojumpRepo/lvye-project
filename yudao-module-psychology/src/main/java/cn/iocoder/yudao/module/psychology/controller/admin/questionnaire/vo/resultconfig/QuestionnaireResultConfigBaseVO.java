package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig;

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

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;

    @Schema(description = "维度名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "睡眠质量")
    @NotNull(message = "维度名称不能为空")
    private String dimensionName;

    @Schema(description = "题目索引", example = "1,2,3")
    private String questionIndex;

    @Schema(description = "计算类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计算类型不能为空")
    private Integer calculateType;

    @Schema(description = "计算公式", example = "SUM(score)")
    private String calculateFormula;

    @Schema(description = "教师端评语", example = "该生在睡眠质量方面表现良好")
    private String teacherComment;

    @Schema(description = "学生端评语", example = "你的睡眠质量很好，继续保持！")
    private String studentComment;

    @Schema(description = "是否异常：0-正常，1-异常", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "是否异常不能为空")
    private Integer isAbnormal;

    @Schema(description = "等级：优秀、良好、一般、较差、很差", example = "优秀")
    private String level;

    @Schema(description = "描述", example = "用于说明该维度的解释与建议补充")
    private String description;

}
