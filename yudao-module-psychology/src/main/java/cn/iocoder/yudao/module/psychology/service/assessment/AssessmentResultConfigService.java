package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultConfigDO;

import java.util.List;

/**
 * 测评结果配置服务
 *
 * @author MinGoo
 */
public interface AssessmentResultConfigService {

    /**
     * 创建测评结果配置
     */
    Long createAssessmentResultConfig(AssessmentResultConfigDO config);

    /**
     * 更新测评结果配置
     */
    void updateAssessmentResultConfig(AssessmentResultConfigDO config);

    /**
     * 删除测评结果配置
     */
    void deleteAssessmentResultConfig(Long id);

    /**
     * 获取测评结果配置
     */
    AssessmentResultConfigDO getAssessmentResultConfig(Long id);

    /**
     * 根据场景ID获取配置列表
     */
    List<AssessmentResultConfigDO> getAssessmentResultConfigsByScenarioId(Long scenarioId);

    /**
     * 根据场景ID和规则类型获取配置列表
     */
    List<AssessmentResultConfigDO> getAssessmentResultConfigsByScenarioIdAndRuleType(Long scenarioId, Integer ruleType);

    /**
     * 验证配置的JSON规则
     */
    boolean validateJsonRule(String calculateFormula);

}
