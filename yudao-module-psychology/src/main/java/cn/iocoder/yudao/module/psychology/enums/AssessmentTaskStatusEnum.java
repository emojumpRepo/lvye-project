package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 测评任务状态枚举
 */
@Getter
@AllArgsConstructor
public enum AssessmentTaskStatusEnum {

    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    CLOSED(3, "已关闭");

    private final Integer status;
    private final String name;

}