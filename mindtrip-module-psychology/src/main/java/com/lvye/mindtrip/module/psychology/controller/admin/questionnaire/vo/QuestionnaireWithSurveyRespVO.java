package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 管理后台 - 问卷分页项 Response VO（扩展版）
 * 说明：基类已包含 surveyCode 字段，这里不再重复定义，避免反射/序列化冲突
 */
@Schema(description = "管理后台 - 问卷分页 Response VO（扩展版）")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireWithSurveyRespVO extends QuestionnaireRespVO {
}
