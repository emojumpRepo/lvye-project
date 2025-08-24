package cn.iocoder.yudao.module.psychology.controller.app.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema(description = "学生家长端 - 测评任务 Response VO")
@Data
public class WebAssessmentTaskRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TASK2024001")
    private String taskNo;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024年春季心理测评")
    private String taskName;

    @Schema(description = "关联问卷 ID 列表")
    private java.util.List<Long> questionnaireIds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "开始时间")
    private Date startline;

    @Schema(description = "截止时间")
    private Date deadline;

    @Schema(description = "发布人管理员")
    private String publishUser;

    @Schema(description = "测评量表")
    private String scaleName;


}