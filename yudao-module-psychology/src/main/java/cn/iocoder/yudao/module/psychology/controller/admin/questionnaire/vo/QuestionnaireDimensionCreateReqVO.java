package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷维度创建请求VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问卷维度创建请求")
public class QuestionnaireDimensionCreateReqVO extends QuestionnaireDimensionBaseVO {

}
