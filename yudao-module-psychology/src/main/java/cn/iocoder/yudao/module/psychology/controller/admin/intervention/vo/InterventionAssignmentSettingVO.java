package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 危机干预分配设置 VO
 */
@Schema(description = "管理后台 - 危机干预分配设置 VO")
@Data
public class InterventionAssignmentSettingVO {

    @Schema(description = "分配模式：manual-手动分配, auto-psychology-自动分配给心理老师, auto-head-teacher-自动分配给班主任",
            example = "manual", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分配模式不能为空")
    private String mode;

    @Schema(description = "默认心理老师ID（当学生档案未绑定责任心理老师时使用）", example = "100")
    private Long defaultPsychologyId;
}