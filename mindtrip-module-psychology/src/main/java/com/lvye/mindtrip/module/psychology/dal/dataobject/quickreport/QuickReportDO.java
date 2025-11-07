package com.lvye.mindtrip.module.psychology.dal.dataobject.quickreport;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报实体
 * @Version: 1.0
 */
@TableName(value = "lvye_psychology_quick_report", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class QuickReportDO extends TenantBaseDO {

    /**
     * 问卷ID
     */
    @TableId
    private Long id;

    /**
     * 学生档案编号
     */
    private Long studentProfileId;

    /**
     * 上报人ID（教师）
     */
    private Long reporterId;

    /**
     * 上报标题
     */
    private String reportTitle;

    /**
     * 上报内容描述
     */
    private String reportContent;

    /**
     * 紧急程度：1-一般，2-关注，3-紧急，4-非常紧急
     */
    private Integer urgencyLevel;

    /**
     * 事件发生时间
     */
    private Date incidentTime;

    /**
     * 上报时间
     */
    private Date reportTime;

    /**
     * 处理状态：1-待处理，2-处理中，3-已处理，4-已关闭
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理备注
     */
    private String handleNotes;

    /**
     * 处理时间
     */
    private Date handleTime;

    /**
     * 是否需要跟进：1-需要，0-不需要
     */
    private Integer followUpRequired;

    /**
     * 标签（如：情绪异常、行为异常、学习问题等）
     */
    private String tags;

    /**
     * 附件信息
     */
    private String attachments;

}
