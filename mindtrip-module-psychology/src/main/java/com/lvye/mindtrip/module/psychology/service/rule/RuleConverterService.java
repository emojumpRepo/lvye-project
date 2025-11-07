package com.lvye.mindtrip.module.psychology.service.rule;

import com.lvye.mindtrip.module.psychology.controller.admin.rule.vo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 规则转换服务 - 前端配置与JSON表达式互转
 */
@Service
@Slf4j
public class RuleConverterService {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 将前端配置转换为JSON表达式
     */
    public JsonNode convertToJsonExpression(RuleConfigBaseVO config) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            
            // 添加元数据
            ObjectNode meta = objectMapper.createObjectNode();
            meta.put("priority", config.getPriority());
            meta.put("stopOnMatch", true);
            meta.put("label", config.getName());
            if (config.getDescription() != null) {
                meta.put("description", config.getDescription());
            }
            root.set("meta", meta);

            // 根据规则类型转换主要逻辑
            RuleConfigBaseVO.RuleDetailConfig detailConfig = config.getConfig();
            switch (detailConfig.getType()) {
                case "simple_threshold":
                    convertSimpleThreshold(root, (SimpleThresholdConfig) detailConfig);
                    break;
                case "sum_threshold":
                    convertSumThreshold(root, (SumThresholdConfig) detailConfig);
                    break;
                case "option_match":
                    convertOptionMatch(root, (OptionMatchConfig) detailConfig);
                    break;
                case "collection_rule":
                    convertCollectionRule(root, (CollectionRuleConfig) detailConfig);
                    break;
                case "abuse_category":
                    convertAbuseCategory(root, (AbuseCategoryConfig) detailConfig);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的规则类型: " + detailConfig.getType());
            }

            // 添加输出配置
            addOutputConfig(root, config.getOutput());

            return root;
        } catch (Exception e) {
            log.error("规则配置转换JSON失败", e);
            throw new RuntimeException("规则配置转换失败: " + e.getMessage());
        }
    }

    /**
     * 简单阈值规则转换
     */
    private void convertSimpleThreshold(ObjectNode root, SimpleThresholdConfig config) {
        ObjectNode cmpNode = objectMapper.createObjectNode();
        
        // 左侧：题目引用
        ObjectNode lhs = objectMapper.createObjectNode();
        lhs.put("q", config.getQuestion());
        cmpNode.set("lhs", lhs);
        
        // 操作符
        cmpNode.put("op", config.getOperator());
        
        // 右侧：阈值
        cmpNode.put("rhs", config.getThreshold());
        
        root.set("cmp", cmpNode);
    }

    /**
     * 求和阈值规则转换
     */
    private void convertSumThreshold(ObjectNode root, SumThresholdConfig config) {
        ObjectNode cmpNode = objectMapper.createObjectNode();
        
        // 左侧：求和表达式
        ObjectNode lhs = objectMapper.createObjectNode();
        ArrayNode questionsArray = objectMapper.createArrayNode();
        config.getQuestions().forEach(questionsArray::add);
        lhs.set("sum", questionsArray);
        cmpNode.set("lhs", lhs);
        
        // 操作符
        cmpNode.put("op", config.getOperator());
        
        // 右侧：阈值
        cmpNode.put("rhs", config.getThreshold());
        
        // 如果有类别名称，添加结果数据
        if (config.getCategory() != null) {
            ObjectNode result = objectMapper.createObjectNode();
            result.put("category", config.getCategory());
            ArrayNode scoreOf = objectMapper.createArrayNode();
            config.getQuestions().forEach(scoreOf::add);
            result.set("scoreOf", scoreOf);
            cmpNode.set("result", result);
        }
        
        root.set("cmp", cmpNode);
    }

    /**
     * 选项匹配规则转换
     */
    private void convertOptionMatch(ObjectNode root, OptionMatchConfig config) {
        if (config.getConditions().size() == 1) {
            // 单个条件
            OptionMatchConfig.OptionCondition condition = config.getConditions().get(0);
            convertSingleCondition(root, condition);
        } else {
            // 多个条件用AND/OR连接
            ArrayNode conditionsArray = objectMapper.createArrayNode();
            
            for (OptionMatchConfig.OptionCondition condition : config.getConditions()) {
                ObjectNode conditionNode = objectMapper.createObjectNode();
                convertSingleCondition(conditionNode, condition);
                conditionsArray.add(conditionNode);
            }
            
            root.set(config.getLogic().toLowerCase(), conditionsArray);
        }
    }

    private void convertSingleCondition(ObjectNode parent, OptionMatchConfig.OptionCondition condition) {
        ObjectNode cmpNode = objectMapper.createObjectNode();
        
        ObjectNode lhs = objectMapper.createObjectNode();
        if ("contains".equals(condition.getOperator())) {
            lhs.put("opt", condition.getQuestion());
        } else {
            lhs.put("q", condition.getQuestion());
        }
        cmpNode.set("lhs", lhs);
        
        cmpNode.put("op", condition.getOperator());
        
        if (condition.getValue() instanceof String) {
            cmpNode.put("rhs", (String) condition.getValue());
        } else if (condition.getValue() instanceof Number) {
            cmpNode.put("rhs", ((Number) condition.getValue()).doubleValue());
        }
        
        parent.set("cmp", cmpNode);
    }

    /**
     * 收集规则转换
     */
    private void convertCollectionRule(ObjectNode root, CollectionRuleConfig config) {
        // 添加基础条件（如果有）
        if (config.getCollectCondition() != null && config.getCollectCondition().getScoreThreshold() != null) {
            // 这里可以添加收集条件的逻辑
            // 暂时简化，主要在result.on_match中处理收集逻辑
        }
        
        // 收集逻辑主要在输出配置中处理
        ObjectNode resultNode = objectMapper.createObjectNode();
        root.set("result", resultNode);
    }

    /**
     * 虐待分类规则转换（多条件OR组合）
     */
    private void convertAbuseCategory(ObjectNode root, AbuseCategoryConfig config) {
        ArrayNode orArray = objectMapper.createArrayNode();
        
        for (AbuseCategoryConfig.AbuseCategoryItem item : config.getCategories()) {
            if (!item.getEnabled()) continue;
            
            ObjectNode conditionNode = objectMapper.createObjectNode();
            ObjectNode cmpNode = objectMapper.createObjectNode();
            
            // 求和条件
            ObjectNode lhs = objectMapper.createObjectNode();
            ArrayNode questionsArray = objectMapper.createArrayNode();
            item.getQuestions().forEach(questionsArray::add);
            lhs.set("sum", questionsArray);
            cmpNode.set("lhs", lhs);
            
            cmpNode.put("op", ">=");
            cmpNode.put("rhs", item.getThreshold());
            
            // 添加类别结果
            ObjectNode result = objectMapper.createObjectNode();
            result.put("category", item.getName());
            ArrayNode scoreOf = objectMapper.createArrayNode();
            item.getQuestions().forEach(scoreOf::add);
            result.set("scoreOf", scoreOf);
            cmpNode.set("result", result);
            
            conditionNode.set("cmp", cmpNode);
            orArray.add(conditionNode);
        }
        
        root.set("or", orArray);
    }

    /**
     * 添加输出配置
     */
    private void addOutputConfig(ObjectNode root, RuleConfigBaseVO.RuleOutputConfig output) {
        ObjectNode resultNode = root.has("result") ? 
            (ObjectNode) root.get("result") : objectMapper.createObjectNode();
        
        // 基础输出字段
        if (output.getLevel() != null) {
            resultNode.put("level", output.getLevel());
        }
        if (output.getIsAbnormal() != null) {
            resultNode.put("isAbnormal", output.getIsAbnormal());
        }
        if (output.getMessage() != null) {
            resultNode.put("message", output.getMessage());
        }

        // 收集配置
        if (output.getCollections() != null && !output.getCollections().isEmpty()) {
            ObjectNode onMatchNode = objectMapper.createObjectNode();
            ArrayNode collectArray = objectMapper.createArrayNode();
            
            for (RuleConfigBaseVO.CollectionConfig collection : output.getCollections()) {
                ObjectNode collectNode = objectMapper.createObjectNode();
                collectNode.put("as", collection.getName());
                
                switch (collection.getType()) {
                    case "reasons":
                        // 原因收集：分数>=阈值的选项文本
                        ArrayNode fromArray = objectMapper.createArrayNode();
                        // 这里需要从具体规则配置中获取题目列表
                        collectNode.set("from", fromArray);
                        
                        ObjectNode whereNode = objectMapper.createObjectNode();
                        ObjectNode cmpNode = objectMapper.createObjectNode();
                        ObjectNode lhsNode = objectMapper.createObjectNode();
                        lhsNode.put("q", "$current");
                        cmpNode.set("lhs", lhsNode);
                        cmpNode.put("op", ">=");
                        cmpNode.put("rhs", 4); // 默认阈值，可配置化
                        whereNode.set("cmp", cmpNode);
                        collectNode.set("where", whereNode);
                        
                        ObjectNode selectNode = objectMapper.createObjectNode();
                        selectNode.put("field", "opt");
                        collectNode.set("select", selectNode);
                        break;
                        
                    case "categories":
                        // 类别收集：从匹配分支收集
                        collectNode.put("fromMatched", true);
                        ObjectNode mapNode = objectMapper.createObjectNode();
                        mapNode.put("name", "$category");
                        ObjectNode scoreNode = objectMapper.createObjectNode();
                        scoreNode.put("sum", "$scoreOf");
                        mapNode.set("score", scoreNode);
                        collectNode.set("map", mapNode);
                        break;
                }
                
                collectArray.add(collectNode);
            }
            
            onMatchNode.set("collect", collectArray);
            resultNode.set("on_match", onMatchNode);
        }
        
        root.set("result", resultNode);
    }

    /**
     * 将JSON表达式转换回前端配置（用于编辑时回显）
     */
    public RuleConfigBaseVO convertFromJsonExpression(JsonNode expression, String ruleType) {
        try {
            RuleConfigBaseVO config = new RuleConfigBaseVO();
            
            // 解析元数据
            if (expression.has("meta")) {
                JsonNode meta = expression.get("meta");
                config.setName(meta.path("label").asText());
                config.setDescription(meta.path("description").asText(null));
                config.setPriority(meta.path("priority").asInt(100));
            }
            
            config.setRuleType(ruleType);
            config.setEnabled(true);
            
            // 根据规则类型解析具体配置
            RuleConfigBaseVO.RuleDetailConfig detailConfig = parseDetailConfig(expression, ruleType);
            config.setConfig(detailConfig);
            
            // 解析输出配置
            RuleConfigBaseVO.RuleOutputConfig outputConfig = parseOutputConfig(expression);
            config.setOutput(outputConfig);
            
            return config;
        } catch (Exception e) {
            log.error("JSON表达式转换配置失败", e);
            throw new RuntimeException("JSON表达式转换失败: " + e.getMessage());
        }
    }

    private RuleConfigBaseVO.RuleDetailConfig parseDetailConfig(JsonNode expression, String ruleType) {
        switch (ruleType) {
            case "simple_threshold":
                return parseSimpleThreshold(expression);
            case "sum_threshold":
                return parseSumThreshold(expression);
            // 其他类型的解析...
            default:
                throw new IllegalArgumentException("不支持的规则类型: " + ruleType);
        }
    }

    private SimpleThresholdConfig parseSimpleThreshold(JsonNode expression) {
        SimpleThresholdConfig config = new SimpleThresholdConfig();
        
        if (expression.has("cmp")) {
            JsonNode cmp = expression.get("cmp");
            config.setQuestion(cmp.path("lhs").path("q").asText());
            config.setOperator(cmp.path("op").asText());
            config.setThreshold(cmp.path("rhs").decimalValue());
        }
        
        return config;
    }

    private SumThresholdConfig parseSumThreshold(JsonNode expression) {
        SumThresholdConfig config = new SumThresholdConfig();
        
        if (expression.has("cmp")) {
            JsonNode cmp = expression.get("cmp");
            
            // 解析题目列表
            JsonNode sumNode = cmp.path("lhs").path("sum");
            if (sumNode.isArray()) {
                List<String> questions = objectMapper.convertValue(sumNode, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                config.setQuestions(questions);
            }
            
            config.setOperator(cmp.path("op").asText());
            config.setThreshold(cmp.path("rhs").decimalValue());
            
            // 解析类别名称
            if (cmp.has("result")) {
                config.setCategory(cmp.path("result").path("category").asText(null));
            }
        }
        
        return config;
    }

    private RuleConfigBaseVO.RuleOutputConfig parseOutputConfig(JsonNode expression) {
        RuleConfigBaseVO.RuleOutputConfig outputConfig = new RuleConfigBaseVO.RuleOutputConfig();
        
        if (expression.has("result")) {
            JsonNode result = expression.get("result");
            outputConfig.setLevel(result.path("level").asText(null));
            outputConfig.setIsAbnormal(result.path("isAbnormal").isBoolean() ? result.path("isAbnormal").asBoolean() : null);
            outputConfig.setMessage(result.path("message").asText(null));
        }
        
        return outputConfig;
    }
}
