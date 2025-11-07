package com.lvye.mindtrip.module.psychology.controller.admin.common.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-18
 * @Description:教师导入
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class TeacherImportExcelVO {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("学号")
    private String jobNo;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("角色")
    private String role;

    @ExcelProperty("任课班级")
    private String className;

    @ExcelProperty("班主任班级")
    private String headTeacherClassName;

    @ExcelProperty("管理年级")
    private String manageGradeName;

}
