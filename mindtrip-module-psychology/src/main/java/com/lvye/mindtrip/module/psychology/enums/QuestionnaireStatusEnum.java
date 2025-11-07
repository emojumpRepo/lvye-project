package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 问卷状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum QuestionnaireStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    PAUSED(2, "已暂停"),
    CLOSED(3, "已关闭");

    private final Integer status;
    private final String name;

    /**
     * 根据状态值获取枚举
     */
    public static QuestionnaireStatusEnum fromStatus(Integer status) {
        for (QuestionnaireStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        return null;
    }

}