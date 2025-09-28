package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 危机干预完成状态枚举
 */
@AllArgsConstructor
@Getter
public enum CrisisCompletedStatusEnum {

    /**
     * 已解决
     */
    RESOLVED(1, "已解决"),

    /**
     * 持续关注
     */
    ONGOING(2, "持续关注");

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 根据状态值获取枚举
     */
    public static CrisisCompletedStatusEnum valueOf(Integer status) {
        for (CrisisCompletedStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        return null;
    }
}
