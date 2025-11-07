package com.lvye.mindtrip.module.psychology.controller.app.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-15
 * @Description:学生家长端测评任务列表VO
 * @Version: 1.0
 */
@Data
public class WebAssessmentTaskVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "关联问卷 ID 列表")
    private java.util.List<Long> questionnaireIds;

    /**
     * Mapper 中 GROUP_CONCAT 返回的问卷ID字符串，供 Service 解析
     */
    private String questionnaireIdsStr;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "场景ID")
    private Long scenarioId;

    @Schema(description = "开始时间")
    private Date startline;

    @Schema(description = "截止时间")
    private Date deadline;

    @Schema(description = "发布人管理员")
    private String publishUser;

}
