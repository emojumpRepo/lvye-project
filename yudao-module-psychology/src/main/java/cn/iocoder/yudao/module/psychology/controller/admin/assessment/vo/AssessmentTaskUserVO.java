package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-13
 * @Description:测评任务参与人员列表
 * @Version: 1.0
 */
@Data
public class AssessmentTaskUserVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 学生档案ID
     */
    @Schema(description = "学生档案ID")
    private Long studentProfileId;

    /**
     * 任务编号（唯一）
     */
    @Schema(description = "任务编号")
    private String taskNo;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 班级
     */
    @Schema(description = "班级")
    private String className;

    /**
     * 年级
     */
    @Schema(description = "年级")
    private String gradeName;

    /**
     * 完成状态（枚举：ParticipantCompletionStatusEnum）
     */
    private Integer status;

    /**
     * 提交时间
     */
    private Date submitTime;

    /**
     * 风险等级
     */
    private Integer riskLevel;

}
