package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "咨询时间戳（毫秒）- 查询对应日期的预约", example = "1640966400000")
    private Long consultTime;

    @Schema(description = "是否逾期")
    private Boolean overdue;
}