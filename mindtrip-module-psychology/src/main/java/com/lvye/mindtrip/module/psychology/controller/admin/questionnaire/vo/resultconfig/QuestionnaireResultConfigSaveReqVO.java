package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 问卷结果配置新增/修改 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireResultConfigSaveReqVO extends QuestionnaireResultConfigBaseVO {

    @Schema(description = "配置ID", example = "1024")
    private Long id;

}
