package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结果生成器类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ResultGeneratorTypeEnum {

    SINGLE_QUESTIONNAIRE(1, "单问卷结果生成器"),
    COMBINED_ASSESSMENT(2, "组合测评结果生成器");

    private final Integer type;
    private final String name;

    /**
     * 根据类型值获取枚举
     */
    public static ResultGeneratorTypeEnum fromType(Integer type) {
        for (ResultGeneratorTypeEnum typeEnum : values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

}