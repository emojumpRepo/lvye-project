package cn.iocoder.yudao.module.psychology.dal.dataobject.profile;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 学生档案 DO
 */
@TableName(value = "psy_student_profile", autoResultMap = true)
@KeySequence("psy_student_profile_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 关联会员用户编号（学生），关联 member_user 的 id */
    private Long memberUserId;

    /** 学号 */
    private String studentNo;

    /** 姓名 */
    private String name;

    /** 性别（字典：system_user_sex） */
    private Integer sex;

    /** 手机号 */
    private String mobile;

    /** 年级部门编号，关联 system_dept.id */
    private Long gradeDeptId;

    /** 班级部门编号，关联 system_dept.id */
    private Long classDeptId;

    /** 毕业状态（字典：graduation_status） */
    private Integer graduationStatus;

    /** 心理状态（字典：psychological_status） */
    private Integer psychologicalStatus;

    /** 风险等级（字典：risk_level） */
    private Integer riskLevel;

    /** 备注 */
    private String remark;
}



