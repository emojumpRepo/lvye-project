package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 检查毕业年级风险等级异常学生 Request VO
 */
@Data
@Schema(description = "管理后台 - 检查毕业年级风险等级异常学生 Request VO")
public class CheckAbnormalStudentsReqVO {

    @Schema(description = "年级部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "年级部门ID不能为空")
    private Long gradeDeptId;

    @Schema(description = "入学年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022")
    @NotNull(message = "届别不能为空")
    private Integer enrollmentYear;
}
