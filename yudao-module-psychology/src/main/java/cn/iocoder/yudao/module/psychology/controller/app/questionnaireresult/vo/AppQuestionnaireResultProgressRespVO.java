package cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "学生端 - 问卷结果生成进度 Response VO")
@Data
public class AppQuestionnaireResultProgressRespVO {

    @Schema(description = "结果编号", example = "2001")
    private Long resultId;

    @Schema(description = "问卷编号", example = "1024")
    private Long questionnaireId;

    @Schema(description = "问卷标题", example = "心理健康测评问卷")
    private String questionnaireTitle;

    @Schema(description = "结果状态", example = "1")
    private Integer resultStatus; // 1-生成中, 2-生成成功, 3-生成失败

    @Schema(description = "结果状态描述", example = "正在生成结果")
    private String resultStatusDesc;

    @Schema(description = "生成进度", example = "75")
    private Integer progress; // 0-100

    @Schema(description = "当前步骤", example = "正在计算各维度得分...")
    private String currentStep;

    @Schema(description = "开始时间", example = "2024-01-01 12:00:00")
    private LocalDateTime startTime;

    @Schema(description = "预计完成时间", example = "2024-01-01 12:05:00")
    private LocalDateTime estimatedCompleteTime;

    @Schema(description = "实际完成时间", example = "2024-01-01 12:04:30")
    private LocalDateTime actualCompleteTime;

    @Schema(description = "错误信息", example = "")
    private String errorMessage;

    @Schema(description = "是否完成", example = "false")
    private Boolean isCompleted;

    @Schema(description = "是否成功", example = "true")
    private Boolean isSuccess;

    @Schema(description = "结果查看链接", example = "/psychology/app/questionnaire-result/2001")
    private String resultViewUrl;

}