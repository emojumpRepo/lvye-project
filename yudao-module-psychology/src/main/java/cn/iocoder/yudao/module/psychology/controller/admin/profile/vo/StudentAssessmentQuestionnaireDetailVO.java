package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-25
 * @Description:学生测评问卷详情
 * @Version: 1.0
 */
@Data
public class StudentAssessmentQuestionnaireDetailVO {

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


}
