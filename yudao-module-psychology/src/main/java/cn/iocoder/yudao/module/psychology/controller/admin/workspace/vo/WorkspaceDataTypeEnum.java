package cn.iocoder.yudao.module.psychology.controller.admin.workspace.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作台数据类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum WorkspaceDataTypeEnum {

    /**
     * 今日心理咨询任务
     */
    TODAY_CONSULTATIONS("TODAY_CONSULTATIONS", "今日心理咨询任务"),

    /**
     * 重点干预学生
     */
    HIGH_RISK_STUDENTS("HIGH_RISK_STUDENTS", "重点干预学生"),

    /**
     * 待处理预警事件
     */
    PENDING_ALERTS("PENDING_ALERTS", "待处理预警事件");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 枚举值
     */
    public static WorkspaceDataTypeEnum fromCode(String code) {
        for (WorkspaceDataTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的工作台数据类型: " + code);
    }
}
