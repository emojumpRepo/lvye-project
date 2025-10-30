package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 正在进行的测评任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OngoingTaskPageReqVO extends PageParam {

    @Schema(description = "任务名称（模糊搜索）", example = "2024春季心理测评")
    private String taskName;

    @Schema(description = "发布人名称（模糊搜索）", example = "张老师")
    private String publishUser;
}
