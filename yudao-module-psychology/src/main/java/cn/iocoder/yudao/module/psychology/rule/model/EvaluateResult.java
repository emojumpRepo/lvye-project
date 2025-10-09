package cn.iocoder.yudao.module.psychology.rule.model;

import java.util.HashMap;
import java.util.Map;

public class EvaluateResult {
    private boolean matched;
    private Map<String, Object> payload = new HashMap<>();
    private Map<String, Object> debug = new HashMap<>();

    public static EvaluateResult matched() {
        EvaluateResult r = new EvaluateResult();
        r.matched = true;
        return r;
    }

    public static EvaluateResult notMatched() {
        return new EvaluateResult();
    }

    public EvaluateResult withPayload(Map<String, Object> payload) {
        if (payload != null) this.payload.putAll(payload);
        return this;
    }

    public EvaluateResult withDebug(String k, Object v) {
        this.debug.put(k, v);
        return this;
    }

    public boolean isMatched() { return matched; }
    public Map<String, Object> getPayload() { return payload; }
    public Map<String, Object> getDebug() { return debug; }
}


