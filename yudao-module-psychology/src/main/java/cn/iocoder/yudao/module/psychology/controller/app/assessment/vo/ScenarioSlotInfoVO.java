package cn.iocoder.yudao.module.psychology.controller.app.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 场景插槽信息 VO
 */
@Schema(description = "场景插槽信息")
@Data
public class ScenarioSlotInfoVO {

    @Schema(description = "插槽编码")
    private String slotKey;

    @Schema(description = "插槽名称")
    private String slotName;

    @Schema(description = "插槽顺序")
    private Integer slotOrder;
}

