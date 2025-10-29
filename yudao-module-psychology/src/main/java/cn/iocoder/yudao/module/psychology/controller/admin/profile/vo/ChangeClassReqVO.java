package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 学生换班请求 VO（支持批量）
 */
@Schema(description = "管理后台 - 学生换班请求")
@Data
public class ChangeClassReqVO {

    @Schema(description = "学生档案ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1024, 1025, 1026]")
    @NotEmpty(message = "学生档案ID列表不能为空")
    private List<Long> studentProfileIds;

    @Schema(description = "目标年级部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "目标年级部门ID不能为空")
    private Long gradeDeptId;

    @Schema(description = "目标班级部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    @NotNull(message = "目标班级部门ID不能为空")
    private Long classDeptId;

    @Schema(description = "换班原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "因家庭搬迁需要换班")
    @NotBlank(message = "换班原因不能为空")
    private String reason;

}
