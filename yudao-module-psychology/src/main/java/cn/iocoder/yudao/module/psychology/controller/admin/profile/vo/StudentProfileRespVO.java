package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 学生档案 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StudentProfileRespVO extends StudentProfileBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "年级", requiredMode = Schema.RequiredMode.REQUIRED)
    private String gradeName;

    @Schema(description = "班级", requiredMode = Schema.RequiredMode.REQUIRED)
    private String className;

    @Schema(description = "特殊标记名称", example = "家庭困难, 心理风险")
    private String specialMarkNames;

}