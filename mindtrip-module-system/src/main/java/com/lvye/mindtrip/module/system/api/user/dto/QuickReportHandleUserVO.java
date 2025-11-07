package com.lvye.mindtrip.module.system.api.user.dto;

import lombok.Data;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-09-01
 * @Description:快速上报负责人选择列表
 * @Version: 1.0
 */
@Data
public class QuickReportHandleUserVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色
     */
    private String roleName;

    /**
     * 名称
     */
    private String name;

}
