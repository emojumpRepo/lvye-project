package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 问卷结果导出 Request VO")
@Data
public class QuestionnaireResultExportReqVO {

    @Schema(description = "问卷ID列表", example = "[1,2,3]")
    private List<Long> questionnaireIds;

    @Schema(description = "学生档案ID列表", example = "[100,101,102]")
    private List<Long> studentProfileIds;

    @Schema(description = "风险等级列表", example = "[1,2,3]")
    private List<Integer> riskLevels;

    @Schema(description = "生成状态列表", example = "[2,3]")
    private List<Integer> generationStatuses;

    @Schema(description = "最小总分", example = "60.0")
    private Double minTotalScore;

    @Schema(description = "最大总分", example = "90.0")
    private Double maxTotalScore;

    @Schema(description = "提交时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] submitTimeRange;

    @Schema(description = "生成时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] generationTimeRange;

    @Schema(description = "导出格式", example = "excel")
    private String exportFormat = "excel";

    @Schema(description = "是否包含详细信息", example = "true")
    private Boolean includeDetails = true;

}