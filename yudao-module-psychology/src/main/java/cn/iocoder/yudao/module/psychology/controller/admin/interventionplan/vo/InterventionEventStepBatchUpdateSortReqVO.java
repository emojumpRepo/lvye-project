package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 干预事件步骤批量更新排序 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预事件步骤批量更新排序 Request VO")
@Data
public class InterventionEventStepBatchUpdateSortReqVO {

    @Schema(description = "干预事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "干预事件ID不能为空")
    private Long interventionId;

    @Schema(description = "步骤排序列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "步骤排序列表不能为空")
    @Valid
    private List<StepSortItem> steps;

    @Schema(description = "步骤排序项")
    @Data
    public static class StepSortItem {

        @Schema(description = "步骤ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "步骤ID不能为空")
        private Long id;

        @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "排序值不能为空")
        private Integer sort;
    }
}
