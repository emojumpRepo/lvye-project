package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 测评任务 Base VO，提供给添加、修改、详细的子 VO 使用
 */
@Data
public class AssessmentTaskBaseVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TASK2024001")
    @NotBlank(message = "任务编号不能为空")
    private String taskNo;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024年春季心理测评")
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "关联问卷 ID 列表", example = "[1,2,3]")
    private java.util.List<Long> questionnaireIds;

    @Schema(description = "目标对象 1-学生，2-家长", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标对象不能为空")
    private Integer targetAudience;

    @Schema(description = "发布人管理员编号", example = "1")
    private Long publishUserId;

    @Schema(description = "发布人管理员", example = "张三")
    private String publishUser;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date startline;

    @Schema(description = "截止时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date deadline;

    @Schema(description = "场景ID", example = "1")
    private Long scenarioId;

    @Schema(description = "任务描述", example = "这是一个心理健康测评任务")
    private String description;

}