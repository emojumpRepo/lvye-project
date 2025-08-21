package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 槽位分配 VO
 * 
 * @author 芋道源码
 */
@Data
@Schema(description = "槽位分配 VO")
public class SlotAssignmentVO {

    @Schema(description = "槽位标识", example = "library")
    private String slotKey;

    @Schema(description = "问卷ID", example = "101")
    private Long questionnaireId;

    @Schema(description = "槽位顺序", example = "1")
    private Integer slotOrder;
}
