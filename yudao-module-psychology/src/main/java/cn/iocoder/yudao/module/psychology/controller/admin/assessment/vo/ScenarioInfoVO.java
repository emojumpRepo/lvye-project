package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 场景信息 VO
 * 
 * @author 芋道源码
 */
@Data
@Schema(description = "场景信息 VO")
public class ScenarioInfoVO {

    @Schema(description = "场景ID", example = "1")
    private Long id;

    @Schema(description = "场景编码", example = "LIBRARY_SCENARIO")
    private String code;

    @Schema(description = "场景名称", example = "图书馆场景")
    private String name;

    @Schema(description = "最大问卷数量", example = "5")
    private Integer maxQuestionnaireCount;

    @Schema(description = "前端路由", example = "/library")
    private String frontendRoute;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive;

    @Schema(description = "场景插槽列表")
    private List<ScenarioSlotInfoVO> slots;

    @Schema(description = "插槽-问卷映射关系")
    private List<SlotQuestionnaireMapping> mappings;

    /**
     * 场景插槽信息 VO
     */
    @Data
    @Schema(description = "场景插槽信息")
    public static class ScenarioSlotInfoVO {
        @Schema(description = "插槽ID", example = "1")
        private Long id;

        @Schema(description = "插槽编码", example = "library")
        private String slotKey;

        @Schema(description = "插槽名称", example = "图书馆")
        private String slotName;

        @Schema(description = "插槽顺序", example = "1")
        private Integer slotOrder;

        @Schema(description = "允许的问卷类型", example = "ANXIETY,DEPRESSION")
        private String allowedQuestionnaireTypes;

        @Schema(description = "前端组件标识", example = "LibraryComponent")
        private String frontendComponent;
    }

    /**
     * 插槽-问卷映射关系 VO
     */
    @Data
    @Schema(description = "插槽-问卷映射关系")
    public static class SlotQuestionnaireMapping {
        @Schema(description = "插槽编码", example = "library")
        private String slotKey;

        @Schema(description = "插槽名称", example = "图书馆")
        private String slotName;

        @Schema(description = "问卷ID", example = "10")
        private Long questionnaireId;

        @Schema(description = "问卷标题", example = "焦虑量表")
        private String questionnaireTitle;

        @Schema(description = "插槽内顺序", example = "1")
        private Integer slotOrder;
    }
}
