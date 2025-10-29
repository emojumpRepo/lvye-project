package cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 保存咨询纪要 Request VO")
@Data
public class ConsultationAppointmentSaveSummaryReqVO {

    @Schema(description = "咨询纪要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "咨询纪要不能为空")
    private String summary;

    @Schema(description = "附件ID列表")
    private List<Long> attachmentIds;
}
