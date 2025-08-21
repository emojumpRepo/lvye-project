package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-22
 * @Description:学生班级报文
 * @Version: 1.0
 */
@Schema(description = "班级/年级学生数量报文")
@Data
public class StudentClassVO {

    /**
     * 班级/年级
     */
    @Schema(description = "班级/年级", example = "张三")
    private Long deptId;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "张三")
    private Long count;

}
