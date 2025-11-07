package com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo;

import com.lvye.mindtrip.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 测评场景分页查询 Request VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "管理后台 - 测评场景分页查询 Request VO")
public class AssessmentScenarioPageReqVO extends PageParam {

    @Schema(description = "场景编码", example = "CAMPUS_TRIP")
    private String code;

    @Schema(description = "场景名称", example = "校园旅行")
    private String name;

    @Schema(description = "场景描述", example = "这是一个校园旅行测评场景，用于评估学生在校园环境中的心理状态")
    private String description;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive;
}
