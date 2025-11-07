package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学生档案导入 Request VO")
@Data
public class StudentProfileImportReqVO {

    @Schema(description = "学生档案列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "学生档案列表不能为空")
    @Valid
    private List<StudentProfileSaveReqVO> studentProfiles;

}