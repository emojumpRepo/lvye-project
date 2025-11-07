package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 危机干预分配模式枚举
 */
@Getter
@AllArgsConstructor
public enum InterventionAssignmentModeEnum {

    MANUAL("manual", "手动分配"),
    AUTO_PSYCHOLOGY("auto-psychology", "自动分配给心理老师"),
    AUTO_HEAD_TEACHER("auto-head-teacher", "自动分配给班主任");

    private final String mode;

    private final String name;

    public static InterventionAssignmentModeEnum getByMode(String mode) {
        for (InterventionAssignmentModeEnum value : values()) {
            if (value.getMode().equals(mode)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isValid(String mode) {
        return getByMode(mode) != null;
    }
}