package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 问卷维度响应VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问卷维度响应")
public class QuestionnaireDimensionRespVO extends QuestionnaireDimensionBaseVO {

    @Schema(description = "维度ID", example = "1")
    private Long id;

    @Schema(description = "问卷名称", example = "CTQ童年创伤问卷")
    private String questionnaireName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否已删除")
    private Boolean deleted;
}
