package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 问卷结果分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireResultPageReqVO extends PageParam {

    @Schema(description = "问卷ID", example = "1")
    private Long questionnaireId;

    @Schema(description = "用户ID", example = "100")
    private Long userId;

    @Schema(description = "风险等级", example = "2")
    private Integer riskLevel;

    @Schema(description = "生成状态", example = "1")
    private Integer generationStatus;

    @Schema(description = "最小总分", example = "60.0")
    private Double minTotalScore;

    @Schema(description = "最大总分", example = "90.0")
    private Double maxTotalScore;

    @Schema(description = "提交时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] submitTime;

    @Schema(description = "生成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] generationTime;

}