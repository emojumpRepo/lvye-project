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
    STUDENT(1),

    /**
     * 家长
     */
    PARENT(2),

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

}
