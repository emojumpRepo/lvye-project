package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生档案导入 Response VO")
@Data
public class StudentProfileImportRespVO {

    @Schema(description = "成功数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer successCount;

    @Schema(description = "失败数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer failureCount;

    @Schema(description = "失败原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private String failReason;

}