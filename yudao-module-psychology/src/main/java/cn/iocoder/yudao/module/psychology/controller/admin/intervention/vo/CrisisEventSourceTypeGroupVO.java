package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 按来源类型分组的危机事件 Response VO")
@Data
public class CrisisEventSourceTypeGroupVO {

    @Schema(description = "来源类型", example = "1")
    private Integer sourceType;

    @Schema(description = "来源类型名称", example = "快速上报")
    private String sourceTypeName;

    @Schema(description = "该类型的危机事件数量", example = "10")
    private Integer count;

    @Schema(description = "该类型的危机事件列表")
    private List<CrisisEventRespVO> events;
}
