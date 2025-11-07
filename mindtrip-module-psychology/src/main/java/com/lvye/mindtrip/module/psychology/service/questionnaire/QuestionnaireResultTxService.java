package com.lvye.mindtrip.module.psychology.service.questionnaire;

import com.lvye.mindtrip.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionnaireResultTxService {

    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;

    /**
     * 使用新事务(REQUIRES_NEW)插入初始结果，确保 generation_status=1 立刻可见
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Long saveInitialResultNewTx(String taskNo, Long userId, Long questionnaireId,
                                       List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
        QuestionnaireResultDO resultDO = new QuestionnaireResultDO();
        resultDO.setAssessmentTaskNo(taskNo);
        resultDO.setUserId(userId);
        resultDO.setQuestionnaireId(questionnaireId);
        resultDO.setGenerationStatus(1); // 生成中
        if (answerList != null) {
            String result = com.alibaba.fastjson.JSON.toJSONString(answerList);
            resultDO.setAnswers(result);
        }
        questionnaireResultMapper.insert(resultDO);
        return resultDO.getId();
    }
}


