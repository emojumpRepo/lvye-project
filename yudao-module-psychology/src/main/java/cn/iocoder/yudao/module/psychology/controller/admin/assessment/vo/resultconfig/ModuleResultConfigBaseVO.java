package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.resultconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class ModuleResultConfigBaseVO {

    @Schema(description = "场景插槽ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "场景插槽ID不能为空")
    private Long scenarioSlotId;

    @Schema(description = "配置名称", example = "图书馆模块-等级规则")
    private String configName;

    @Schema(description = "规则类型：0-等级方面规则，1-评语方面规则", example = "0")
    private Integer ruleType;

    @Schema(description = "计算公式(JSON)", example = "{...}")
    private String calculateFormula;

    @Schema(description = "配置描述", example = "根据维度情况输出等级和建议")
    private String description;

    @Schema(description = "评价等级描述", example = "正常/关注/预警/高危")
    private String level;

    @Schema(description = "建议文本", example = "建议与老师沟通，按计划作息")
    private String suggestions;

    @Schema(description = "学生评语JSON数组", example = "[\"继续保持\",\"请注意作息\"]")
    private String comments;

    @Schema(description = "状态（0：禁用，1：启用）", example = "1")
    private Integer status;
}


