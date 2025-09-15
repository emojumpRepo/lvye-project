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

    @Schema(description = "操作人ID", example = "1")
    private Long operatorUserId;

    @Schema(description = "操作人姓名", example = "李老师")
    private String operatorName;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "操作内容")
    private String content;

    @Schema(description = "附件列表")
    private List<String> attachments;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}