package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Schema(description = "管理后台 - 学生档案新增/修改 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StudentProfileSaveReqVO extends StudentProfileBaseVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

}