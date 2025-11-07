package com.lvye.mindtrip.module.psychology.rule.strategy;

import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentResultConfigDO;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateContext;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;

/**
 * 测评结果计算策略
 */
public interface AssessmentResultStrategy {
    
    /**
     * 计算测评结果
     * @param config 测评配置
     * @param context 执行上下文
     * @return 评估结果（包含计算结果和配置信息）
     */
    EvaluateResult calculateAssessmentResult(AssessmentResultConfigDO config, EvaluateContext context);
}
