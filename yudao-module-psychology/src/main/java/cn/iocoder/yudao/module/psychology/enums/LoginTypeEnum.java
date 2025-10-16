package cn.iocoder.yudao.module.psychology.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-19
 * @Description:登录类型枚举
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum LoginTypeEnum  implements ArrayValuable<Integer> {

    /**
     * 学生
     */
    STUDENT(0),

    /**
     * 家长
     */
    PARENT(1),

    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(LoginTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;


    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static LoginTypeEnum valueOfType(Integer type) {
        return ArrayUtil.firstMatch(o -> o.getType().equals(type), values());
    }

    /**
     * 将 LoginTypeEnum 转换为 Boolean（用于测评参与）
     * @param loginType 登录类型枚举
     * @return true=家长，false=学生
     */
    public static Boolean toIsParent(LoginTypeEnum loginType) {
        if (loginType == null) {
            return false;
        }
        return loginType == PARENT;
    }

    /**
     * 将 LoginTypeEnum 的 type 值转换为 Boolean（用于测评参与）
     * @param type 登录类型值
     * @return true=家长，false=学生
     */
    public static Boolean toIsParent(Integer type) {
        if (type == null) {
            return false;
        }
        return type.equals(PARENT.getType());
    }

    /**
     * 将 Boolean 转换为 LoginTypeEnum（用于登录验证）
     * @param isParent true=家长，false=学生
     * @return 对应的登录类型枚举
     */
    public static LoginTypeEnum fromIsParent(Boolean isParent) {
        return Boolean.TRUE.equals(isParent) ? PARENT : STUDENT;
    }

    /**
     * 将 LoginTypeEnum 转换为 targetAudience/parentFlag 值
     * 现在所有值都统一了：0=学生，1=家长
     * @param loginType 登录类型枚举
     * @return 0=学生，1=家长（用于 targetAudience 和 parentFlag 字段）
     */
    public static Integer toTargetAudience(LoginTypeEnum loginType) {
        if (loginType == null) {
            return 0; // 默认为学生
        }
        return loginType.getType();
    }

    /**
     * 将 LoginTypeEnum 的 type 值转换为 targetAudience/parentFlag 值
     * 现在所有值都统一了：0=学生，1=家长
     * @param type 登录类型值（0=学生，1=家长）
     * @return 0=学生，1=家长（用于 targetAudience 和 parentFlag 字段）
     */
    public static Integer toTargetAudience(Integer type) {
        if (type == null) {
            return 0; // 默认为学生
        }
        return type;
    }

    /**
     * 将 targetAudience/parentFlag 值转换为 LoginTypeEnum
     * 现在所有值都统一了：0=学生，1=家长
     * @param targetAudience 目标对象值（0=学生，1=家长）
     * @return 对应的登录类型枚举
     */
    public static LoginTypeEnum fromTargetAudience(Integer targetAudience) {
        if (targetAudience == null) {
            return STUDENT; // 默认为学生
        }
        return targetAudience.equals(1) ? PARENT : STUDENT;
    }

    /**
     * 将 targetAudience/parentFlag 值转换为 Boolean
     * 现在所有值都统一了：0=学生，1=家长
     * @param targetAudience 目标对象值（0=学生，1=家长）
     * @return true=家长，false=学生
     */
    public static Boolean targetAudienceToIsParent(Integer targetAudience) {
        if (targetAudience == null) {
            return false; // 默认为学生
        }
        return targetAudience.equals(1);
    }

}
