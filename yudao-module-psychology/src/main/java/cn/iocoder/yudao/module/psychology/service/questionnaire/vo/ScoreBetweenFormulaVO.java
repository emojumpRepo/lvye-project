package cn.iocoder.yudao.module.psychology.service.questionnaire.vo;

import lombok.Data;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:分数区间计算公式实体
 * @Version: 1.0
 */
@Data
public class ScoreBetweenFormulaVO {

    /**
     * 最小分数
     */
    private int minScore;

    /**
     * 最大分数
     */
    private int maxScore;

}
