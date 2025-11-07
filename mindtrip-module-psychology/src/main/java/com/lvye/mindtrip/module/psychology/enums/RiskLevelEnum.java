package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 风险等级枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum RiskLevelEnum {

    NORMAL(1, "正常", "心理状态良好，无需特别关注"),
    ATTENTION(2, "关注", "需要适当关注，建议定期观察"),
    WARNING(3, "预警", "存在一定风险，需要及时干预"),
    HIGH_RISK(4, "高危", "风险较高，需要立即采取干预措施");

    private final Integer level;
    private final String name;
    private final String description;

    /**
     * 根据等级值获取枚举
     */
    public static RiskLevelEnum fromLevel(Integer level) {
        for (RiskLevelEnum levelEnum : values()) {
            if (levelEnum.getLevel().equals(level)) {
                return levelEnum;
            }
        }
        return null;
    }

    /**
     * 根据代码获取枚举
     */
    public static RiskLevelEnum fromCode(Integer code) {
        return fromLevel(code);
    }

}