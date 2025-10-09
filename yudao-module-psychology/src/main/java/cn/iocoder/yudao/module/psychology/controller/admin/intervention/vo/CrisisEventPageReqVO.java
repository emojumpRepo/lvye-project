package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 危机事件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrisisEventPageReqVO extends PageParam {

    @Schema(description = "学生档案ID", example = "1")
    private Long studentProfileId;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "处理人ID", example = "1")
    private Long handlerUserId;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "负责心理老师ID")
    private Long counselorUserId;

    @Schema(description = "处理进度状态", example = "1")
    private Integer processStatus;
}