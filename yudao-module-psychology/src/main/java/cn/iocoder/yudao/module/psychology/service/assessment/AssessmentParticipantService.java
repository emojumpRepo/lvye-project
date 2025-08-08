package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.web.assessment.vo.WebAssessmentParticipateReqVO;

/**
 * 测评参与 Service 接口
 */
public interface AssessmentParticipantService {

    /**
     * 开始参与测评
     *
     * @param taskId 任务编号
     * @param memberUserId 会员用户编号
     * @param isParent 是否家长参与
     */
    void startAssessment(Long taskId, Long memberUserId, Boolean isParent);

    /**
     * 提交测评答案
     *
     * @param taskId 任务编号
     * @param memberUserId 会员用户编号
     * @param participateReqVO 参与请求
     */
    void submitAssessment(Long taskId, Long memberUserId, WebAssessmentParticipateReqVO participateReqVO);

    /**
     * 获取参与者状态
     *
     * @param taskId 任务编号
     * @param memberUserId 会员用户编号
     * @return 参与状态
     */
    Integer getParticipantStatus(Long taskId, Long memberUserId);

}