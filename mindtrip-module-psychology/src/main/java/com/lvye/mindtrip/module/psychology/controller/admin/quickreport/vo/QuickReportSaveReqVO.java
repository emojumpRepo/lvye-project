package com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报
 * @Version: 1.0
 */
@Schema(description = "管理后台 - 快速上报 Request VO")
@Data
@ToString(callSuper = true)
public class QuickReportSaveReqVO {

    /**
     * 学生档案编号
     */
    @Schema(description = "学生档案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "学生档案编号不能为空")
    private Long studentProfileId;

    /**
     * 上报标题
     */
    @Schema(description = "上报标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "上报标题")
    @NotBlank(message = "上报标题不能为空")
    @Length(max = 120, message = "上报标题不能超过 200")
    private String reportTitle;

    /**
     * 上报内容描述
     */
    @Schema(description = "上报内容描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "上报内容描述")
    @NotBlank(message = "上报内容描述不能为空")
    private String reportContent;

    /**
     * 紧急程度：1-一般，2-关注，3-紧急，4-非常紧急
     */
    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "紧急程度")
    @NotNull(message = "紧急程度不能为空")
    private Integer urgencyLevel;

    /**
     * 事件发生时间
     */
    @Schema(description = "事件发生时间", example = "2025-08-29 23:51:22")
    private Date incidentTime;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID", example = "紧急程度")
    @NotNull(message = "处理人ID不能为空")
    private Long handlerId;

    /**
     * 是否需要跟进：1-需要，0-不需要
     */
    @Schema(description = "是否需要跟进", example = "紧急程度")
    @NotNull(message = "是否需要跟进不能为空")
    private Integer followUpRequired;

    /**
     * 标签（如：情绪异常、行为异常、学习问题等）
     */
    @Schema(description = "标签", example = "标签")
    private String tags;

    /**
     * 附件信息
     */
    @Schema(description = "附件信息", example = "附件信息")
    private String attachments;


}
