package com.lvye.mindtrip.module.psychology.controller.admin.intervention.vo;

import com.lvye.mindtrip.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 危机事件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrisisEventPageReqVO extends PageParam {

    @Schema(description = "学生档案ID", example = "1")
    private Long studentProfileId;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "学生学号", example = "2021001")
    private String studentNo;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "处理人ID（负责心理老师）")
    private Long counselorUserId;

    @Schema(description = "处理进度状态", example = "1")
    private Integer processStatus;

    @Schema(description = "事件来源类型", example = "1")
    private Integer sourceType;

    @Schema(description = "状态（字典：intervention_status）", example = "1")
    private Integer status;
}