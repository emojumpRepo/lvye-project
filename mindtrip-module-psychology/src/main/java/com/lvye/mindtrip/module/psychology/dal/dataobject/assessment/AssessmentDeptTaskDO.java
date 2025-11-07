package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-11
 * @Description:部门测评任务表
 * @Version: 1.0
 */
@TableName(value = "lvye_assessment_dept_task", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentDeptTaskDO extends TenantBaseDO {

    /**
     * ID 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 部门 ID
     */
    private Long deptId;

}
