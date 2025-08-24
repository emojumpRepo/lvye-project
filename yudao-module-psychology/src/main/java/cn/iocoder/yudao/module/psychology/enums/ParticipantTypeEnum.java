package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 参与者类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ParticipantTypeEnum {

    STUDENT(1, "学生本人"),
    PARENT(2, "家长代答");

    private final Integer type;
    private final String name;

    /**
     * 根据类型值获取枚举
     */
    public static ParticipantTypeEnum fromType(Integer type) {
        for (ParticipantTypeEnum typeEnum : values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

}