package cn.iocoder.yudao.module.psychology.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:联系人枚举
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum ContactEnum {

    FATHER(1, "父亲"),
    MOTHER(2, "母亲"),
    GUARDIAN(3, "监护人");

    private final Integer code;
    private final String name;


    public static ContactEnum getCode(String name) {
        for (ContactEnum contactEnum : ContactEnum.values()) {
            if (contactEnum.getName().equals(name)) {
                return contactEnum;
            } else {
                return null;
            }
        }
        return null;
    }

}
