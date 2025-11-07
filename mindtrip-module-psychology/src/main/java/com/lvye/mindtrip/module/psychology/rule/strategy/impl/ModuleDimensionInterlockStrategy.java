package com.lvye.mindtrip.module.psychology.rule.strategy.impl;

import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateContext;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;
import com.lvye.mindtrip.module.psychology.rule.executor.impl.SimpleExpressionExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 模块多维度联动策略
 * 规则结构与测评维度联动一致，但作用域限定为模块（场景插槽）内的维度结果集合
 */
@Slf4j
@Component
public class ModuleDimensionInterlockStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluateResult calculateModuleResult(ModuleResultConfigDO config,
                                                List<DimensionResultDO> slotDimensions,
                                                EvaluateContext context) {
        try {
            JsonNode expression = objectMapper.readTree(config.getCalculateFormula());
            // 仅支持新的 {"strategy":"multi_linkage","multiDimensionV2":{...}}
            if (!(expression.has("strategy") && "multi_linkage".equals(expression.get("strategy").asText()))) {
                return EvaluateResult.notMatched();
            }
            JsonNode interlock = expression.get("multiDimensionV2");
            if (interlock == null) return EvaluateResult.notMatched();

            // 新版 multiDimensionV2：branches + branchLogic
            if (interlock.has("branches")) {
                boolean orLogic = !interlock.has("branchLogic") || "or".equalsIgnoreCase(interlock.get("branchLogic").asText());
                SimpleExpressionExecutor ex = new SimpleExpressionExecutor();
                boolean allMatched = true;
                JsonNode lastMatchedResult = null;
                for (JsonNode branch : interlock.get("branches")) {
                    EvaluateResult r = ex.evaluate(branch, context);
                    boolean matched = r != null && r.isMatched();
                    if (orLogic && matched) {
                        lastMatchedResult = branch.get("result");
                        // 命中即返回
                        return buildEvalFromResult(config, lastMatchedResult);
                    }
                    if (!orLogic && !matched) {
                        allMatched = false;
                        break;
                    } else if (!orLogic && matched) {
                        lastMatchedResult = branch.get("result");
                    }
                }
                if (!orLogic && allMatched) {
                    return buildEvalFromResult(config, lastMatchedResult);
                }
                return EvaluateResult.notMatched();
            }

            return EvaluateResult.notMatched();
        } catch (Exception e) {
            log.error("模块多维度联动计算失败: configId={}", config.getId(), e);
            return EvaluateResult.notMatched();
        }
    }

    private EvaluateResult buildEvalFromResult(ModuleResultConfigDO config, JsonNode result) {
        EvaluateResult eval = EvaluateResult.matched();
        if (result != null) {
            if (result.has("level")) eval.getPayload().put("level", result.get("level").asText());
            if (result.has("riskLevel")) eval.getPayload().put("riskLevel", result.get("riskLevel").asInt());
            if (result.has("description")) eval.getPayload().put("description", result.get("description").asText());
        }
        if (!eval.getPayload().containsKey("level") && config.getLevel() != null) eval.getPayload().put("level", config.getLevel());
        if (!eval.getPayload().containsKey("description") && config.getDescription() != null) eval.getPayload().put("description", config.getDescription());
        eval.getPayload().put("configId", config.getId());
        eval.getPayload().put("configLevel", config.getLevel());
        eval.getPayload().put("configDescription", config.getDescription());
        eval.getPayload().put("configSuggestions", config.getSuggestions());
        eval.getPayload().put("configComments", config.getComments());
        return eval;
    }
}


