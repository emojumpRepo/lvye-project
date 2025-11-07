package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 家中孩子情况 VO
 */
@Data
@Schema(description = "家中孩子情况")
public class FamilyChildrenInfoVO {

    @Schema(description = "是否是家里唯一的孩子（1:是，0:否）", example = "1")
    private Integer isOnlyChild;

    @Schema(description = "家里一共有几个孩子", example = "1")
    private Integer childrenCount;

    @Schema(description = "您是家里第几个出生的孩子", example = "1")
    private Integer birthOrder;

    @Schema(description = "与家里第二个孩子相差几岁（独生子女为0）", example = "0")
    private Integer ageGapToSecond;
}
