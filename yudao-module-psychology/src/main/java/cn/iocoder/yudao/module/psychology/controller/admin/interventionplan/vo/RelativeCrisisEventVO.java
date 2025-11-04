package cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关联危机事件 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 关联危机事件 VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelativeCrisisEventVO {

    @Schema(description = "危机干预记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "事件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "EV20250318123456")
    private String eventId;

    @Schema(description = "来源类型（枚举：CrisisSourceTypeEnum）", example = "1")
    private Integer sourceType;

}
