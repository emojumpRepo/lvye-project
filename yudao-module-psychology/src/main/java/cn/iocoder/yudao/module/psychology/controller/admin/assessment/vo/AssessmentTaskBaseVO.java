package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

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
    private String name;

    @Schema(description = "量表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "A")
    @NotBlank(message = "量表编号不能为空")
    private String scaleCode;

    @Schema(description = "目标对象", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标对象不能为空")
    private Integer targetAudience;

    @Schema(description = "发布人管理员编号", example = "1")
    private Long publishUserId;

    @Schema(description = "截止时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime deadline;

}