package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 测评任务 DO
 */
@TableName(value = "lvye_assessment_task", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentTaskDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 任务编号（唯一）
     */
    private String taskNo;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 量表编号，固定问卷 A/B
     */
    private String scaleCode;

    /**
     *目标对象（字典：target_audience）
     */
    private Integer targetAudience;

    /**
     * 状态（枚举：AssessmentTaskStatusEnum）
     */
    private Integer status;

    /**
     * 发布人管理员编号
     */
    private Long publishUserId;

    /**
     * 开始时间
     */
    private Date startline;

    /**
     * 截止时间
     */
    private Date deadline;

    /**
     * 发布人管理员
     */
    @TableField(exist = false)
    private String publishUser;

    /**
     * 完成人数
     */
    @TableField(exist = false)
    private Long finishNum;

    /**
     * 总人数
     */
    @TableField(exist = false)
    private Long totalNum;


}



