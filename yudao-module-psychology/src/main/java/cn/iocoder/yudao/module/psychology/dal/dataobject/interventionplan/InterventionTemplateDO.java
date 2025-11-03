package cn.iocoder.yudao.module.psychology.dal.dataobject.interventionplan;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 干预模板 DO
 *
 * @author 芋道源码
 */
@TableName(value = "lvye_intervention_template", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterventionTemplateDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 模板标题
     */
    private String title;

    /**
     * 是否是官方模板
     */
    private Boolean isOfficial;

}
