package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 问卷类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum QuestionnaireTypeEnum {

    MENTAL_HEALTH(1, "心理健康"),
    LEARNING_ADAPTATION(2, "学习适应"),
    INTERPERSONAL_RELATIONSHIP(3, "人际关系"),
    EMOTION_MANAGEMENT(4, "情绪管理");

    private final Integer type;
    private final String name;

    /**
     * 根据类型值获取枚举
     */
    public static QuestionnaireTypeEnum fromType(Integer type) {
        for (QuestionnaireTypeEnum typeEnum : values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

}