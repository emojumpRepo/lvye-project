package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.resultconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模块结果配置响应VO")
public class ModuleResultConfigRespVO extends ModuleResultConfigBaseVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}


