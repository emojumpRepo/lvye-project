package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:学生监护人报文体
 * @Version: 1.0
 */
@Schema(description = "学生监护人报文体")
@Data
public class ParentContactVO {

    @Schema(description = "监护人ID", example = "张三")
    private Long id;

    @Schema(description = "姓名", example = "张三")
    @NotNull
    @Length(max = 60, message = "姓名不能超过 120")
    private String name;

    @Schema(description = "关系", example = "1")
    @NotNull
    @Length(max = 1, message = "关系不能超过 1")
    private int relation;

    @Schema(description = "手机号码", example = "137222222222")
    @NotNull
    @Length(max = 11, message = "手机号码不能 11")
    private String mobile;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "职业", example = "教师")
    @Length(max = 50, message = "职业不能超过 50 个字符")
    private String work;

    @Schema(description = "婚姻状态", example = "1")
    private Integer maritalStatus;

}
