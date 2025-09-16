package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 问卷分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnairePageReqVO extends PageParam {

    @Schema(description = "问卷标题", example = "心理健康评估")
    private String title;

    @Schema(description = "问卷类型", example = "1")
    private Integer questionnaireType;

    @Schema(description = "目标对象", example = "1")
    private Integer targetAudience;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "是否开放", example = "1")
    private Integer isOpen;

    @Schema(description = "是否支持独立使用", example = "1")
    private Integer supportIndependentUse;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}