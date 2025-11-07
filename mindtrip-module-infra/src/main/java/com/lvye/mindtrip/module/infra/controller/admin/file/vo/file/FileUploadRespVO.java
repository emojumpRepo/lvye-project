package com.lvye.mindtrip.module.infra.controller.admin.file.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 文件上传 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRespVO {

    @Schema(description = "文件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long id;

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "头像.jpg")
    private String name;

    @Schema(description = "文件访问 URL", requiredMode = Schema.RequiredMode.REQUIRED, 
            example = "https://test.mindtrip.iocoder.cn/xxx.jpg")
    private String url;

    @Schema(description = "文件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "image/jpeg")
    private String type;

    @Schema(description = "文件路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxx.jpg")
    private String path;

    @Schema(description = "文件大小", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer size;

    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long configId;

}

