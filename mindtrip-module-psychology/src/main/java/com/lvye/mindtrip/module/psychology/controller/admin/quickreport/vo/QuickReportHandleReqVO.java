package com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报处理
 * @Version: 1.0
 */
@Schema(description = "管理后台 - 快速上报处理 Request VO")
@Data
@ToString(callSuper = true)
public class QuickReportHandleReqVO {

    /**
     * 上报记录ID
     */
    @Schema(description = "上报记录ID", example = "紧急程度")
    @NotNull(message = "上报记录ID不能为空")
    private Long id;

    /**
     * 上报标题
     */
    @Schema(description = "reportTitle", example = "reportTitle")
    private String reportTitle;

    /**
     * 上报内容描述
     */
    @Schema(description = "上报内容描述", example = "上报内容描述")
    private String reportContent;

    /**
     * 处理状态：1-待处理，2-处理中，3-已处理，4-已关闭
     */
    @Schema(description = "处理状态", example = "1")
    private Integer status;

    /**
     * 处理备注
     */
    @Schema(description = "处理备注", example = "处理备注")
    private String handleNotes;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间", example = "处理时间")
    private Date handleTime;

    /**
     * 标签（如：情绪异常、行为异常、学习问题等）
     */
    @Schema(description = "标签", example = "行为异常")
    private String tags;

    /**
     * 附件信息
     */
    @Schema(description = "附件信息", example = "附件信息")
    private String attachments;


}
