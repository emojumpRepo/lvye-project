package cn.iocoder.yudao.module.psychology.dal.dataobject.consultation;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 心理咨询评估 DO
 * 
 * @author 芋道源码
 */
@TableName(value = "lvye_consultation_assessment", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationAssessmentDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 咨询预约ID
     */
    private Long appointmentId;

    /**
     * 学生档案编号（冗余字段，便于查询）
     */
    private Long studentProfileId;

    /**
     * 评估人（心理老师）管理员编号
     */
    private Long counselorUserId;

    /**
     * 风险等级（字典：risk_level）
     * 1-重大、2-严重、3-一般、4-观察、5-正常
     */
    private Integer riskLevel;

    /**
     * 问题类型识别（JSON数组）
     * 例如：["学业压力", "人际关系"]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> problemTypes;

    /**
     * 后续处理建议（字典：follow_up_suggestion）
     * 继续咨询、继续量表测评、持续观察、问题基本解决、转介专业治疗
     */
    private Integer followUpSuggestion;

    /**
     * 评估方式（字典：assessment_mode）
     * 1-自由输入、2-模板上传
     */
    private Integer assessmentMode;

    /**
     * 评估内容（当mode为text时）
     */
    private String content;

    /**
     * 上传的评估文件ID（当mode为file时）
     */
    private Long fileId;

    /**
     * 是否为草稿
     */
    private Boolean draft;

    /**
     * 评估报告最终提交时间
     */
    private LocalDateTime submittedAt;
}