package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结果生成状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ResultGenerationStatusEnum {

    PENDING(0, "待生成"),
    GENERATING(1, "生成中"),
    COMPLETED(2, "已生成"),
    FAILED(3, "生成失败");

    private final Integer status;
    private final String name;

    /**
     * 根据状态值获取枚举
     */
    public static ResultGenerationStatusEnum fromStatus(Integer status) {
        for (ResultGenerationStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        return null;
    }

}