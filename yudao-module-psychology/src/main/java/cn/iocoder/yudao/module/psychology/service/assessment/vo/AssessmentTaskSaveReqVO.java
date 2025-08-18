package cn.iocoder.yudao.module.psychology.service.assessment.vo;

import lombok.Data;

/**
 * 简化的测评任务保存请求 VO
 */
@Data
public class AssessmentTaskSaveReqVO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 测评任务名称
     */
    private String taskName;

    /**
     * 量表编号
     */
    private String scaleCode;

    /**
     * 目标对象 1-学生，2-家长
     */
    private Integer targetAudience;

    /**
     * 状态
     */
    private Integer status;

}
