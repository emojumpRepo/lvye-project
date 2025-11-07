package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 参与者完成状态枚举
 */
@Getter
@AllArgsConstructor
public enum ParticipantCompletionStatusEnum {

    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成");

    private final Integer status;
    private final String name;

}