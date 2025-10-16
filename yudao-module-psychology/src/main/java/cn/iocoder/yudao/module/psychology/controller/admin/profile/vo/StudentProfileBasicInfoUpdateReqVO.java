package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 学生档案基本信息更新 Request VO
 */
@Data
@Schema(description = "管理后台 - 学生档案基本信息更新 Request VO")
public class StudentProfileBasicInfoUpdateReqVO {

    @Schema(description = "学生档案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学生档案ID不能为空")
    private Long id;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "民族", example = "1")
    private Integer ethnicity;

    @Schema(description = "实际年龄（岁）", example = "16")
    private Integer actualAge;

    @Schema(description = "出生日期", example = "2008-05-20")
    private LocalDate birthDate;

    @Schema(description = "身高（厘米）", example = "175.50")
    private BigDecimal height;

    @Schema(description = "体重（千克）", example = "65.80")
    private BigDecimal weight;

    @Schema(description = "家中孩子情况")
    private FamilyChildrenInfoVO familyChildrenInfo;
}
