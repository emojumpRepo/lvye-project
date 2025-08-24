package cn.iocoder.yudao.module.psychology.service.assessment.vo;

import lombok.Data;
import java.util.List;

/**
 * 简化的测评任务参与者请求 VO
 */
@Data
public class AssessmentTaskParticipantsReqVO {

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 用户ID列表
     */
    private List<Long> userIds;

}
