package com.lvye.mindtrip.module.psychology.service.assessment;

import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentResultDO;

/**
 * 基于场景的测评结果计算服务
 *
 * @author MinGoo
 */
public interface ScenarioBasedAssessmentResultService {

    /**
     * 计算测评结果
     * 
     * @param assessmentId 测评ID
     * @param scenarioId 场景ID
     * @param studentProfileId 学生档案ID
     * @param userId 用户ID
     * @return 测评结果
     */
    AssessmentResultDO calculateAssessmentResult(Long assessmentId, Long scenarioId, Long studentProfileId, Long userId, String taskNo);

    /**
     * 重新计算测评结果
     * 
     * @param assessmentResultId 测评结果ID
     * @return 更新后的测评结果
     */
    AssessmentResultDO recalculateAssessmentResult(Long assessmentResultId);

    /**
     * 为（包含当前问卷ID的）插槽生成模块结果：当该插槽内关联问卷均已完成时，执行模块规则并落库。
     * 若 currentQuestionnaireId 为空，则退化为检查全部插槽。
     */
    void generateModuleResultsForCompletedSlots(String taskNo, Long scenarioId, Long studentProfileId, Long userId, Long currentQuestionnaireId);

}
