package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 问卷同步 Request VO")
@Data
public class QuestionnaireSyncReqVO {

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "问卷编号不能为空")
    private Long id;

    @Schema(description = "同步类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "同步类型不能为空")
    private Integer syncType; // 1-发布, 2-暂停, 3-状态同步

    @Schema(description = "外部问卷ID", example = "external_123")
    private String externalId;

}