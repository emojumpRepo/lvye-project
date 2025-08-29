package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学生档案信息完善情况 Response VO
 */
@Data
@Schema(description = "管理后台 - 学生档案信息完善情况 Response VO")
public class StudentProfileCompletenessRespVO {

    @Schema(description = "学生档案ID", example = "1")
    private Long id;

    @Schema(description = "学号", example = "2024001")
    private String studentNo;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "信息是否完善", example = "true")
    private Boolean isComplete;

    @Schema(description = "完善度百分比", example = "85")
    private Integer completenessPercentage;

    @Schema(description = "缺失的字段列表")
    private List<String> missingFields;

    @Schema(description = "各字段完善情况")
    private List<FieldCompletenessVO> fieldCompleteness;

    @Data
    @Schema(description = "字段完善情况")
    public static class FieldCompletenessVO {
        @Schema(description = "字段名称", example = "性别")
        private String fieldName;

        @Schema(description = "字段代码", example = "sex")
        private String fieldCode;

        @Schema(description = "是否已填写", example = "true")
        private Boolean isFilled;

        @Schema(description = "字段值", example = "1")
        private String fieldValue;

        @Schema(description = "是否必填", example = "true")
        private Boolean isRequired;
    }
}
