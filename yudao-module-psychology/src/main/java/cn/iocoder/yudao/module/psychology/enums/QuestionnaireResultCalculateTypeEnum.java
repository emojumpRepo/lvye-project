package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果计算类型类聚
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum QuestionnaireResultCalculateTypeEnum {

    SCORE(1, "分数区间"),
    AGE_SEX_SCORE(2, "年龄性别与分数区间"),
    MOST_CHOOSE(3, "最多选择");

    private final Integer type;
    private final String desc;

}
