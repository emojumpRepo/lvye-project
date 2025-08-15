package cn.iocoder.yudao.module.psychology.controller.app.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "学生家长端 - 学生档案 Response VO")
@Data
public class WebStudentProfileRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024001")
    private String studentNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "年级部门编号", example = "1")
    private Long gradeDeptId;

    @Schema(description = "班级部门编号", example = "2")
    private Long classDeptId;

    @Schema(description = "心理状态", example = "1")
    private Integer psychologicalStatus;

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}