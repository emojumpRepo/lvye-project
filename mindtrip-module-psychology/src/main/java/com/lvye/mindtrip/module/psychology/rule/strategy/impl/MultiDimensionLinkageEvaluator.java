package com.lvye.mindtrip.module.psychology.rule.strategy.impl;

import com.lvye.mindtrip.module.psychology.rule.executor.impl.SimpleExpressionExecutor;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateContext;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 通用多维度联动规则评估器
 * 仅支持新版 multi_linkage 格式：
 * {"strategy":"multi_linkage","multiDimensionV2":{ "branches":[...], "branchLogic":"or|and", ... }}
 * 分支内部采用 SimpleExpressionExecutor 的 and/or/cmp 语义进行判断
 */
public class MultiDimensionLinkageEvaluator {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SimpleExpressionExecutor executor = new SimpleExpressionExecutor();

    public EvaluateResult evaluate(String ruleJson, EvaluateContext context) {
        try {
            JsonNode root = mapper.readTree(ruleJson);
            if (!root.has("strategy") || !"multi_linkage".equals(root.get("strategy").asText())) {
                return EvaluateResult.notMatched();
            }
            JsonNode v2 = root.get("multiDimensionV2");
            if (v2 == null || !v2.isObject()) return EvaluateResult.notMatched();

            boolean orLogic = !v2.has("branchLogic") || "or".equalsIgnoreCase(v2.get("branchLogic").asText());
            JsonNode branches = v2.get("branches");
            if (branches == null || !branches.isArray()) return EvaluateResult.notMatched();

            EvaluateResult last = null;
            boolean allMatched = true;
            for (JsonNode branch : branches) {
                EvaluateResult r = executor.evaluate(branch, context);
                boolean matched = r != null && r.isMatched();
                if (orLogic) {
                    if (matched) {
                        JsonNode res = branch.get("result");
                        return buildFromResult(r, res);
                    }
                } else {
                    if (!matched) {
                        allMatched = false;
                        break;
                    } else {
                        last = r;
                    }
                }
            }
            if (!orLogic && allMatched) {
                JsonNode res = branches.get(branches.size() - 1).get("result");
                return buildFromResult(last, res);
            }
            return EvaluateResult.notMatched();
        } catch (Exception e) {
            return EvaluateResult.notMatched();
        }
    }

    private EvaluateResult buildFromResult(EvaluateResult base, JsonNode result) {
        EvaluateResult out = EvaluateResult.matched();
        if (base != null && base.getPayload() != null) {
            out.getPayload().putAll(base.getPayload());
        }
        if (result != null) {
            if (result.has("level")) out.getPayload().put("level", result.get("level").asText());
            if (result.has("riskLevel")) out.getPayload().put("riskLevel", result.get("riskLevel").asInt());
            if (result.has("description")) out.getPayload().put("description", result.get("description").asText());
        }
        return out;
    }
}


