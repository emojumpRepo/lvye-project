package com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo;

import com.lvye.mindtrip.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.lvye.mindtrip.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 测评任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AssessmentTaskPageReqVO extends PageParam {

    @Schema(description = "任务编号", example = "TASK2024001")
    private String taskNo;

    @Schema(description = "任务名称", example = "2024年春季心理测评")
    private String name;

    @Schema(description = "按问卷过滤（问卷ID）", example = "1")
    private Long questionnaireId;

    @Schema(description = "目标对象", example = "1")
    private Integer targetAudience;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "发布人管理员编号", example = "1")
    private Long publishUserId;

    @Schema(description = "截止时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] deadline;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}