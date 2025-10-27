package cn.iocoder.yudao.module.infra.controller.admin.file.vo.file;

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
            example = "https://test.yudao.iocoder.cn/xxx.jpg")
    private String url;

    @Schema(description = "文件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "image/jpeg")
    private String type;

}

