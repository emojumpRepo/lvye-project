package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果计算服务层
 * @Version: 1.0
 */
public interface QuestionnaireResultCalculateService {

    /**
     * 问卷结果计算
     * @param questionnaireId 问卷id
     * @param userId 用户id
     * @param answerList 答题列表
     * @return
     */
    List<QuestionnaireResultVO> resultCalculate(Long questionnaireId, Long userId, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList);

    /**
     * 问卷结果计算（带问卷结果ID，支持维度结果保存）
     * @param questionnaireId 问卷id
     * @param userId 用户id
     * @param questionnaireResultId 问卷结果ID
     * @param answerList 答题列表
     * @return
     */
    List<QuestionnaireResultVO> resultCalculate(Long questionnaireId, Long userId, Long questionnaireResultId, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList);
}
