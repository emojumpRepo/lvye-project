package com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 干预事件步骤响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预事件步骤响应 VO")
@Data
public class InterventionEventStepRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "步骤标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "建立初步信任关系")
    private String title;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "步骤状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "教师笔记", example = "学生配合度较高")
    private String notes;

    @Schema(description = "附件ID列表", example = "[1, 2, 3]")
    private List<Long> attachmentIds;

}
