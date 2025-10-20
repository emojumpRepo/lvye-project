package cn.iocoder.yudao.module.psychology.dal.dataobject.consultation;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 危机事件处理过程 DO
 * 
 * @author 芋道源码
 */
@TableName(value = "lvye_crisis_event_process", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrisisEventProcessDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 危机事件ID
     */
    private Long eventId;

    /**
     * 测评任务编号
     */
    private String taskNo;

    /**
     * 操作人管理员编号
     */
    private Long operatorUserId;

    /**
     * 操作类型（字典：process_action）
     * 分配、更改负责人、选择处理方式、阶段性评估、结案等
     */
    private String action;

    /**
     * 处理内容记录（用于存储处理方式、评估内容等文本信息）
     */
    private String content;

    /**
     * 操作原因
     */
    private String reason;

    /**
     * 涉及的用户ID（如新负责人ID、原负责人ID等）
     */
    private Long relatedUserId;

    /**
     * 原用户ID（用于记录变更前的负责人等）
     */
    private Long originalUserId;


    /**
     * 附件ID列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> attachments;

    /**
     * 评估ID（关联评估记录）
     */
    private Long assessmentId;
}