package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 今日咨询预约分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConsultationAppointmentTodayPageReqVO extends PageParam {

    @Schema(description = "咨询师ID（可选，不传则查询所有咨询师）", example = "1")
    private Long counselorUserId;

    @Schema(description = "状态（可选）1-已预约 2-已完成 3-已闭环", example = "1")
    private Integer status;
}
