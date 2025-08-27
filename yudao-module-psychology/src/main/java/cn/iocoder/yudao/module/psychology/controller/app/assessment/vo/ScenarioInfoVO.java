package cn.iocoder.yudao.module.psychology.controller.app.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 场景信息 VO
 */
@Schema(description = "场景信息")
@Data
public class ScenarioInfoVO {

    @Schema(description = "场景ID")
    private Long id;

    @Schema(description = "场景编码")
    private String code;

    @Schema(description = "场景名称")
    private String name;

    @Schema(description = "前端路由")
    private String frontendRoute;

    @Schema(description = "场景插槽列表")
    private List<ScenarioSlotInfoVO> slots;
}

