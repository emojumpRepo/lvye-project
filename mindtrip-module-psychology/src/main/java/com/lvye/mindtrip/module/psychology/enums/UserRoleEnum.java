package com.lvye.mindtrip.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    SYSTEM_ADMIN("system_admin", "系统管理员", "拥有最高权限，可查看所有数据"),
    GRADE_MANAGER("grade_manager", "年级管理员", "可查看其所管辖年级的全部师生数据"),
    CLASS_TEACHER("class_teacher", "班主任", "可查看自己所带班级的完整学生数据"),
    PSYCHOLOGY_TEACHER("psychology_teacher", "心理老师", "权限与其被分配管理的班级挂钩，是系统的主要使用者"),
    SUBJECT_TEACHER("subject_teacher", "任课老师", "默认几乎无数据查看权限，主要功能是使用快速上报");

    private final String code;
    private final String name;
    private final String description;

}