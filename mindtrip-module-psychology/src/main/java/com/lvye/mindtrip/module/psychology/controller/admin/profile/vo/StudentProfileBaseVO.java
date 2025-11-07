package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * 学生档案 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class StudentProfileBaseVO {

    @Schema(description = "用户编号", example = "123")
    private Long userId;

    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024001")
    @NotBlank(message = "学号不能为空")
    @Length(max = 60, message = "学号不能超过 60")
    private String studentNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Length(max = 120, message = "姓名不能超过 120")
    private String name;

    @Schema(description = "出生日期", example = "2008-05-20 00:00:00")
    private LocalDateTime birthDate;

    @Schema(description = "家庭住址", example = "北京市朝阳区某某街道123号")
    @Length(max = 500, message = "家庭住址不能超过 500 字符")
    private String homeAddress;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "民族", example = "1")
    private Integer ethnicity;

    @Schema(description = "身高（厘米）", example = "175.50")
    private java.math.BigDecimal height;

    @Schema(description = "体重（千克）", example = "65.80")
    private java.math.BigDecimal weight;

    @Schema(description = "实际年龄（岁）", example = "16")
    private Integer actualAge;

    @Schema(description = "家中孩子情况（JSON格式）", example = "{\"isOnlyChild\":1,\"childrenCount\":1,\"birthOrder\":1,\"ageGapToSecond\":0}")
    private String familyChildrenInfo;

    @Schema(description = "手机号", example = "13800138000")
    @Length(max = 11, message = "手机号不能超过11")
    private String mobile;

    @Schema(description = "监护人手机号", example = "13900139000")
    @Length(max = 11, message = "监护人手机号不能超过11")
    private String guardianMobile;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "110101200801012345")
    @NotBlank(message = "身份证号不能为空")
    @Length(min = 18, max = 18, message = "身份证号必须为18位")
    private String idCard;

    @Schema(description = "届别（入学年份）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024")
    @NotNull(message = "届别不能为空")
    private Integer enrollmentYear;

    @Schema(description = "毕业年份", example = "2025")
    private Integer graduationYear;

    @Schema(description = "年级部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "年级部门编号不能为空")
    private Long gradeDeptId;

    @Schema(description = "班级部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "班级部门编号不能为空")
    private Long classDeptId;

    @Schema(description = "毕业状态", example = "0")
    private Integer graduationStatus;

    @Schema(description = "心理状态", example = "1")
    private Integer psychologicalStatus;

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "特殊标记", example = "2,3")
    @Length(max = 500, message = "特殊标记不能超过 500 字符")
    private String specialMarks;

    @Schema(description = "备注", example = "这是一个备注")
    private String remark;

}