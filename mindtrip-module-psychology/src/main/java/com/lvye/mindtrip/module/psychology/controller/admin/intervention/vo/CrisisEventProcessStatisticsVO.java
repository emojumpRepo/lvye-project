package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "管理后台 - 危机事件处理统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrisisEventProcessStatisticsVO {

    @Schema(description = "干预处理方式统计（1-心理访谈、2-量表评估、3-持续关注、4-直接解决）")
    @JsonProperty("intervention_process_method")
    private List<StatisticsItem> interventionProcessMethod;

    @Schema(description = "后续建议统计（1-继续访谈、2-继续评估、3-持续关注、4-问题解决、5-其他）")
    @JsonProperty("follow_up_suggestion")
    private List<StatisticsItem> followUpSuggestion;

    @Schema(description = "危机事件状态统计（1-已上报、2-已分配）")
    @JsonProperty("crisis_event_status")
    private List<StatisticsItem> crisisEventStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsItem {
        @Schema(description = "类型", example = "1")
        private Integer type;

        @Schema(description = "数量", example = "0")
        private Long count;
    }
}
