package cn.iocoder.yudao.module.psychology.service.assessment.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 简化的测评任务用户 VO
 */
@Data
public class AssessmentTaskUserVO {

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 完成状态 1-未开始 2-进行中 3-已完成
     */
    private Integer status;

    /**
     * 参与人员标识：0 学生；1 家长
     */
    private Integer parentFlag;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
