package com.lvye.mindtrip.module.psychology.dal.dataobject.intervention;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 干预等级变更历史 DO
 * 
 * @author 芋道源码
 */
@TableName(value = "lvye_intervention_level_history", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterventionLevelHistoryDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 学生档案编号
     */
    private Long studentProfileId;

    /**
     * 原等级
     */
    private Integer oldLevel;

    /**
     * 新等级
     */
    private Integer newLevel;

    /**
     * 调整原因
     */
    private String changeReason;

    /**
     * 操作人管理员编号
     */
    private Long operatorUserId;
}