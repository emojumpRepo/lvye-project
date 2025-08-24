package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-18
 * @Description:学生档案导入
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class StudentImportExcelVO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("出生日期")
    private String birthDate;

    @ExcelProperty("家庭住址")
    private String homeAddress;

    @ExcelProperty("性别")
    private String sex;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("年级")
    private String gradeName;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("家长")
    private String parentName;

    @ExcelProperty("关系")
    private String relation;

    @ExcelProperty("家长手机号码")
    private String parentMobile;

    @ExcelProperty("备注")
    private String remark;

}
