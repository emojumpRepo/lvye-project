package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-11
 * @Description:用户测评任务表
 * @Version: 1.0
 */
@TableName(value = "lvye_assessment_user_task", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentUserTaskDO extends TenantBaseDO {

    /**
     * 任务编号
     */
    private String taskNo;
    /**
     * 用户id
     */
    private Long userId;

    /**
     *
     * 参与人员标识：1 家长；0 学生
     */
    private int parentFlag;

    /**
     * 完成状态（枚举：ParticipantCompletionStatusEnum）
     */
    private Integer status;

    /**
     * 开始作答时间
     */
    private Date startTime;

    /** 提交时间 */
    private Date submitTime;

}
