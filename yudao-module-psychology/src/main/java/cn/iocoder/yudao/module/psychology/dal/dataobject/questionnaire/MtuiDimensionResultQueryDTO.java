package cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * MTUI大学维度结果查询DTO
 * 用于承载联表查询结果
 *
 * @author 芋道源码
 */
@Data
public class MtuiDimensionResultQueryDTO {

    // ========== 问卷结果表字段 ==========
    /**
     * 问卷结果ID
     */
    private Long questionnaireResultId;

    /**
     * 问卷ID
     */
    private Long questionnaireId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 测评任务编号
     */
    private String assessmentTaskNo;

    /**
     * 答题数据(JSON格式)
     */
    private String answers;

    /**
     * 完成时间
     */
    private Date completedTime;

    // ========== 问卷表字段 ==========
    /**
     * 问卷名称
     */
    private String questionnaireName;

    /**
     * 问卷描述
     */
    private String questionnaireDescription;

    /**
     * 问卷类型
     */
    private Integer questionnaireType;

    // ========== 维度结果表字段 ==========
    /**
     * 维度结果ID
     */
    private Long dimensionResultId;

    /**
     * 维度得分
     */
    private BigDecimal score;

    /**
     * 是否异常
     */
    private Integer isAbnormal;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 等级描述
     */
    private String level;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 学生评语
     */
    private String studentComment;

    // ========== 问卷维度表字段 ==========
    /**
     * 维度ID
     */
    private Long dimensionId;

    /**
     * 维度名称
     */
    private String dimensionName;

    /**
     * 维度编码
     */
    private String dimensionCode;

    /**
     * 维度描述
     */
    private String dimensionDescription;

    /**
     * 维度排序
     */
    private Integer sortOrder;

    /**
     * 是否参与模块计算
     */
    private Integer participateModuleCalc;

    /**
     * 是否参与测评计算
     */
    private Integer participateAssessmentCalc;

    /**
     * 是否参与心理问题排行
     */
    private Integer participateRanking;
}
