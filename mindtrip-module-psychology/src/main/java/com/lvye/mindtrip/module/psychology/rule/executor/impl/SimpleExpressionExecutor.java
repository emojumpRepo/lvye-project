package com.lvye.mindtrip.module.psychology.rule.executor.impl;

import com.lvye.mindtrip.module.psychology.rule.executor.ExpressionExecutor;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateContext;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            boolean ok = evalAnd(node.get("and"), ctx, payload, debug);
            if (ok && node.has("result")) mergeResultToPayload(node.get("result"), payload);
            return ok;
        }
        if (node.has("or")) {
            System.out.println("[DEBUG] 执行or操作");
            boolean ok = evalOr(node.get("or"), ctx, payload, debug);
            if (ok && node.has("result")) mergeResultToPayload(node.get("result"), payload);
            return ok;
        }
        if (node.has("cmp")) {
            System.out.println("[DEBUG] 执行cmp操作");
            boolean matched = evalCmp(node.get("cmp"), ctx);
            if (matched) {
                if (node.has("label")) {
                    recordLabelValue(node, node.get("cmp"), ctx, payload);
                }
                if (node.has("result")) mergeResultToPayload(node.get("result"), payload);
            }
            return matched;
        }
        if (node.has("range")) {
            System.out.println("[DEBUG] 执行range操作");
            boolean matched = evalRange(node.get("range"), ctx);
            if (matched) {
                if (node.has("label")) {
                    recordLabelValue(node, node.get("range"), ctx, payload);
                }
                if (node.has("result")) mergeResultToPayload(node.get("result"), payload);
            }
            return matched;
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
        boolean anyMatched = false;
        
        // 对于包含 label 的 or 规则，需要计算所有条件的值（即使某个已经命中）
        boolean hasAnyLabel = false;
        for (JsonNode n : arr) {
            if (n.has("label")) {
                hasAnyLabel = true;
                break;
            }
        }
        
        if (hasAnyLabel) {
            // 如果有任何条件带 label，则计算所有条件并记录所有 label 的值
            for (JsonNode n : arr) {
                boolean matched = eval(n, ctx, payload, debug);
                if (matched) {
                    anyMatched = true;
                }
                // 即使不匹配，如果有 label 也要记录其值
                if (n.has("label") && !matched) {
                    recordLabelValueAlways(n, ctx, payload);
                }
            }
            return anyMatched;
        } else {
            // 没有 label 的情况，保持原有逻辑：短路求值
            for (JsonNode n : arr) {
                if (eval(n, ctx, payload, debug)) return true;
            }
            return false;
        }
    }

    private boolean evalCmp(JsonNode cmp, EvaluateContext ctx) {
        JsonNode lhsNode = cmp.get("lhs") != null ? cmp.get("lhs") : cmp.get("left");
        JsonNode rhsNode = cmp.get("rhs") != null ? cmp.get("rhs") : cmp.get("right");
        Object lhs = evalValue(lhsNode, ctx);
        String op = normalizeOp(cmp.get("op").asText());
        
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

    /**
     * 当节点包含 label 且命中时，记录 label 以及 lhs/target 的求值（如 sum 的结果）到 payload.labels 数组中
     */
    @SuppressWarnings("unchecked")
    private void recordLabelValue(JsonNode node, JsonNode opNode, EvaluateContext ctx, Map<String,Object> payload) {
        try {
            String label = node.get("label").asText();
            Object value = null;
            Object threshold = null;

            // 支持从 cmp 的 lhs 或 range 的 target/field 取值
            if (node.has("cmp")) {
                JsonNode lhs = opNode.get("lhs") != null ? opNode.get("lhs") : opNode.get("left");
                if (lhs != null) {
                    value = evalValue(lhs, ctx);
                }
                // 提取边界值
                JsonNode rhs = opNode.get("rhs") != null ? opNode.get("rhs") : opNode.get("right");
                if (rhs != null) {
                    threshold = evalValue(rhs, ctx);
                }
            } else if (node.has("range")) {
                JsonNode targetNode = opNode.get("target") != null ? opNode.get("target") : opNode.get("field");
                if (targetNode != null) {
                    value = evalValue(targetNode, ctx);
                }
                // range 的边界值（优先取 min，其次 max）
                if (opNode.has("min")) {
                    threshold = evalValue(opNode.get("min"), ctx);
                } else if (opNode.has("max")) {
                    threshold = evalValue(opNode.get("max"), ctx);
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("label", label);
            if (value != null) item.put("value", value);
            if (threshold != null) item.put("threshold", threshold);
            item.put("matched", true); // 标记为已匹配

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> labels = (List<Map<String, Object>>) payload.computeIfAbsent("labels", k -> new ArrayList<Map<String, Object>>());
            labels.add(item);
            System.out.println("[DEBUG] 记录label结果(已匹配): " + item);
        } catch (Exception e) {
            System.out.println("[DEBUG] 记录label结果异常: " + e.getMessage());
        }
    }

    /**
     * 无论条件是否匹配，都记录带 label 的条件的计算值
     * 用于在 or 规则中收集所有 label 的分数
     */
    @SuppressWarnings("unchecked")
    private void recordLabelValueAlways(JsonNode node, EvaluateContext ctx, Map<String,Object> payload) {
        try {
            if (!node.has("label")) {
                return;
            }
            
            String label = node.get("label").asText();
            Object value = null;
            Object threshold = null;

            // 从 cmp 的 lhs 获取值
            if (node.has("cmp")) {
                JsonNode cmpNode = node.get("cmp");
                JsonNode lhs = cmpNode.get("lhs") != null ? cmpNode.get("lhs") : cmpNode.get("left");
                if (lhs != null) {
                    value = evalValue(lhs, ctx);
                }
                // 提取边界值
                JsonNode rhs = cmpNode.get("rhs") != null ? cmpNode.get("rhs") : cmpNode.get("right");
                if (rhs != null) {
                    threshold = evalValue(rhs, ctx);
                }
            } 
            // 从 range 的 target/field 获取值
            else if (node.has("range")) {
                JsonNode rangeNode = node.get("range");
                JsonNode targetNode = rangeNode.get("target") != null ? rangeNode.get("target") : rangeNode.get("field");
                if (targetNode != null) {
                    value = evalValue(targetNode, ctx);
                }
                // range 的边界值（优先取 min，其次 max）
                if (rangeNode.has("min")) {
                    threshold = evalValue(rangeNode.get("min"), ctx);
                } else if (rangeNode.has("max")) {
                    threshold = evalValue(rangeNode.get("max"), ctx);
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("label", label);
            if (value != null) item.put("value", value);
            if (threshold != null) item.put("threshold", threshold);
            item.put("matched", false); // 标记为未匹配

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> labels = (List<Map<String, Object>>) payload.computeIfAbsent("labels", k -> new ArrayList<Map<String, Object>>());
            labels.add(item);
            System.out.println("[DEBUG] 记录label结果(未匹配): " + item);
        } catch (Exception e) {
            System.out.println("[DEBUG] 记录label结果异常: " + e.getMessage());
        }
    }

    private String normalizeOp(String op) {
        if (op == null) return null;
        switch (op) {
            case "eq": return "==";
            case "ne": return "!=";
            case "gt": return ">";
            case "gte": return ">=";
            case "lt": return "<";
            case "lte": return "<=";
            default: return op; // 兼容原有 '==', '=', '!=', '>', '>=', '<', '<='
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
        // 支持按维度变量取值：{"dim":"code|main|other", "var":"riskLevel|riskLevel3Count|..."}
        if (n.has("dim")) {
            String dim = n.get("dim").asText();
            String var = n.has("var") ? n.get("var").asText() : null;
            if (var == null) return null;
            // main/other 特殊命名空间
            if ("main".equals(dim)) {
                Object v = ctx.getVariables().get("main." + var);
                if (v != null) return v;
            } else if ("other".equals(dim)) {
                Object v = ctx.getVariables().get("other." + var);
                if (v != null) return v;
            }
            // 一般维度：从变量 dimension_{code}_{var} 获取；若不存在，尝试 ext 中的维度结果
            String key = "dimension_" + dim + "_" + var;
            if (ctx.getVariables().containsKey(key)) {
                return ctx.getVariables().get(key);
            }
            Object slotMap = ctx.getExt().get("slotDimensionMap");
            if (slotMap instanceof java.util.Map) {
                Object dr = ((java.util.Map<?, ?>) slotMap).get(dim);
                if (dr instanceof com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.DimensionResultDO) {
                    com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.DimensionResultDO d = (com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.DimensionResultDO) dr;
                    if ("riskLevel".equals(var)) return d.getRiskLevel();
                    if ("level".equals(var)) return d.getLevel();
                    if ("score".equals(var)) return d.getScore();
                }
            }
            return null;
        }
        if (n.has("sum")) {
            BigDecimal sum = BigDecimal.ZERO;
            JsonNode sumNode = n.get("sum");
            if (sumNode.isArray()) {
                for (JsonNode q : sumNode) {
                    sum = sum.add(scoreOf(q.asText(), ctx));
                }
                return sum;
            } else if (sumNode.isObject()) {
                ArrayNode of = (ArrayNode) sumNode.get("of");
                JsonNode when = sumNode.get("when");
                if (of != null) {
                    for (JsonNode q : of) {
                        String qn = q.asText();
                        if (when != null) {
                            EvaluateContext sub = new EvaluateContext();
                            sub.getVariables().putAll(ctx.getVariables());
                            sub.getQuestionScoreMap().putAll(ctx.getQuestionScoreMap());
                            sub.getQuestionOptionTextMap().putAll(ctx.getQuestionOptionTextMap());
                            sub.withVar("current.question", qn);
                            if (!eval(when, sub, Collections.emptyMap(), Collections.emptyMap())) continue;
                        }
                        sum = sum.add(scoreOf(qn, ctx));
                    }
                }
                return sum;
            }
            return sum;
        }
        if (n.has("avg")) {
            // 支持两种写法：
            // 1) {"avg": ["Q1","Q2",...]}
            // 2) {"avg": {"of": ["Q1","Q2",...], "when": { ... 可选过滤条件 ... }}}
            JsonNode avgNode = n.get("avg");
            List<BigDecimal> values = new ArrayList<>();
            if (avgNode.isArray()) {
                for (JsonNode q : avgNode) {
                    values.add(scoreOf(q.asText(), ctx));
                }
            } else if (avgNode.isObject()) {
                ArrayNode of = (ArrayNode) avgNode.get("of");
                JsonNode when = avgNode.get("when");
                if (of != null) {
                    for (JsonNode q : of) {
                        String qn = q.asText();
                        if (when != null) {
                            EvaluateContext sub = new EvaluateContext();
                            sub.getVariables().putAll(ctx.getVariables());
                            sub.getQuestionScoreMap().putAll(ctx.getQuestionScoreMap());
                            sub.getQuestionOptionTextMap().putAll(ctx.getQuestionOptionTextMap());
                            sub.withVar("current.question", qn);
                            if (!eval(when, sub, Collections.emptyMap(), Collections.emptyMap())) continue;
                        }
                        values.add(scoreOf(qn, ctx));
                    }
                }
            }
            if (values.isEmpty()) return BigDecimal.ZERO;
            BigDecimal total = BigDecimal.ZERO;
            for (BigDecimal v : values) total = total.add(v);
            return total.divide(new BigDecimal(values.size()), 8, RoundingMode.HALF_UP);
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

    private void mergeResultToPayload(JsonNode resultNode, Map<String,Object> payload) {
        if (resultNode == null || !resultNode.isObject()) return;
        for (java.util.Iterator<String> it = resultNode.fieldNames(); it.hasNext();) {
            String k = it.next();
            JsonNode v = resultNode.get(k);
            if (v == null || v.isNull()) continue;
            if (v.isNumber()) payload.put(k, new java.math.BigDecimal(v.asText()));
            else if (v.isTextual()) payload.put(k, v.asText());
            else if (v.isBoolean()) payload.put(k, v.asBoolean());
            else payload.put(k, v.toString());
        }
    }
}


