package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 周预约数据查询 Request VO")
@Data
public class ConsultationAppointmentWeeklyReqVO {

    @Schema(description = "周偏移量（0-当前周，-1-上一周，1-下一周）", example = "0")
    private Integer weekOffset = 0;
}
