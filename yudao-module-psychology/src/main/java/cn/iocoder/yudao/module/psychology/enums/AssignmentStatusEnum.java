package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 咨询师分配状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum AssignmentStatusEnum {

    ACTIVE(1, "有效"),
    INACTIVE(2, "已失效");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

    public static AssignmentStatusEnum valueOf(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }

}