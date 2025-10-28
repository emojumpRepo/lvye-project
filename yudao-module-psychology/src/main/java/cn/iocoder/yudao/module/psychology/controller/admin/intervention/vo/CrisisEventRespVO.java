package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 危机事件 Response VO")
@Data
public class CrisisEventRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "事件编号", example = "RPT_2024_12345")
    private String eventId;

    @Schema(description = "学生档案ID", example = "1")
    private Long studentProfileId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学生学号", example = "2021001")
    private String studentNumber;

    @Schema(description = "班级名称", example = "高一(1)班")
    private String className;

    @Schema(description = "学生用户ID", example = "1")
    private Long studentUserId;

    @Schema(description = "事件标题")
    private String title;

    @Schema(description = "事件描述")
    private String description;

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "处理人ID", example = "1")
    private Long handlerUserId;

    @Schema(description = "处理人姓名", example = "李老师")
    private String handlerName;

    @Schema(description = "来源类型", example = "1")
    private Integer sourceType;

    @Schema(description = "上报人ID", example = "1")
    private Long reporterUserId;

    @Schema(description = "上报人姓名", example = "王老师")
    private String reporterName;

    @Schema(description = "上报时间")
    private LocalDateTime reportedAt;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "事发地点")
    private String location;

    @Schema(description = "处理方式", example = "1")
    private Integer processMethod;

    @Schema(description = "处理原因说明")
    private String processReason;

    @Schema(description = "结案总结")
    private String closureSummary;

    @Schema(description = "处理进度百分比", example = "50")
    private Integer progress;

    @Schema(description = "处理状态", example = "1")
    private Integer processStatus;

    @Schema(description = "是否自动分配", example = "false")
    private Boolean autoAssigned;

    @Schema(description = "处理时间")
    private LocalDateTime handleAt;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "处理历史记录")
    private List<ProcessHistoryVO> processHistory;

    @Schema(description = "最新评估")
    private LatestAssessmentVO latestAssessment;

    @Schema(description = "评估记录列表（按创建时间倒序）")
    private List<AssessmentRecordVO> latestAssessments;

    @Schema(description = "所有评估记录列表（按创建时间倒序）")
    private List<AssessmentRecordVO> allAssessmentRecords;

    @Schema(description = "正在进行的测评任务（未完成状态，包含未开始和进行中）")
    private PendingAssessmentTaskVO pendingAssessmentTask;

    @Schema(description = "测评任务列表（该危机事件关联的所有测评任务）")
    private List<AssessmentTaskVO> assessmentTasks;

    @Data
    public static class ProcessHistoryVO {
        @Schema(description = "处理记录ID")
        private Long id;

        @Schema(description = "危机事件ID")
        private Long eventId;

        @Schema(description = "操作时间")
        private LocalDateTime operateTime;

        @Schema(description = "操作人姓名")
        private String operatorName;

        @Schema(description = "操作类型")
        private String action;

        @Schema(description = "操作内容")
        private String content;

        @Schema(description = "操作原因")
        private String reason;

        @Schema(description = "附件ID列表")
        private List<Long> attachmentIds;

        @Schema(description = "评估记录ID")
        private Long assessmentId;

        @Schema(description = "测评结果ID")
        private Long taskResultId;
    }

    @Data
    public static class LatestAssessmentVO {
        @Schema(description = "评估时间")
        private LocalDateTime assessTime;

        @Schema(description = "风险等级")
        private Integer riskLevel;

        @Schema(description = "问题类型")
        private List<String> problemTypes;

        @Schema(description = "后续建议")
        private Integer followUpSuggestion;
    }

    @Data
    public static class AssessmentRecordVO {
        @Schema(description = "评估ID")
        private Long id;

        @Schema(description = "评估人ID")
        private Long assessorUserId;

        @Schema(description = "评估人姓名")
        private String assessorName;

        @Schema(description = "评估类型（1-阶段性评估、2-最终评估）")
        private Integer assessmentType;

        @Schema(description = "风险等级")
        private Integer riskLevel;

        @Schema(description = "风险等级名称")
        private String riskLevelName;

        @Schema(description = "问题类型识别")
        private List<String> problemTypes;

        @Schema(description = "后续建议")
        private Integer followUpSuggestion;

        @Schema(description = "后续建议名称")
        private String followUpSuggestionName;

        @Schema(description = "评估详细内容")
        private String content;

        @Schema(description = "附件ID列表")
        private List<Long> attachments;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;
    }

    @Data
    public static class PendingAssessmentTaskVO {
        @Schema(description = "任务ID")
        private Long taskId;

        @Schema(description = "任务编号")
        private String taskNo;

        @Schema(description = "任务名称")
        private String taskName;

        @Schema(description = "完成状态")
        private Integer status;
    }

    @Data
    public static class AssessmentTaskVO {
        @Schema(description = "任务ID")
        private Long taskId;

        @Schema(description = "任务编号")
        private String taskNo;

        @Schema(description = "任务名称")
        private String taskName;

        @Schema(description = "完成状态")
        private Integer status;

        @Schema(description = "风险等级")
        private Integer riskLevel;

        @Schema(description = "任务开始时间")
        private LocalDateTime startline;

        @Schema(description = "任务截止时间")
        private LocalDateTime deadline;

        @Schema(description = "提交时间")
        private LocalDateTime submitTime;
    }
}