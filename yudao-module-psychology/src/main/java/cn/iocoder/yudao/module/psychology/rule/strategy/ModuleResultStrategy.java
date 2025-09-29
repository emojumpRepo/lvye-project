package cn.iocoder.yudao.module.psychology.rule.strategy;

import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateContext;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateResult;

/**
 * 模块结果计算策略
 */
public interface ModuleResultStrategy {
    
    /**
     * 计算模块结果
     * @param config 模块配置
     * @param context 执行上下文
     * @return 评估结果（包含计算结果和配置信息）
     */
    EvaluateResult calculateModuleResult(ModuleResultConfigDO config, EvaluateContext context);
}
