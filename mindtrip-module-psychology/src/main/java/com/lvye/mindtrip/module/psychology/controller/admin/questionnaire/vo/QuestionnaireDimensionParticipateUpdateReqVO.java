package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 问卷维度参与设置批量更新请求VO
 */
@Data
@Schema(description = "问卷维度参与设置批量更新请求")
public class QuestionnaireDimensionParticipateUpdateReqVO {

    @Schema(description = "维度ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "维度ID列表不能为空")
    private List<Long> dimensionIds;

    @Schema(description = "是否参与模块计算")
    private Boolean participateModuleCalc;

    @Schema(description = "是否参与测评计算")
    private Boolean participateAssessmentCalc;

    @Schema(description = "是否参与心理问题排行")
    private Boolean participateRanking;

    @Schema(description = "操作类型", allowableValues = {"MODULE", "ASSESSMENT", "RANKING", "ALL"})
    @NotNull(message = "操作类型不能为空")
    private String operationType;
}
