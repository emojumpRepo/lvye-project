package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 正在进行的干预计划响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 正在进行的干预计划响应 VO")
@Data
public class InterventionPlanOngoingRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "危机干预编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "IV_20250106_0001")
    private String interventionId;

    @Schema(description = "干预事件标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "学生心理危机干预计划")
    private String title;

    @Schema(description = "学生档案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long studentProfileId;

    @Schema(description = "学生姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String studentName;

    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024001")
    private String studentNo;

    @Schema(description = "班级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "高一(1)班")
    private String className;

    @Schema(description = "状态（1=进行中）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建者名称", example = "张老师")
    private String creatorName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
