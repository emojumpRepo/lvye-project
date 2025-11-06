package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 干预计划响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 干预计划响应 VO")
@Data
public class InterventionPlanRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "危机干预编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "IV20250318123456")
    private String interventionId;

    @Schema(description = "学生档案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long studentProfileId;

    @Schema(description = "学号", example = "2024001")
    private String studentNo;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "班级名称", example = "高一(1)班")
    private String className;

    @Schema(description = "干预事件标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "学生心理干预计划")
    private String title;

    @Schema(description = "关联事件ID列表（用于存储）", example = "[1, 2, 3]")
    private List<Long> relativeEventIds;

    @Schema(description = "关联危机事件列表（包含ID和事件编号）")
    private List<RelativeCrisisEventVO> relativeEvents;

    @Schema(description = "干预模板ID", example = "1")
    private Long templateId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "创建者名字")
    private String creatorName;

    @Schema(description = "干预步骤列表")
    private List<InterventionEventStepRespVO> steps;

    @Schema(description = "状态（1=进行中）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

}
