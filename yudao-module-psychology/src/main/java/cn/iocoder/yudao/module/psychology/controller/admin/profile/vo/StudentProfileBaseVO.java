package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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

    @Schema(description = "性别", example = "1")
    @Length(max = 1, message = "性别不能超过 1")
    private Integer sex;

    @Schema(description = "手机号", example = "13800138000")
    @Length(max = 11, message = "手机号不能超过11")
    private String mobile;

    @Schema(description = "年级部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "年级部门编号不能为空")
    private Long gradeDeptId;

    @Schema(description = "班级部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "班级部门编号不能为空")
    private Long classDeptId;

    @Schema(description = "毕业状态", example = "0")
    @Length(max = 1, message = "毕业状态不能超过 1")
    private Integer graduationStatus;

    @Schema(description = "心理状态", example = "1")
    @Length(max = 1, message = "心理状态不能超过 1")
    private Integer psychologicalStatus;

    @Schema(description = "风险等级", example = "1")
    @Length(max = 1, message = "风险等级不能超过 1")
    private Integer riskLevel;

    @Schema(description = "备注", example = "这是一个备注")
    private String remark;

}