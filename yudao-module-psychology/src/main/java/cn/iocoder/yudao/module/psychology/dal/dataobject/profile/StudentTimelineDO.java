package cn.iocoder.yudao.module.psychology.dal.dataobject.profile;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-18
 * @Description:学生时间线
 * @Version: 1.0
 */
@TableName(value = "lvye_student_profile_timeline")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentTimelineDO extends TenantBaseDO {

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
     * 学生档案编号
     */
    private Long studentProfileId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 操作人
     */
    private Long operator;

    /**
     * 类型
     */
    private Integer eventType;

    /**
     * 内容
     */
    private String content;

    /**
     * 关联业务编号（如任务、咨询、上报等）
     */
    private Long bizId;

}
