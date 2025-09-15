package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 咨询预约分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConsultationAppointmentPageReqVO extends PageParam {

    @Schema(description = "学生档案ID", example = "1")
    private Long studentProfileId;

    @Schema(description = "咨询师ID", example = "1")
    private Long counselorUserId;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "是否逾期")
    private Boolean overdue;
}