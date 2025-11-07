package com.lvye.mindtrip.module.psychology.dal.dataobject.organization;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 组织架构扩展 DO（用于标识部门为学校/学部/年级/班级，并存储班级信息）
 */
@TableName(value = "lvye_dept_ext", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeptExtDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 部门编号，关联 system_dept.id */
    private Long deptId;

    /** 部门类型（字典：dept_type：school/division/grade/class） */
    private Integer deptType;

    /** 年级编号（如一年级=1，二年级=2），仅当为年级或班级时使用 */
    private Integer gradeNo;

    /** 班级序号（如1班=1），仅当为班级时使用 */
    private Integer classNo;

    /** 班主任管理员编号，仅当为班级时使用 */
    private Long headTeacherUserId;
}


