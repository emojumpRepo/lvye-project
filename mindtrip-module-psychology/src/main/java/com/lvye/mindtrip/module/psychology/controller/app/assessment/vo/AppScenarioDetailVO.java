package com.lvye.mindtrip.module.psychology.controller.app.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "App - 场景明细 VO")
public class AppScenarioDetailVO {

    @Schema(description = "场景ID")
    private Long id;

    @Schema(description = "场景编码")
    private String code;

    @Schema(description = "场景名称")
    private String name;

    @Schema(description = "最大问卷数量")
    private Integer maxQuestionnaireCount;

    @Schema(description = "前端路由")
    private String frontendRoute;

    @Schema(description = "元数据JSON")
    private String metadataJson;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "插槽列表")
    private List<AppScenarioSlotDetailVO> slots;

    @Data
    @Schema(description = "App - 场景插槽明细 VO")
    public static class AppScenarioSlotDetailVO {
        @Schema(description = "插槽ID")
        private Long id;

        @Schema(description = "插槽编码")
        private String slotKey;

        @Schema(description = "插槽名称")
        private String slotName;

        @Schema(description = "插槽顺序")
        private Integer slotOrder;

        @Schema(description = "前端组件标识")
        private String frontendComponent;

        @Schema(description = "元数据JSON")
        private String metadataJson;

        @Schema(description = "问卷列表")
        private java.util.List<AppScenarioQuestionnaireAccessVO> questionnaires;
    }
}
