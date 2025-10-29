package cn.iocoder.yudao.module.psychology.controller.admin.workspace.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 工作台数据分页查询 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 工作台数据分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkspaceDataPageReqVO extends PageParam {

    @Schema(description = "数据类型（TODAY_CONSULTATIONS-今日心理咨询任务，HIGH_RISK_STUDENTS-重点干预学生，PENDING_ALERTS-待处理预警事件）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "TODAY_CONSULTATIONS")
    @NotNull(message = "数据类型不能为空")
    private String type;

    @Schema(description = "咨询师用户ID（可选，用于过滤特定咨询师的数据）", example = "1")
    private Long counselorUserId;
}
