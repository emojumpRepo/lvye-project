package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 管理后台 - 问卷分页项 Response VO（包含 survey_code）
 */
@Schema(description = "管理后台 - 问卷分页 Response VO（包含 survey_code）")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireWithSurveyRespVO extends QuestionnaireRespVO {

    @Schema(description = "问卷编码（外部系统提供的编码）", example = "ABC123")
    @JsonProperty("survey_code")
    private String surveyCode;
}



