package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-25
 * @Description:学生测评任务历史
 * @Version: 1.0
 */
@Data
public class StudentAssessmentTaskHisVO {

    /**
     * 任务ID（唯一）
     */
    @Schema(description = "任务ID")
    private String taskId;


    /**
     * 任务编号（唯一）
     */
    @Schema(description = "任务编号")
    private String taskNo;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String taskName;

    /**
     *目标对象（字典：target_audience）
     */
    @Schema(description = "目标对象")
    private Integer targetAudience;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 风险等级
     */
    @Schema(description = "风险等级")
    private String riskLevel;

    /**
     * 评价
     */
    @Schema(description = "评价")
    private String evaluate;

    /**
     * 建议内容
     */
    @Schema(description = "建议内容")
    private String suggestions;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private Date startline;

    /**
     * 截止时间
     */
    @Schema(description = "截止时间")
    private Date deadline;


}
