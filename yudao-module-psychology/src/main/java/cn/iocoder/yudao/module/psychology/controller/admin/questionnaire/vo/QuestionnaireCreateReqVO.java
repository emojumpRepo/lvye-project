package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 问卷创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireCreateReqVO extends QuestionnaireBaseVO {

    // 继承父类 QuestionnaireBaseVO 的所有字段，无需重复定义

}