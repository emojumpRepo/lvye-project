package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量毕业 Request VO
 */
@Data
@Schema(description = "管理后台 - 年级批量毕业 Request VO")
public class BatchGraduateReqVO {

    @Schema(description = "年级部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "年级部门ID不能为空")
    private Long gradeDeptId;

    @Schema(description = "毕业年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025")
    @NotNull(message = "毕业年份不能为空")
    private Integer graduationYear;

    @Schema(description = "入学年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022")
    @NotNull(message = "届别不能为空")
    private Integer enrollmentYear;

    @Schema(description = "不进行毕业的学生ID列表（排除这些学生）", example = "[1, 2, 3]")
    private List<Long> extraIds;
}
