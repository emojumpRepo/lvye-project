package com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 正在进行的测评任务 Response VO")
@Data
public class OngoingTaskRespVO {

    @Schema(description = "任务编号", example = "TASK001")
    private String taskNo;

    @Schema(description = "任务名称", example = "2024春季心理测评")
    private String taskName;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "开始时间")
    private Date startline;

    @Schema(description = "截止时间")
    private Date deadline;

    @Schema(description = "发布人ID", example = "1")
    private Long publishUserId;

    @Schema(description = "发布人名称", example = "张老师")
    private String publishUser;

    @Schema(description = "任务状态：0-草稿，1-进行中，2-已完成，3-已过期", example = "1")
    private Integer status;

    @Schema(description = "关联的问卷列表")
    private List<QuestionnaireSimpleInfo> questionnaires;

    @Schema(description = "完成人数", example = "85")
    private Long completedCount;

    @Schema(description = "总人数", example = "120")
    private Long totalCount;

    @Schema(description = "完成率（百分比）", example = "70.83")
    private BigDecimal completionRate;

    /**
     * 问卷简要信息
     */
    @Schema(description = "问卷简要信息")
    @Data
    public static class QuestionnaireSimpleInfo {
        @Schema(description = "问卷ID", example = "1")
        private Long id;

        @Schema(description = "问卷标题", example = "心理健康评估")
        private String title;
    }

    /**
     * 计算完成率
     */
    public void calculateCompletionRate() {
        if (totalCount != null && totalCount > 0) {
            BigDecimal completed = new BigDecimal(completedCount != null ? completedCount : 0);
            BigDecimal total = new BigDecimal(totalCount);
            this.completionRate = completed.multiply(new BigDecimal("100"))
                    .divide(total, 2, RoundingMode.HALF_UP);
        } else {
            this.completionRate = BigDecimal.ZERO;
        }
    }
}
