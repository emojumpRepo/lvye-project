package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 测评任务新增/修改 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AssessmentTaskSaveReqVO extends AssessmentTaskBaseVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

}