package cn.iocoder.yudao.module.psychology.controller.app.assessment.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "学生家长端 - 测评参与 Request VO")
@Data
public class WebAssessmentParticipateReqVO {

    @Schema(description = "用户ID", example = "123")
    private Long userId;

    @Schema(description = "任务编号", example = "123")
    private String taskNo;

    @Schema(description = "问卷ID", example = "123")
    private Long questionnaireId;

    @Schema(description = "答案列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "答案列表不能为空")
    @JsonDeserialize(using = AnswersDeserializer.class)
    private List<AssessmentAnswerItem> answers;

    @Schema(description = "答案项")
    @Data
    public static class AssessmentAnswerItem {

        @Schema(description = "题目内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private String title;

        @Schema(description = "题目索引", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer index;

        @Schema(description = "答案内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "A")
        private String answer;

        @Schema(description = "得分", example = "5")
        private Integer score;
    }

}