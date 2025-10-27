package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学生档案简化信息 VO
 * 用于搜索接口返回，只包含基本信息
 */
@Data
public class StudentProfileSimpleVO {

    @Schema(description = "学生档案编号", example = "1024")
    private Long id;

    @Schema(description = "学号", example = "2024001")
    private String studentNo;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "年级名称", example = "高一")
    private String gradeName;

    @Schema(description = "班级名称", example = "高一(1)班")
    private String className;

    @Schema(description = "年级部门编号", example = "1")
    private Long gradeDeptId;

    @Schema(description = "班级部门编号", example = "2")
    private Long classDeptId;

}
