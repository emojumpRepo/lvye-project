package com.lvye.mindtrip.module.psychology.service.questionnaire.vo;

import lombok.Data;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果试题
 * @Version: 1.0
 */
@Data
public class QuestionnaireResultVO {

    /**
     * 维度结果
     */
    private String dimensionName;

    /**
     * 得分
     */
    private int score;

    /**
     * 教师端评语
     */
    private String teacherComment;

    /**
     * 学生端评语
     */
    private String studentComment;

    /**
     * 是否异常
     */
    private Integer isAbnormal;

    /**
     * 等级：优秀、良好、一般、较差、很差
     */
    private String level;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 风险等级（1-无/低风险，2-轻度风险，3-中度风险，4-重度风险）
     */
    private Integer riskLevel;
}
