package cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报分页查询请求报文
 * @Version: 1.0
 */
@Schema(description = "管理后台 - 快速上报分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuickReportPageReqVO extends PageParam {

    /**
     * 当前用户ID
     */
    private Long userId;

    /**
     * 上报标题
     */
    @Schema(description = "上报标题", example = "上报标题")
    private String reportTitle;

    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名", example = "小明")
    private String studentName;

    /**
     * 上报人（教师）
     */
    @Schema(description = "上报人", example = "小明")
    private String reporter;

    /**
     * 处理人
     */
    @Schema(description = "上报人", example = "小明")
    private String handler;

    /**
     * 紧急程度：1-一般，2-关注，3-紧急，4-非常紧急
     */
    @Schema(description = "紧急程度", example = "1")
    private Integer urgencyLevel;

    /**
     * 处理状态：1-待处理，2-处理中，3-已处理，4-已关闭
     */
    @Schema(description = "处理状态", example = "1")
    private Integer status;

    /**
     * 是否需要跟进：1-需要，0-不需要
     */
    @Schema(description = "是否需要跟进", example = "1")
    private Integer followUpRequired;


}
