package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
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
     * ID 编号，自增
     */
    @TableId
    private Long id;

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
     * 参与人员标识：0 学生；1 家长
     */
    private int parentFlag;

    /**
     * 完成状态（枚举：ParticipantCompletionStatusEnum）
     */
    private Integer status;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 评价
     */
    private String evaluate;

    /**
     * 建议内容
     */
    private String suggestions;

    /**
     * 开始作答时间
     */
    private Date startTime;

    /**
     * 提交时间
     */
    private Date submitTime;

}
