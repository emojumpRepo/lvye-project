package cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "学生端 - 问卷答案提交 Request VO")
@Data
public class AppQuestionnaireAnswerSubmitReqVO {

    @Schema(description = "问卷编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "问卷编号不能为空")
    private Long questionnaireId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "访问记录ID", example = "1001")
    private Long accessId;

    @Schema(description = "答案数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "答案数据不能为空")
    @Size(min = 1, message = "答案数据不能为空")
    @Valid
    private List<QuestionAnswerVO> answers;

    @Schema(description = "提交来源", example = "1")
    private Integer submitSource = 1; // 1-移动端, 2-PC端, 3-小程序

    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;

    @Schema(description = "答题时长（秒）", example = "900")
    @Min(value = 1, message = "答题时长必须大于0")
    @Max(value = 86400, message = "答题时长不能超过24小时")
    private Integer answerDuration;

    @Schema(description = "备注", example = "学生自主完成")
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;

    @Schema(description = "问题答案")
    @Data
    public static class QuestionAnswerVO {

        @Schema(description = "问题编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "Q001")
        @NotNull(message = "问题编号不能为空")
        private String questionId;

        @Schema(description = "问题类型", example = "1")
        private Integer questionType; // 1-单选, 2-多选, 3-填空, 4-量表

        @Schema(description = "答案内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "A")
        @NotNull(message = "答案内容不能为空")
        @Size(max = 1000, message = "答案内容长度不能超过1000字符")
        private String answerContent;

        @Schema(description = "答案分值", example = "3")
        private Integer answerScore;

        @Schema(description = "答案权重", example = "1.0")
        private Double answerWeight = 1.0;

        @Schema(description = "是否跳题", example = "false")
        private Boolean isSkipped = false;

        @Schema(description = "答题时长（秒）", example = "30")
        @Min(value = 1, message = "单题答题时长必须大于0")
        @Max(value = 3600, message = "单题答题时长不能超过1小时")
        private Integer questionDuration;

    }

}