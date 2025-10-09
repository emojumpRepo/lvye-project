package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 问卷结果配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireResultConfigPageReqVO extends PageParam {

    @Schema(description = "维度ID", example = "1024")
    private Long dimensionId;

    @Schema(description = "计算类型", example = "1")
    private Integer calculateType;

    @Schema(description = "是否异常", example = "0")
    private Integer isAbnormal;

    @Schema(description = "风险等级：1-无/低风险，2-轻度风险，3-中度风险，4-重度风险", example = "1")
    private Integer riskLevel;

    @Schema(description = "等级：优秀、良好、一般、较差、很差", example = "优秀")
    private String level;

    @Schema(description = "描述关键词", example = "睡眠建议")
    private String description;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
