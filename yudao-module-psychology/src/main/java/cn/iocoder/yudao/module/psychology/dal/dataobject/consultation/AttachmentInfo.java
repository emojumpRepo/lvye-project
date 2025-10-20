package cn.iocoder.yudao.module.psychology.dal.dataobject.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 附件信息
 * 
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "附件ID")
    private Long id;

    @Schema(description = "文件名")
    private String name;

    @Schema(description = "文件URL")
    private String url;

    @Schema(description = "文件类型")
    private String type;
}

