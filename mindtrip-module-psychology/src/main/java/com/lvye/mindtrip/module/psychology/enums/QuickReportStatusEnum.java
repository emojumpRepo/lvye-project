package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报状态枚举
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum QuickReportStatusEnum {

    PENDING(1, "待处理"),
    PROCESSING(2, "处理中"),
    RESOLVED(3, "已处理"),
    CLOSED(4, "已关闭");

    private final Integer status;
    private final String name;

}
