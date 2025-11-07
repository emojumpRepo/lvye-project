package com.lvye.mindtrip.module.psychology.service.assessment.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 简化的学生家长端测评任务 VO
 */
@Data
public class WebAssessmentTaskVO {

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
     * 关联问卷 ID 列表
     */
    private java.util.List<Long> questionnaireIds;

    /**
     * 目标对象 1-学生，2-家长
     */
    private Integer targetAudience;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startline;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 是否已完成
     */
    private Boolean completed;

    /**
     * 完成进度
     */
    private Integer progress;

}
