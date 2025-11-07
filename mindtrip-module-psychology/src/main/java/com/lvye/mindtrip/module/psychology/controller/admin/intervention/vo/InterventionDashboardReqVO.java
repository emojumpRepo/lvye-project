package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import com.lvye.mindtrip.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 五级干预看板查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InterventionDashboardReqVO extends PageParam {

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "年级ID")
    private Long gradeId;

    @Schema(description = "咨询师ID")
    private Long counselorUserId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学号", example = "2021001")
    private String studentNumber;

    @Schema(description = "就读状态", example = "1")
    private Integer studyStatus;

    @Schema(description = "是否只看我负责的", example = "false")
    private Boolean onlyMine;

    @Schema(description = "排序字段", example = "riskLevel")
    private String sortField;

    @Schema(description = "排序方式", example = "desc")
    private String sortOrder;

    @Schema(description = "排除的危机事件状态（用于待评等级查询，排除指定状态的危机事件）", example = "5")
    private Integer excludeCrisisStatus;

    @Schema(description = "干预事件状态（1=危机干预中，2=危机干预结案）", example = "1")
    private Integer interventionEventStatus;

    @Schema(description = "排除的干预事件状态（用于排除正在干预的学生）", example = "1")
    private Integer excludeInterventionEventStatus;
}