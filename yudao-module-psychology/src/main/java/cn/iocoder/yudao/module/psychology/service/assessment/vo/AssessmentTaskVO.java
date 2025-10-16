package cn.iocoder.yudao.module.psychology.service.assessment.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 简化的测评任务响应 VO
 */
@Data
public class AssessmentTaskVO {

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
     * 槽位-问卷分配（分页展示可选）
     */
    private java.util.List<cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.SlotAssignmentVO> assignments;

    /**
     * 目标对象 1-学生，2-家长
     */
    private Integer targetAudience;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 发布人管理员编号
     */
    private Long publishUserId;

    /**
     * 发布人姓名
     */
    private String publishUser;

    /**
     * 开始时间
     */
    private LocalDateTime startline;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 总人数
     */
    private Long totalNum;

    /**
     * 完成人数
     */
    private Long finishNum;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
