package cn.iocoder.yudao.module.psychology.dal.dataobject.consultation;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 危机事件评估 DO
 * 
 * @author 芋道源码
 */
@TableName(value = "lvye_crisis_event_assessment", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrisisEventAssessmentDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 危机事件ID
     */
    private Long eventId;

    /**
     * 学生档案ID
     */
    private Long studentProfileId;

    /**
     * 评估人管理员编号
     */
    private Long assessorUserId;

    /**
     * 评估类型（字典：assessment_type）
     * 1-阶段性评估、2-最终评估
     */
    private Integer assessmentType;

    /**
     * 风险等级（字典：risk_level）
     * 1-重大、2-严重、3-一般、4-观察、5-正常
     */
    private Integer riskLevel;

    /**
     * 问题类型识别（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> problemTypes;

    /**
     * 后续建议（字典：follow_up_suggestion）
     * 1-继续访谈、2-继续评估、3-持续关注、4-问题解决
     */
    private Integer followUpSuggestion;

    /**
     * 评估详细内容
     */
    private String content;

    /**
     * 是否有就诊用药情况
     */
    private Boolean hasMedicalVisit;

    /**
     * 就诊记录
     */
    private String medicalVisitRecord;

    /**
     * 持续关注记录
     */
    private String observationRecord;

    /**
     * 附件ID列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> attachmentIds;
}