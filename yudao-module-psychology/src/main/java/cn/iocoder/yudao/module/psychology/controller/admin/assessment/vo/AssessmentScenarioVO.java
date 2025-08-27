package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 测评场景 VO - 统一的创建和更新VO
 */
@Data
@Schema(description = "测评场景 VO")
public class AssessmentScenarioVO {

    @Schema(description = "主键ID，更新时必填", example = "1")
    private Long id;

    @Schema(description = "场景编码", example = "CAMPUS_TRIP")
    @NotBlank(message = "场景编码不能为空")
    private String code;

    @Schema(description = "场景名称", example = "校园旅行")
    @NotBlank(message = "场景名称不能为空")
    private String name;

    @Schema(description = "最大问卷数量，空表示不限制", example = "3")
    private Integer maxQuestionnaireCount;

    @Schema(description = "前端路由标识", example = "campusTrip")
    @NotBlank(message = "前端路由标识不能为空")
    private String frontendRoute;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "是否启用不能为空")
    private Boolean isActive;

    @Schema(description = "扩展配置(JSON)", example = "{}")
    private String metadataJson;

    @Schema(description = "场景插槽列表")
    private List<ScenarioSlotVO> slots;

    /**
     * 场景插槽VO
     */
    @Data
    @Schema(description = "场景插槽信息")
    public static class ScenarioSlotVO {
        @Schema(description = "插槽ID，更新时必填", example = "1")
        private Long id;

        @Schema(description = "插槽编码", example = "library", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "插槽编码不能为空")
        private String slotKey;

        @Schema(description = "插槽名称", example = "图书馆", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "插槽名称不能为空")
        private String slotName;

        @Schema(description = "插槽顺序", example = "1")
        private Integer slotOrder;

        @Schema(description = "允许的问卷类型，逗号分隔", example = "ANXIETY,DEPRESSION")
        private String allowedQuestionnaireTypes;

        @Schema(description = "前端组件标识", example = "LibraryComponent")
        private String frontendComponent;

        @Schema(description = "元数据JSON，用于存储插槽的额外配置信息", example = "{\"theme\":\"dark\",\"layout\":\"vertical\"}")
        private String metadataJson;

        @Schema(description = "关联问卷ID", example = "1001")
        private Long questionnaireId;

        @Schema(description = "问卷详情")
        private QuestionnaireInfoVO questionnaire;
    }
}
