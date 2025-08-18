package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学生特殊标记 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 学生特殊标记 VO")
@Data
public class StudentSpecialMarkVO {

    @Schema(description = "标记值", example = "1")
    private String value;

    @Schema(description = "标记名称", example = "身体残疾")
    private String name;

    @Schema(description = "标记描述", example = "身体功能障碍、需要特殊照顾的学生")
    private String description;

    @Schema(description = "是否选中", example = "true")
    private Boolean checked;

    /**
     * 学生特殊标记批量操作请求 VO
     */
    @Schema(description = "学生特殊标记批量操作请求")
    @Data
    public static class BatchUpdateReqVO {

        @Schema(description = "学生档案ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> studentProfileIds;

        @Schema(description = "要添加的特殊标记值", example = "2,3")
        private String addMarks;

        @Schema(description = "要移除的特殊标记值", example = "1")
        private String removeMarks;

        @Schema(description = "操作类型：add-添加，remove-移除，set-设置", requiredMode = Schema.RequiredMode.REQUIRED)
        private String operation;
    }

    /**
     * 特殊标记统计 VO
     */
    @Schema(description = "特殊标记统计信息")
    @Data
    public static class StatisticsVO {

        @Schema(description = "标记名称", example = "身体残疾")
        private String markName;

        @Schema(description = "标记值", example = "1")
        private String markValue;

        @Schema(description = "学生数量", example = "25")
        private Long studentCount;

        @Schema(description = "占比", example = "12.5")
        private Double percentage;
    }
}
