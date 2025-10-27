package cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 危机事件处理历史 Response VO")
@Data
public class CrisisEventProcessHistoryVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "事件ID", example = "1")
    private Long eventId;
    
    @Schema(description = "测评任务编号", example = "1")
    private String taskNo;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorUserId;

    @Schema(description = "操作人姓名", example = "李老师")
    private String operatorName;

    @Schema(description = "操作类型", example = "ASSIGN_HANDLER")
    private String action;

    @Schema(description = "操作类型名称", example = "分配负责人")
    private String actionName;

    @Schema(description = "操作内容（如处理方式、评估内容等）")
    private String content;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "涉及用户ID（如新负责人ID）", example = "2")
    private Long relatedUserId;

    @Schema(description = "涉及用户姓名（如新负责人姓名）", example = "张老师")
    private String relatedUserName;

    @Schema(description = "原用户ID（如原负责人ID）", example = "3")
    private Long originalUserId;

    @Schema(description = "原用户姓名（如原负责人姓名）", example = "王老师")
    private String originalUserName;

    @Schema(description = "附件ID列表")
    private List<Long> attachments;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}