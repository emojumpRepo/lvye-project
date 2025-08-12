package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:联系人美剧
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum ContactEnum {

    FATHER("1", "父亲"),
    MOTHER("2", "母亲"),
    GUARDIAN("3", "监护人");

    private final String code;
    private final String name;

}
