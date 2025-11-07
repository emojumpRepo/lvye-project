package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 问卷维度更新请求VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问卷维度更新请求")
public class QuestionnaireDimensionUpdateReqVO extends QuestionnaireDimensionBaseVO {

    @Schema(description = "维度ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "维度ID不能为空")
    private Long id;
}
