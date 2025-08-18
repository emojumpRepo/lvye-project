package cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "学生端 - 问卷答案提交 Response VO")
@Data
public class AppQuestionnaireAnswerSubmitRespVO {

    @Schema(description = "问卷结果ID", example = "2001")
    private Long resultId;

    @Schema(description = "问卷编号", example = "1024")
    private Long questionnaireId;

    @Schema(description = "问卷标题", example = "心理健康测评问卷")
    private String questionnaireTitle;

    @Schema(description = "提交时间", example = "2024-01-01 12:00:00")
    private LocalDateTime submitTime;

    @Schema(description = "结果生成状态", example = "1")
    private Integer resultStatus; // 1-生成中, 2-生成成功, 3-生成失败

    @Schema(description = "结果生成进度", example = "50")
    private Integer resultProgress; // 0-100

    @Schema(description = "状态消息", example = "答案提交成功，正在生成结果...")
    private String statusMessage;

    @Schema(description = "是否需要等待结果", example = "true")
    private Boolean needWaitResult;

    @Schema(description = "预计完成时间", example = "2024-01-01 12:05:00")
    private LocalDateTime estimatedCompleteTime;

    @Schema(description = "结果查看链接", example = "/psychology/app/questionnaire-result/2001")
    private String resultViewUrl;

}