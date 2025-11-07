package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 五级干预看板等级数据 Response VO")
@Data
public class InterventionDashboardLevelVO {
    
    @Schema(description = "类型标识", example = "major")
    private String type;
    
    @Schema(description = "等级标签", example = "重大")
    private String label;
    
    @Schema(description = "字典值", example = "5")
    private Integer dictValue;
    
    @Schema(description = "该等级学生数", example = "10")
    private Integer count;

    @Schema(description = "学生列表分页数据")
    private PageResult<InterventionStudentRespVO> studentPage;
}