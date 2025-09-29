package cn.iocoder.yudao.module.psychology.dal.dataobject.profile;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 家庭住址
     */
    private String homeAddress;

    /**
     * 性别（字典：system_user_sex）
     */
    private Integer sex;

    /**
     * 民族（字典：student_ethnicity）
     */
    private Integer ethnicity;

    /**
     * 身高（厘米）
     */
    private java.math.BigDecimal height;

    /**
     * 体重（千克）
     */
    private java.math.BigDecimal weight;

    /**
     * 实际年龄（岁）
     */
    private Integer actualAge;

    /**
     * 家中孩子情况（JSON格式）
     */
    private String familyChildrenInfo;

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
     * 特殊标记（多选，逗号分隔数字键值）
     * 如：2,3 表示学习困难+心理风险
     */
    private String specialMarks;

    /**
     * 监护人手机号
     */
    private String guardianMobile;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 届别（入学年份）
     */
    private Integer enrollmentYear;

    /**
     * 备注
     */
    private String remark;
}



