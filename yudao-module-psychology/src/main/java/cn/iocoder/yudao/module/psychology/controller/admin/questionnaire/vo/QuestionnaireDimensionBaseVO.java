package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 问卷维度基础VO
 */
@Data
@Schema(description = "问卷维度基础信息")
public class QuestionnaireDimensionBaseVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;

    @Schema(description = "维度名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "情感虐待")
    @NotBlank(message = "维度名称不能为空")
    private String dimensionName;

    @Schema(description = "维度编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "EMOTIONAL_ABUSE")
    @NotBlank(message = "维度编码不能为空")
    private String dimensionCode;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "兼容旧类型，可为空")
    private Integer calculateType;

    @Schema(description = "是否参与模块计算", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否参与模块计算不能为空")
    private Boolean participateModuleCalc;

    @Schema(description = "是否参与测评计算", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否参与测评计算不能为空")
    private Boolean participateAssessmentCalc;

    @Schema(description = "是否参与心理问题排行", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否参与心理问题排行不能为空")
    private Boolean participateRanking;

    @Schema(description = "排序", example = "1")
    @Min(value = 0, message = "排序最小值为0")
    @Max(value = 999, message = "排序最大值为999")
    private Integer sortOrder;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;
}
