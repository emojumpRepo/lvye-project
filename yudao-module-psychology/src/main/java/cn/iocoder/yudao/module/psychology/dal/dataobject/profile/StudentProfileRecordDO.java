package cn.iocoder.yudao.module.psychology.dal.dataobject.profile;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生历史档案 DO
 */
@TableName(value = "lvye_student_profile_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentProfileRecordDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学年
     */
    private String studyYear;

    /**
     * 年级部门编号
     */
    private Long gradeDeptId;

    /**
     * 班级部门编号
     */
    private Long classDeptId;

    /**
     * 备注
     */
    private String remark;
}



