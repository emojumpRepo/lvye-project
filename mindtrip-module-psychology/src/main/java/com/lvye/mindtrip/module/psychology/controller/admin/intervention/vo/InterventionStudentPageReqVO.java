package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import com.lvye.mindtrip.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 干预等级学生分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InterventionStudentPageReqVO extends PageParam {

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "咨询师ID")
    private Long counselorUserId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学号", example = "2021001")
    private String studentNumber;
}