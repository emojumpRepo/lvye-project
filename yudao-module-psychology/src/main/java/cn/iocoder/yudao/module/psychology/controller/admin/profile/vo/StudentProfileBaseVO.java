package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 学生档案 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class StudentProfileBaseVO {

    @Schema(description = "关联会员用户编号（学生）", example = "123")
    private Long memberUserId;

    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024001")
    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

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

    @Schema(description = "备注", example = "这是一个备注")
    private String remark;

}