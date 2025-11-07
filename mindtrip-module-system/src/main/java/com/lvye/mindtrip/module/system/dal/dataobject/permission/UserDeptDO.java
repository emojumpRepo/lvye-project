package com.lvye.mindtrip.module.system.dal.dataobject.permission;

import com.lvye.mindtrip.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-09
 * @Description: 用户和部门关联
 * @Version: 1.0
 */
@TableName("system_user_dept")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDeptDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 用户 ID
     */
    private Long userId;
    /**
     * 部门 ID
     */
    private Long deptId;
}
