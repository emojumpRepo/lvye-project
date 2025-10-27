package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 创建危机事件 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrisisEventCreateRespVO {

    @Schema(description = "事件ID", example = "1")
    private Long id;

    @Schema(description = "事件编号", example = "RPT_2025_123456")
    private String eventId;

    @Schema(description = "事件标题", example = "学生心理危机")
    private String title;
}
