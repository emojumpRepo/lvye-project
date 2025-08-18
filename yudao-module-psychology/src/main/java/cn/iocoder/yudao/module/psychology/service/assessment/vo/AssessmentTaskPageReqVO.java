package cn.iocoder.yudao.module.psychology.service.assessment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 简化的测评任务分页请求 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentTaskPageReqVO extends PageParam {

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 量表编号
     */
    private String scaleCode;

    /**
     * 目标对象
     */
    private Integer targetAudience;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;

}
