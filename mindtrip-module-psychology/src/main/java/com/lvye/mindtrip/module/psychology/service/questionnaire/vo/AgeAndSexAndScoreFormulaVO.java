package com.lvye.mindtrip.module.psychology.service.questionnaire.vo;

import lombok.Data;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:年龄性别与分数区间计算公式实体
 * @Version: 1.0
 */
@Data
public class AgeAndSexAndScoreFormulaVO {

    /**
     * 性别
     */
    private int sex;

    /**
     * 最小年龄
     */
    private int minAge;

    /**
     * 最大年龄
     */
    private int maxAge;

    /**
     * 最小分数
     */
    private int minScore;

    /**
     * 最大分数
     */
    private int maxScore;

}
