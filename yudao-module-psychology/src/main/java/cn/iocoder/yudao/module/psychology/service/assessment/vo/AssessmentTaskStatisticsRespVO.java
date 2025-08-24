package cn.iocoder.yudao.module.psychology.service.assessment.vo;

import lombok.Data;

/**
 * 简化的测评任务统计响应 VO
 */
@Data
public class AssessmentTaskStatisticsRespVO {

    /**
     * 总参与人数
     */
    private Long totalParticipants = 0L;

    /**
     * 已完成人数
     */
    private Long completedParticipants = 0L;

    /**
     * 进行中人数
     */
    private Long inProgressParticipants = 0L;

    /**
     * 未开始人数
     */
    private Long notStartedParticipants = 0L;

    /**
     * 完成率
     */
    private Double completionRate = 0.0;

}
