package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学生档案时间线事件类型枚举
 */
@Getter
@AllArgsConstructor
public enum TimelineEventTypeEnum {

    PROFILE_CREATED(1, "档案创建", "学生档案创建"),
    ASSESSMENT_COMPLETED(2, "测评完成", "完成心理测评"),
    CONSULTATION_RECORD(3, "咨询记录", "心理咨询记录"),
    CRISIS_INTERVENTION(4, "危机干预", "危机干预事件"),
    STATUS_CHANGE(5, "状态变更", "心理状态变更"),
    FAMILY_INFO_UPDATE(6, "家庭情况变更", "家庭情况信息更新"),
    GRADUATION(7, "毕业处理", "学生毕业相关处理"),
    QUICK_REPORT(8, "快速上报", "教师快速上报事件"),
    ASSESSMENT_REPORT(9, "评估报告", "提交评估报告"),
    STUDENT_PROFILE_ADJUSTMENT(10, "学生档案调整", "学生档案调整"),
    CRISIS_INTERVENTION_PLAN(11,"危机干预计划","危机干预计划");

    private final Integer type;
    private final String name;
    private final String description;

}