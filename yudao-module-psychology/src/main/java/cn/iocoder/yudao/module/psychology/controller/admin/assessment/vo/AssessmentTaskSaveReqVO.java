package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 测评任务新增/修改 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AssessmentTaskSaveReqVO extends AssessmentTaskBaseVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "年级/班级列表", example = "1,2,3,4")
    @Valid
    private List<Long> deptIdList;

    @Schema(description = "用户列表", example = "1,2,3,4")
    @Valid
    private List<Long> userIdList;

}