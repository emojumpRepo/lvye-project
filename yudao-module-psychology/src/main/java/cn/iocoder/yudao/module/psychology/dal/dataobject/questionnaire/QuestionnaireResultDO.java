package cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 问卷结果 DO
 *
 * @author 芋道源码
 */
@TableName(value = "lvye_questionnaire_result", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireResultDO extends TenantBaseDO {

    /**
     * 结果ID
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
     * 关联的测评任务编号（如果是测评任务的一部分）
     */
    private String assessmentTaskNo;

    /**
     * 答题详情
     */
    private String answers;

    /**
     * 原始得分
     */
    private BigDecimal score;

    /**
     * 风险等级：1-正常，2-关注，3-预警，4-高危
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
     * 各维度得分
     */
    private String dimensionScores;

    /**
     * 详细结果数据
     */
    private String resultData;

    /**
     * 完成时间
     */
    private Date completedTime;

    /**
     * 生成状态：0-待生成，1-生成中，2-已生成，3-生成失败
     */
    private Integer generationStatus;

    /**
     * 结果生成时间
     */
    private Date generationTime;

    /**
     * 生成错误信息
     */
    private String generationError;

}