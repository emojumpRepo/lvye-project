package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 咨询师分配类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum AssignmentTypeEnum {

    PRIMARY(1, "主责咨询师"),
    TEMPORARY(2, "临时咨询师");

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 类型名
     */
    private final String name;

    public static AssignmentTypeEnum valueOf(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

}