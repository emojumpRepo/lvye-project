package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 测评参与 Service 实现类
 */
@Service
@Validated
@Slf4j
public class AssessmentParticipantServiceImpl implements AssessmentParticipantService {

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startAssessment(String taskNo, Long memberUserId, Boolean isParent) {
        // 简化实现：跳过复杂验证，直接记录日志
        log.info("用户 {} 开始测评任务 {}, 是否家长: {}", memberUserId, taskNo, isParent);

        // TODO: 实现具体的开始测评逻辑
        // 当前为简化实现，后续可以添加具体的业务逻辑
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAssessment(Long taskId, Long memberUserId, WebAssessmentParticipateReqVO participateReqVO) {
        // 简化实现：跳过复杂验证，直接记录日志
        log.info("用户 {} 提交测评任务 {}, 答案数量: {}",
                memberUserId, taskId,
                participateReqVO.getAnswers() != null ? participateReqVO.getAnswers().size() : 0);

        // TODO: 实现具体的提交测评逻辑
        // 当前为简化实现，后续可以添加具体的业务逻辑
    }

    @Override
    public Integer getParticipantStatus(Long taskId, Long memberUserId) {
        // 简化实现：跳过复杂验证，直接返回默认状态
        log.info("查询用户 {} 在任务 {} 的参与状态", memberUserId, taskId);

        // TODO: 实现具体的状态查询逻辑
        // 当前为简化实现，默认返回未开始状态
        return 1; // 1-未开始状态
    }

}