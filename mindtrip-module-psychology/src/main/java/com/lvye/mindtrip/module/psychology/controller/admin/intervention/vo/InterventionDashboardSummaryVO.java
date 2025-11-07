package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - 五级干预看板统计 Response VO")
@Data
public class InterventionDashboardSummaryVO {

    @Schema(description = "重大风险学生数", example = "5")
    private Integer majorCount;

    @Schema(description = "严重风险学生数", example = "10")
    private Integer severeCount;

    @Schema(description = "一般风险学生数", example = "20")
    private Integer generalCount;

    @Schema(description = "观察风险学生数", example = "30")
    private Integer observationCount;

    @Schema(description = "正常学生数", example = "100")
    private Integer normalCount;

    @Schema(description = "待评估学生数", example = "15")
    private Integer pendingAssessmentCount;

    @Schema(description = "总学生数", example = "180")
    private Integer totalCount;

    @Schema(description = "各等级详细统计")
    private Map<String, LevelDetail> levelDetails;
    
    @Schema(description = "学生列表分页数据")
    private PageResult<InterventionStudentRespVO> studentPage;

    @Data
    public static class LevelDetail {
        @Schema(description = "该等级学生数")
        private Integer count;
        
        @Schema(description = "占比", example = "10.5")
        private Double percentage;
        
        @Schema(description = "环比变化", example = "2")
        private Integer change;
    }
}