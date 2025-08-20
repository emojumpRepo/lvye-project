package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 测评任务 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AssessmentTaskRespVO extends AssessmentTaskBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    /**
     * 完成人数
     */
    @Schema(description = "完成人数")
    private Integer finishNum;

    /**
     * 总人数
     */
    @Schema(description = "总人数")
    private Integer totalNum;

    /**
     * 关联问卷 ID 列表
     */
    private java.util.List<Long> questionnaireIds;

}