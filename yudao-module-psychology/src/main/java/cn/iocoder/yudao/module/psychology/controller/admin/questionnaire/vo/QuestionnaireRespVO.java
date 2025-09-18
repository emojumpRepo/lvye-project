package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireRespVO extends QuestionnaireBaseVO {

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "是否支持独立使用", example = "1")
    private Integer supportIndependentUse;

    @Schema(description = "访问次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer accessCount;

    @Schema(description = "完成次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer completionCount;

    @Schema(description = "同步状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer syncStatus;

    @Schema(description = "最后同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}