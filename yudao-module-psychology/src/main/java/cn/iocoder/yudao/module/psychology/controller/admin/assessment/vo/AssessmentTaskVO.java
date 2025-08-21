package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 测评任务 VO
 */
@Schema(description = "管理后台 - 测评任务 Response VO")
@Data
public class AssessmentTaskVO {

    /**
     * 主键
     */
    private Long id;

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
     * 关联问卷 ID 列表
     */
    @Schema(description = "关联问卷 ID 列表")
    private java.util.List<Long> questionnaireIds;

    /**
     * 槽位-问卷分配（分页展示用）
     */
    @Schema(description = "槽位-问卷分配")
    private java.util.List<SlotAssignmentVO> assignments;

    /**
     *目标对象（字典：target_audience）
     */
    @Schema(description = "目标对象")
    private Integer targetAudience;

    /**
     * 状态（枚举：AssessmentTaskStatusEnum）
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 发布人管理员编号
     */
    @Schema(description = "发布人管理员编号")
    private Long publishUserId;

    /**
     * 发布人管理员
     */
    @Schema(description = "发布人管理员")
    private String publishUser;

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

    /**
     * 完成人数
     */
    @Schema(description = "完成人数")
    private Long finishNum;

    /**
     * 总人数
     */
    @Schema(description = "总人数")
    private Long totalNum;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 问卷ID字符串（用于数据库查询）
     */
    @Schema(hidden = true)
    private String questionnaireIdsStr;

}



