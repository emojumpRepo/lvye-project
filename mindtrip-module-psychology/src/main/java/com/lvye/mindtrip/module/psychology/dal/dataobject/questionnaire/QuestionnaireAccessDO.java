package com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire;

import com.lvye.mindtrip.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 问卷访问记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "lvye_questionnaire_access", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireAccessDO extends TenantBaseDO {

    /**
     * 访问记录ID
     */
    @TableId
    private Long id;

    /**
     * 问卷ID
     */
    private Long questionnaireId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    /**
     * 访问IP
     */
    private String accessIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 访问来源：1-直接访问，2-测评任务，3-推荐链接
     */
    private Integer accessSource;

    /**
     * 会话时长（秒）
     */
    private Integer sessionDuration;

}