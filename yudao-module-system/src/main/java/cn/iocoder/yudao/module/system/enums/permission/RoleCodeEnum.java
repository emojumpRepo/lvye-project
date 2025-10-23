package cn.iocoder.yudao.module.system.enums.permission;

import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色标识枚举
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum {

    SUPER_ADMIN("super_admin", "超级管理员"),
    TENANT_ADMIN("tenant_admin", "系统管理员"),
    CRM_ADMIN("crm_admin", "CRM 管理员"), // CRM 系统专用
    GRADE_TEACHER("grade_teacher", "年级管理员"),
    PSYCHOLOGY_TEACHER("psychology_teacher", "心理老师"),
    HEAD_TEACHER("head_teacher", "班主任"),
    STUDENT("student", "学生");
    ;

    /**
     * 角色编码
     */
    private final String code;
    /**
     * 名字
     */
    private final String name;

    public static boolean isSuperAdmin(String code) {
        return ObjectUtils.equalsAny(code, SUPER_ADMIN.getCode());
    }

}
