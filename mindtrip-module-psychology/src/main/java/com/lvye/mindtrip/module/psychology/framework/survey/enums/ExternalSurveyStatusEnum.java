package com.lvye.mindtrip.module.psychology.framework.survey.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 外部问卷系统状态枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum ExternalSurveyStatusEnum {

    NEW("new", 0, "草稿"),
    PUBLISHED("published", 1, "已发布"),
    PAUSING("pausing", 2, "已下线"),
    FINISHED("finished", 3, "已失效");

    /**
     * 外部系统状态值
     */
    private final String externalStatus;
    
    /**
     * 本地系统状态值
     */
    private final Integer localStatus;
    
    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据外部状态获取本地状态
     *
     * @param externalStatus 外部状态
     * @return 本地状态，如果找不到则返回草稿状态
     */
    public static Integer getLocalStatus(String externalStatus) {
        if (externalStatus == null) {
            return NEW.getLocalStatus();
        }
        
        for (ExternalSurveyStatusEnum statusEnum : values()) {
            if (statusEnum.getExternalStatus().equals(externalStatus)) {
                return statusEnum.getLocalStatus();
            }
        }
        
        // 默认返回草稿状态
        return NEW.getLocalStatus();
    }

    /**
     * 根据外部状态获取状态描述
     *
     * @param externalStatus 外部状态
     * @return 状态描述
     */
    public static String getDescription(String externalStatus) {
        if (externalStatus == null) {
            return NEW.getDescription();
        }
        
        for (ExternalSurveyStatusEnum statusEnum : values()) {
            if (statusEnum.getExternalStatus().equals(externalStatus)) {
                return statusEnum.getDescription();
            }
        }
        
        return NEW.getDescription();
    }

}
