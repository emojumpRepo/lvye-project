package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 危机事件状态统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrisisEventStatusStatisticsVO {

    @Schema(description = "状态值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long count;
}