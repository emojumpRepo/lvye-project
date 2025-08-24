package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-18
 * @Description:学生时间线类型枚举
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum StudentTimelineEnum {

    INTERVIEW_RECORDS(1, "访谈记录"),
    STATUS_UPDATE(2, "状态变更"),
    COMMUNICATION_RECORD(3, "沟通记录"),
    INTERVENTION_EVENT(4, "干预时间"),
    EXAM_HISTORY(4, "测评历史"),
    INFORMATION_UPDATE(5, "个人信息更新")
    ;

    private final Integer level;
    private final String name;


}
