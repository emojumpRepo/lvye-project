package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;

/**
 * 测评参与 Service 接口
 */
public interface AssessmentParticipantService {

    /**
     * 开始参与测评
     *
     * @param taskNo 任务编号
     * @param userId 用户编号
     */
    void startAssessment(String taskNo, Long userId);

    /**
     * 提交测评答案
     *
     * @param taskNo 任务编号
     * @param userId 会员用户编号
     * @param participateReqVO 参与请求
     */
    void submitAssessment(String taskNo, Long userId, WebAssessmentParticipateReqVO participateReqVO);

    /**
     * 获取参与者状态
     *
     * @param taskNo 任务编号
     * @param userId 用户编号
     * @return 参与状态
     */
    Integer getParticipantStatus(String taskNo, Long userId);

}