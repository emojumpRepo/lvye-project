package com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:测评任务参与者请求报文
 * @Version: 1.0
 */
@Schema(description = "管理后台 - 测评任务参与者新增/删除 Request VO")
@Data
public class AssessmentTaskParticipantsReqVO {

    @Schema(description = "任务编号", example = "TASK2024001")
    private String taskNo;

    @Schema(description = "用户列表", example = "1,2,3,4")
    @Valid
    private List<Long> userIds;


}
