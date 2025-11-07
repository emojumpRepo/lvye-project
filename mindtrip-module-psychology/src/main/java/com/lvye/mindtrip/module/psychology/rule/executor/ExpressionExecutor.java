package com.lvye.mindtrip.module.psychology.rule.executor;

import com.lvye.mindtrip.module.psychology.rule.model.EvaluateContext;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * JSON 表达式执行器入口
 */
public interface ExpressionExecutor {

    /**
     * 评估表达式
     * @param expression JSON 表达式根节点
     * @param context 执行上下文
     * @return 评估结果（是否命中、产出载荷、调试信息）
     */
    EvaluateResult evaluate(JsonNode expression, EvaluateContext context);
}


