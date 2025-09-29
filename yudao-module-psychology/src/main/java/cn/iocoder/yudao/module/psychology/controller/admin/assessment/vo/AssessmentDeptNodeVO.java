package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 部门统计树节点（年级/班级）
 */
@Data
public class AssessmentDeptNodeVO {

    @Schema(description = "部门ID（年级或班级），未知时为 -1")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "总参与人数")
    private Long totalParticipants;

    @Schema(description = "已完成人数")
    private Long completedParticipants;

    @Schema(description = "完成率，百分比，两位小数")
    private BigDecimal completionRate;

    @Schema(description = "风险等级统计")
    private List<RiskLevelStatisticsVO> riskLevelStatistics;

    @Schema(description = "子节点（班级列表，仅年级节点包含）")
    private List<AssessmentDeptNodeVO> children;
}

