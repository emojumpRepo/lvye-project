package com.lvye.mindtrip.module.system.enums.common;

import cn.hutool.core.util.ArrayUtil;
import com.lvye.mindtrip.framework.common.core.ArrayValuable;
import com.lvye.mindtrip.framework.common.enums.DateIntervalEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 性别的枚举值
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SexEnum implements ArrayValuable<String> {

    /** 男 */
    MALE(1),
    /** 女 */
    FEMALE(2),
    /* 未知 */
    UNKNOWN(0);

    /**
     * 性别
     */
    private final Integer sex;

    public static final String[] ARRAYS = Arrays.stream(values()).map(SexEnum::name).toArray(String[]::new);

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static Integer getName(String sex){
        return ArrayUtil.firstMatch(item -> item.name().equals(sex), SexEnum.values()).getSex();
    }

}
