package cn.iocoder.yudao.module.psychology.controller.web.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "学生家长端 - 测评参与 Request VO")
@Data
public class WebAssessmentParticipateReqVO {

    @Schema(description = "是否家长参与", example = "false")
    private Boolean isParent = false;

    @Schema(description = "答案列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "答案列表不能为空")
    private List<AssessmentAnswerItem> answers;

    @Schema(description = "答案项")
    @Data
    public static class AssessmentAnswerItem {

        @Schema(description = "题目索引", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer questionIndex;

        @Schema(description = "答案内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "A")
        private String answer;

        @Schema(description = "得分", example = "5")
        private Integer score;
    }

}