package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:学生监护人档案请求保温
 * @Version: 1.0
 */
@Schema(description = "管理后台 - 学生 - 家长档案新增/修改 Request VO")
@Data
public class StudentParentProfileReqVO {

    @Schema(description = "学生档案编号", example = "101")
    @NotNull
    private Long studentProfileId;

    @NotEmpty
    @Schema(description = "监护人列表", example = "")
    private List<ParentContactVO> parentList;

}
