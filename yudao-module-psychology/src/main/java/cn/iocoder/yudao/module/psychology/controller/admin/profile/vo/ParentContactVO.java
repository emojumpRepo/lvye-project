package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
    private String name;

    @Schema(description = "关系", example = "1")
    @NotNull
    private int relation;

    @Schema(description = "手机号码", example = "137222222222")
    @NotNull
    private String mobile;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

}
