package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-26
 * @Description:问卷任务参与人员列表
 * @Version: 1.0
 */
@Schema(description = "问卷任务参与人员列表")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireUserPageVO extends PageParam {

    @Schema(description = "任务编号", example = "123")
    @NotEmpty
    private String taskNo;

    @Schema(description = "问卷ID", example = "123")
    @NotEmpty
    private String questionnaireId;

    @Schema(description = "名称", example = "小明")
    private String name;

    @Schema(description = "学号", example = "123")
    private String studentNo;

}
