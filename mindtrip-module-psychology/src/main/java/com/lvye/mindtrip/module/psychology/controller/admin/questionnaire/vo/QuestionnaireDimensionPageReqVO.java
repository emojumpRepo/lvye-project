package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo;

import com.lvye.mindtrip.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷维度分页查询请求VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问卷维度分页查询请求")
public class QuestionnaireDimensionPageReqVO extends PageParam {

    @Schema(description = "问卷ID", example = "12")
    private Long questionnaireId;

    @Schema(description = "维度名称", example = "情感虐待")
    private String dimensionName;

    @Schema(description = "维度编码", example = "EMOTIONAL_ABUSE")
    private String dimensionCode;

    @Schema(description = "是否参与模块计算")
    private Boolean participateModuleCalc;

    @Schema(description = "是否参与测评计算")
    private Boolean participateAssessmentCalc;

    @Schema(description = "是否参与心理问题排行")
    private Boolean participateRanking;

    @Schema(description = "状态", example = "1")
    private Integer status;
}
