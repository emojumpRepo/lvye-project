package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 测评任务 DO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentTaskVO extends TenantBaseDO {

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
    private String name;

    /**
     * 量表编号，固定问卷 A/B
     */
    @Schema(description = "量表编号")
    private String scaleCode;

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
     * 截止时间
     */
    @Schema(description = "截止时间")
    private Date deadline;


}



