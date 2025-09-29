package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultDO;

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

}
