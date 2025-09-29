package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class QuestionnaireResultAsyncService {

    /**
     * 事务提交后异步执行计算逻辑
     */
    @Async
    public void calculateAfterCommit(Long questionnaireId, Long userId, Long questionnaireResultId,
                                     List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answers,
                                     Runnable updater) {
        try {
            // 由调用方在 updater 中完成计算与更新
            if (updater != null) {
                updater.run();
            }
        } catch (Exception e) {
            log.error("异步计算问卷结果失败, questionnaireId={}, userId={}, resultId={}", questionnaireId, userId, questionnaireResultId, e);
        }
    }
}


