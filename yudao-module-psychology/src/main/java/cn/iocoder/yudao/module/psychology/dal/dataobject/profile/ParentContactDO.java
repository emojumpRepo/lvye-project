package cn.iocoder.yudao.module.psychology.dal.dataobject.profile;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 家长联系人 DO
 */
@TableName(value = "lvye_student_parent_profile", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ParentContactDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /**
     * 学生档案编号
     */
    private Long studentProfileId;

    /**
     * 关系（父亲/母亲/监护人等）
     */
    private int relation;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 备注
     */
    private String remark;

    /**
     * 职业
     */
    private String work;

    /**
     * 婚姻状态
     */
    private Integer maritalStatus;
}



