package cn.iocoder.yudao.module.psychology.dal.dataobject.intervention;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 危机干预事件 DO
 *
 * @author 芋道源码
 */
@TableName(value = "lvye_intervention_event", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterventionEventDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 危机干预编号
     */
    private String interventionId;

    /**
     * 学生档案ID
     */
    private Long studentProfileId;

    /**
     * 干预事件标题
     */
    private String title;

    /**
     * 关联事件ID列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> relativeEventIds;

    /**
     * 干预模板ID
     */
    private Long templateId;

}
