package cn.iocoder.yudao.module.psychology.dal.dataobject.profile;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 学生档案 DO
 */
@TableName(value = "lvye_student_profile")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentProfileDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 用户编号（学生），关联 system_users 的 id
     */
    private Long userId;

    /**
     * 学号 用户编号（学生），关联 system_users 的 username
     */
    private String studentNo;

    /**
     * 姓名 关联 system_users 的 kickname
     */
    private String name;

    /**
     * 年级部门编号，关联 system_dept.id
     */
    private Long gradeDeptId;

    /**
     * 班级部门编号，关联 system_dept.id
     */
    private Long classDeptId;

    /**
     * 毕业状态（字典：graduation_status）
     */
    private Integer graduationStatus;

    /**
     * 心理状态
     */
    private Integer psychologicalStatus;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 备注
     */
    private String remark;
}



