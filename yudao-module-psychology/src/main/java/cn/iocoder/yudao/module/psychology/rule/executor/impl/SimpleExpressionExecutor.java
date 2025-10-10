package cn.iocoder.yudao.module.psychology.rule.executor.impl;

import cn.iocoder.yudao.module.psychology.rule.executor.ExpressionExecutor;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateContext;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.math.BigDecimal;
import java.util.*;

/**
 * 轻量 JSON 表达式执行器骨架
 */
@org.springframework.stereotype.Component
public class SimpleExpressionExecutor implements ExpressionExecutor {

    @Override
    public EvaluateResult evaluate(JsonNode expr, EvaluateContext ctx) {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> debug = new HashMap<>();
        boolean matched = eval(expr, ctx, payload, debug);
        if (matched && expr.has("result")) {
            applyOnMatch(expr.get("result"), ctx, payload, debug);
        }
        return matched ? EvaluateResult.matched().withPayload(payload) : EvaluateResult.notMatched().withPayload(payload);
    }

    private boolean eval(JsonNode node, EvaluateContext ctx, Map<String,Object> payload, Map<String,Object> debug) {
        if (node == null || node.isNull()) {
            System.out.println("[DEBUG] eval: node为null或isNull");
            return false;
        }
        
        System.out.println("[DEBUG] eval被调用: " + node);
        
        if (node.has("and")) {
            System.out.println("[DEBUG] 执行and操作");
            return evalAnd(node.get("and"), ctx, payload, debug);
        }
        if (node.has("or")) {
            System.out.println("[DEBUG] 执行or操作");
            return evalOr(node.get("or"), ctx, payload, debug);
        }
        if (node.has("cmp")) {
            System.out.println("[DEBUG] 执行cmp操作");
            return evalCmp(node.get("cmp"), ctx);
        }
        if (node.has("range")) {
            System.out.println("[DEBUG] 执行range操作");
            return evalRange(node.get("range"), ctx);
        }
        if (node.has("count")) {
            System.out.println("[DEBUG] 执行count操作");
            int countResult = evalCount(node.get("count"), ctx);
            boolean result = countResult > 0;
            System.out.println("[DEBUG] count结果: " + countResult + ", 返回: " + result);
            return result;
        }
        
        System.out.println("[DEBUG] eval: 没有匹配的操作类型");
        return false;
    }

    private boolean evalAnd(JsonNode arr, EvaluateContext ctx, Map<String,Object> payload, Map<String,Object> debug) {
        if (!(arr instanceof ArrayNode)) return false;
        for (JsonNode n : arr) {
            if (!eval(n, ctx, payload, debug)) return false;
        }
        return true;
    }

    private boolean evalOr(JsonNode arr, EvaluateContext ctx, Map<String,Object> payload, Map<String,Object> debug) {
        if (!(arr instanceof ArrayNode)) return false;
        for (JsonNode n : arr) {
            if (eval(n, ctx, payload, debug)) return true;
        }
        return false;
    }

    private boolean evalCmp(JsonNode cmp, EvaluateContext ctx) {
        Object lhs = evalValue(cmp.get("lhs"), ctx);
        String op = cmp.get("op").asText();
        JsonNode rhsNode = cmp.get("rhs");
        
        System.out.println("[DEBUG] evalCmp: lhs=" + lhs + " (类型:" + (lhs != null ? lhs.getClass().getSimpleName() : "null") + "), op=" + op);
        
        if ("in".equals(op) || "not_in".equals(op)) {
            List<Object> list = new ArrayList<>();
            if (rhsNode != null && rhsNode.isArray()) {
                for (JsonNode it : rhsNode) list.add(evalValue(it, ctx));
            }
            boolean contains = listContains(list, lhs);
            return "in".equals(op) ? contains : !contains;
        } else {
            Object rhs = evalValue(rhsNode, ctx);
            System.out.println("[DEBUG] evalCmp: rhs=" + rhs + " (类型:" + (rhs != null ? rhs.getClass().getSimpleName() : "null") + ")");
            
            boolean result = compare(lhs, op, rhs);
            System.out.println("[DEBUG] evalCmp结果: " + lhs + " " + op + " " + rhs + " = " + result);
            return result;
        }
    }

    private boolean evalRange(JsonNode range, EvaluateContext ctx) {
        System.out.println("[DEBUG] evalRange被调用: " + range.toString());
        
        // 兼容 "target" 和 "field" 两种格式
        JsonNode targetNode = range.get("target") != null ? range.get("target") : range.get("field");
        System.out.println("[DEBUG] targetNode: " + (targetNode != null ? targetNode.toString() : "null"));
        
        Object target = evalValue(targetNode, ctx);
        System.out.println("[DEBUG] target解析结果: " + target + " (类型: " + (target != null ? target.getClass().getSimpleName() : "null") + ")");
        
        BigDecimal min = toNumber(range.get("min"));
        BigDecimal max = toNumber(range.get("max"));
        BigDecimal val = toNumber(target);
        
        System.out.println("[DEBUG] 数值转换: val=" + val + ", min=" + min + ", max=" + max);
        
        if (val == null || min == null || max == null) {
            System.out.println("[DEBUG] 某个值为null，返回false");
            return false;
        }
        
        boolean result = val.compareTo(min) >= 0 && val.compareTo(max) <= 0;
        System.out.println("[DEBUG] 范围检查结果: " + val + " 在 [" + min + ", " + max + "] 范围内? " + result);
        
        return result;
    }

    private int evalCount(JsonNode count, EvaluateContext ctx) {
        System.out.println("[DEBUG] evalCount被调用: " + count);
        ArrayNode of = (ArrayNode) count.get("of");
        JsonNode when = count.get("when");
        int c = 0;
        if (of != null) {
            System.out.println("[DEBUG] 遍历题目列表，总数: " + of.size());
            for (JsonNode q : of) {
                String qn = q.asText();
                EvaluateContext sub = new EvaluateContext();
                sub.getVariables().putAll(ctx.getVariables());
                sub.getQuestionScoreMap().putAll(ctx.getQuestionScoreMap());
                sub.getQuestionOptionTextMap().putAll(ctx.getQuestionOptionTextMap());
                
                // 设置当前题目的相关变量
                sub.withVar("current.question", qn);
                BigDecimal currentScore = ctx.getQuestionScoreMap().getOrDefault(qn, BigDecimal.ZERO);
                sub.withVar("current.score", currentScore);
                
                System.out.println("[DEBUG] 检查题目: " + qn + ", 分数: " + currentScore);
                
                boolean matched = when == null || eval(when, sub, Collections.emptyMap(), Collections.emptyMap());
                System.out.println("[DEBUG] 题目 " + qn + " 条件匹配结果: " + matched);
                
                if (matched) c++;
            }
        }
        System.out.println("[DEBUG] count操作最终结果: " + c);
        return c;
    }

    private Object evalValue(JsonNode n, EvaluateContext ctx) {
        if (n == null) return null;
        if (n.has("sum")) {
            BigDecimal sum = BigDecimal.ZERO;
            for (JsonNode q : n.get("sum")) {
                sum = sum.add(scoreOf(q.asText(), ctx));
            }
            return sum;
        }
        if (n.has("count")) {
            System.out.println("[DEBUG] evalValue处理count操作");
            int countResult = evalCount(n.get("count"), ctx);
            System.out.println("[DEBUG] evalValue count结果: " + countResult);
            return new BigDecimal(countResult);
        }
        if (n.has("q")) {
            return scoreOf(n.get("q").asText(), ctx);
        }
        if (n.has("opt")) {
            return ctx.getQuestionOptionTextMap().getOrDefault(n.get("opt").asText(), null);
        }
        if (n.has("var")) {
            return ctx.getVariables().get(n.get("var").asText());
        }
        if (n.has("const")) {
            JsonNode v = n.get("const");
            if (v.isNumber()) return new BigDecimal(v.asText());
            if (v.isTextual()) return v.asText();
            if (v.isBoolean()) return v.asBoolean();
        }
        if (n.isNumber()) {
            BigDecimal result = new BigDecimal(n.asText());
            System.out.println("[DEBUG] evalValue处理数字: " + n.asText() + " -> " + result);
            return result;
        }
        if (n.isTextual()) {
            String text = n.asText();
            System.out.println("[DEBUG] evalValue处理文本: " + text);
            System.out.println("[DEBUG] 变量Map: " + ctx.getVariables());
            System.out.println("[DEBUG] 题目分数Map: " + ctx.getQuestionScoreMap());
            
            // 首先尝试从变量中获取
            if (ctx.getVariables().containsKey(text)) {
                Object value = ctx.getVariables().get(text);
                System.out.println("[DEBUG] 从变量中找到: " + text + " = " + value);
                return value;
            }
            // 然后尝试从题目分数中获取
            if (ctx.getQuestionScoreMap().containsKey(text)) {
                Object value = ctx.getQuestionScoreMap().get(text);
                System.out.println("[DEBUG] 从题目分数中找到: " + text + " = " + value);
                return value;
            }
            // 最后返回文本本身
            System.out.println("[DEBUG] 未找到变量，返回文本本身: " + text);
            return text;
        }
        if (n.isBoolean()) return n.asBoolean();
        return null;
    }

    private BigDecimal scoreOf(String q, EvaluateContext ctx) {
        return ctx.getQuestionScoreMap().getOrDefault(q, BigDecimal.ZERO);
    }

    private boolean compare(Object lhs, String op, Object rhs) {
        if (lhs == null || rhs == null) return false;
        if (lhs instanceof BigDecimal || rhs instanceof BigDecimal || isNumeric(lhs) || isNumeric(rhs)) {
            BigDecimal l = toNumber(lhs);
            BigDecimal r = toNumber(rhs);
            if (l == null || r == null) return false;
            int c = l.compareTo(r);
            switch (op) {
                case ">": return c > 0;
                case ">=": return c >= 0;
                case "<": return c < 0;
                case "<=": return c <= 0;
                case "=":
                case "==": return c == 0;
                case "!=": return c != 0;
                default: return false;
            }
        } else {
            String l = String.valueOf(lhs);
            String r = String.valueOf(rhs);
            switch (op) {
                case "=":
                case "==": return Objects.equals(l, r);
                case "!=": return !Objects.equals(l, r);
                default: return false;
            }
        }
    }

    private boolean listContains(List<Object> list, Object v) {
        for (Object o : list) {
            if (compare(o, "=", v)) return true;
        }
        return false;
    }

    private boolean isNumeric(Object o) {
        if (o == null) return false;
        if (o instanceof BigDecimal) return true;
        try { new BigDecimal(String.valueOf(o)); return true; } catch (Exception e) { return false; }
    }

    private BigDecimal toNumber(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        try { return new BigDecimal(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private void applyOnMatch(JsonNode resultNode, EvaluateContext ctx, Map<String,Object> payload, Map<String,Object> debug) {
        JsonNode onMatch = resultNode.get("on_match");
        if (onMatch == null) return;
        if (onMatch.has("once")) {
            Iterator<String> it = onMatch.get("once").fieldNames();
            while (it.hasNext()) {
                String k = it.next();
                payload.put(k, onMatch.get("once").get(k));
            }
        }
        if (onMatch.has("collect")) {
            for (JsonNode c : onMatch.get("collect")) {
                if (c.has("from")) collectFromQuestions(c, ctx, payload);
                // fromMatched 支持后续在 or 分支记录命中路径后实现
            }
        }
    }

    private void collectFromQuestions(JsonNode spec, EvaluateContext ctx, Map<String,Object> payload) {
        String as = spec.get("as").asText();
        List<Object> arr = (List<Object>) payload.computeIfAbsent(as, k -> new ArrayList<>());
        JsonNode from = spec.get("from");
        JsonNode where = spec.get("where");
        JsonNode select = spec.get("select");
        for (JsonNode q : from) {
            String qn = q.asText();
            EvaluateContext sub = new EvaluateContext();
            sub.getVariables().putAll(ctx.getVariables());
            sub.getQuestionScoreMap().putAll(ctx.getQuestionScoreMap());
            sub.getQuestionOptionTextMap().putAll(ctx.getQuestionOptionTextMap());
            sub.withVar("current.question", qn);
            boolean ok = where == null || eval(where, sub, Collections.emptyMap(), Collections.emptyMap());
            if (!ok) continue;
            String field = select.get("field").asText();
            Object value;
            if ("opt".equals(field)) {
                value = ctx.getQuestionOptionTextMap().get(qn);
            } else if ("score".equals(field)) {
                value = ctx.getQuestionScoreMap().get(qn);
            } else {
                value = null;
            }
            if (value != null) arr.add(value);
        }
    }
}


