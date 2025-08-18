package cn.iocoder.yudao.module.psychology.controller.app.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Schema(description = "学生家长端 - 学生档案 Response VO")
@Data
public class WebStudentProfileRespVO {

    @Schema(description = "用户编号", example = "123")
    private Long userId;

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

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date updateTime;

    @Schema(description = "年级")
    private String gradeName;

    @Schema(description = "班级")
    private String className;

}