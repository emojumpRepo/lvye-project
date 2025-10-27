package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.resultconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 模块结果配置新增/修改 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ModuleResultConfigSaveReqVO extends ModuleResultConfigBaseVO {

    @Schema(description = "配置ID", example = "1024")
    private Long id;
}


