package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 问卷访问来源枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum QuestionnaireAccessSourceEnum {

    DIRECT_ACCESS(1, "直接访问"),
    ASSESSMENT_TASK(2, "测评任务"),
    RECOMMENDED_LINK(3, "推荐链接");

    private final Integer source;
    private final String name;

    /**
     * 根据来源值获取枚举
     */
    public static QuestionnaireAccessSourceEnum fromSource(Integer source) {
        for (QuestionnaireAccessSourceEnum sourceEnum : values()) {
            if (sourceEnum.getSource().equals(source)) {
                return sourceEnum;
            }
        }
        return null;
    }

}