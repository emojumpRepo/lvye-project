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
     * 操作人管理员编号
     */
    private Long operatorUserId;

    /**
     * 操作类型（字典：process_action）
     * 分配、更改负责人、选择处理方式、阶段性评估、结案等
     */
    private String action;

    /**
     * 处理内容记录
     */
    private String content;

    /**
     * 附件URL列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> attachments;
}