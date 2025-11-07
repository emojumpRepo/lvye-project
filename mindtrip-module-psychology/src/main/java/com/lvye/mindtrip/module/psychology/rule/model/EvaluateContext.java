package com.lvye.mindtrip.module.psychology.rule.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行上下文
 */
public class EvaluateContext {
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, BigDecimal> questionScoreMap = new HashMap<>();
    private Map<String, String> questionOptionTextMap = new HashMap<>();
    private Map<String, Object> ext = new HashMap<>();

    public Map<String, Object> getVariables() { return variables; }
    public Map<String, BigDecimal> getQuestionScoreMap() { return questionScoreMap; }
    public Map<String, String> getQuestionOptionTextMap() { return questionOptionTextMap; }
    public Map<String, Object> getExt() { return ext; }

    public EvaluateContext withVar(String key, Object value) { this.variables.put(key, value); return this; }
    public EvaluateContext withScore(String q, BigDecimal v) { this.questionScoreMap.put(q, v); return this; }
    public EvaluateContext withOpt(String q, String t) { this.questionOptionTextMap.put(q, t); return this; }
    public EvaluateContext withExt(String k, Object v) { this.ext.put(k, v); return this; }
}


