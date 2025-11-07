package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷同步 Response VO")
@Data
public class QuestionnaireSyncRespVO {

    @Schema(description = "同步是否成功", example = "true")
    private Boolean success;

    @Schema(description = "同步消息", example = "同步成功")
    private String message;

    @Schema(description = "外部问卷ID", example = "external_123")
    private String externalId;

    @Schema(description = "同步时间", example = "2024-01-01 12:00:00")
    private LocalDateTime syncTime;

    @Schema(description = "同步状态", example = "1")
    private Integer syncStatus;

}