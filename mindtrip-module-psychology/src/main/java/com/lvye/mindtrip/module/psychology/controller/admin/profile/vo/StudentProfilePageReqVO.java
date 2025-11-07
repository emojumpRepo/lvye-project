package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import com.lvye.mindtrip.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.lvye.mindtrip.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 学生档案分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StudentProfilePageReqVO extends PageParam {

    @Schema(description = "学号（支持模糊查询）", example = "2024001")
    private String studentNo;

    @Schema(description = "姓名（支持模糊查询）", example = "张三")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "民族", example = "1")
    private Integer ethnicity;

    @Schema(description = "年级部门编号", example = "1")
    private Long gradeDeptId;

    @Schema(description = "班级部门编号", example = "2")
    private Long classDeptId;

    @Schema(description = "毕业状态", example = "0")
    private Integer graduationStatus;

    @Schema(description = "心理状态", example = "1")
    private Integer psychologicalStatus;

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "用户部门编号（自动注入）", example = "100")
    private Long deptId;

}