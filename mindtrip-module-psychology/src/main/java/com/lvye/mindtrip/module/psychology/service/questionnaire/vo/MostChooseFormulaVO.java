package com.lvye.mindtrip.module.psychology.service.questionnaire.vo;

import lombok.Data;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:最多选择计算公式试题
 * @Version: 1.0
 */
@Data
public class MostChooseFormulaVO {

    /**
     * 题目分数
     */
    private int questionScore;

    /**
     * 出现次数
     */
    private int chooseCount;

}
