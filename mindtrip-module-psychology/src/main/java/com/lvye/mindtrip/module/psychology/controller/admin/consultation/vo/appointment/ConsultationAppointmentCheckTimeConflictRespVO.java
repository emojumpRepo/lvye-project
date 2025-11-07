package com.lvye.mindtrip.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 咨询预约时间冲突校验 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationAppointmentCheckTimeConflictRespVO {

    @Schema(description = "是否存在时间冲突", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean hasConflict;

    @Schema(description = "说明信息", example = "该时间段与已有预约冲突")
    private String message;
}
