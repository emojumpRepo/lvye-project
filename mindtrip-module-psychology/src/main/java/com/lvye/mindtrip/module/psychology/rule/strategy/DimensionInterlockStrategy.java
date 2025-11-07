package com.lvye.mindtrip.module.psychology.rule.strategy;

import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 多维度联动策略
 * 基于各维度的级别，按联动表映射匹配输出结果
 *
 * @author MinGoo
 */
@Component
public class DimensionInterlockStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DimensionInterlockStrategy.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 计算多维度联动结果
     * 
     * JSON规则格式：
     * {
     *   "dimensionInterlock": {
     *     "selfDimension": "dimension_code_1",
     *     "otherDimensions": ["dimension_code_2", "dimension_code_3"],
     *     "interlockTable": [
     *       {
     *         "condition": {
     *           "selfLevel": "NONE",
     *           "otherLevelCounts": {"MILD": 0, "MODERATE": 0, "SEVERE": 0}
     *         },
     *         "result": {"level": "正常", "riskLevel": 1, "description": "未发现显著问题"}
     *       },
     *       {
     *         "condition": {
     *           "selfLevel": "MILD",
     *           "otherLevelCounts": {"MILD": {"min": 0, "max": 1}, "MODERATE": 0, "SEVERE": 0}
     *         },
     *         "result": {"level": "轻度关注", "riskLevel": 2, "description": "存在轻度问题"}
     *       }
     *     ]
     *   }
     * }
     */
    public EvaluateResult calculateAssessmentResult(AssessmentResultConfigDO config, 
                                                  List<DimensionResultDO> participatingDimensions) {
        try {
            // 解析JSON规则
            JsonNode expression = objectMapper.readTree(config.getCalculateFormula());
            
            // 检查是否为多维度联动规则
            if (!expression.has("dimensionInterlock")) {
                return EvaluateResult.notMatched();
            }
            
            JsonNode interlock = expression.get("dimensionInterlock");
            
            // 获取自身维度和其他维度配置
            String selfDimensionCode = interlock.get("selfDimension").asText();
            JsonNode otherDimensionsNode = interlock.get("otherDimensions");
            
            // 构建维度级别映射
            Map<String, String> dimensionLevels = participatingDimensions.stream()
                .filter(d -> d.getDimensionCode() != null && d.getLevel() != null)
                .collect(Collectors.toMap(
                    DimensionResultDO::getDimensionCode,
                    DimensionResultDO::getLevel,
                    (existing, replacement) -> existing // 如果有重复，保留第一个
                ));
            
            logger.info("多维度联动计算: 自身维度={}, 维度级别映射={}", selfDimensionCode, dimensionLevels);
            
            // 获取自身维度级别
            String selfLevel = dimensionLevels.get(selfDimensionCode);
            if (selfLevel == null) {
                logger.warn("未找到自身维度级别: dimensionCode={}", selfDimensionCode);
                return EvaluateResult.notMatched();
            }
            
            // 统计其他维度级别数量
            Map<String, Integer> otherLevelCounts = new HashMap<>();
            if (otherDimensionsNode != null && otherDimensionsNode.isArray()) {
                for (JsonNode otherDimNode : otherDimensionsNode) {
                    String otherDimCode = otherDimNode.asText();
                    String otherLevel = dimensionLevels.get(otherDimCode);
                    if (otherLevel != null) {
                        otherLevelCounts.put(otherLevel, otherLevelCounts.getOrDefault(otherLevel, 0) + 1);
                    }
                }
            }
            
            logger.info("其他维度级别统计: {}", otherLevelCounts);
            
            // 遍历联动表匹配条件
            JsonNode interlockTable = interlock.get("interlockTable");
            if (interlockTable != null && interlockTable.isArray()) {
                for (JsonNode tableRow : interlockTable) {
                    JsonNode condition = tableRow.get("condition");
                    JsonNode result = tableRow.get("result");
                    
                    if (matchCondition(condition, selfLevel, otherLevelCounts)) {
                        // 命中该条件
                        EvaluateResult evalResult = EvaluateResult.matched();
                        evalResult.getPayload().put("selfDimension", selfDimensionCode);
                        evalResult.getPayload().put("selfLevel", selfLevel);
                        evalResult.getPayload().put("otherLevelCounts", otherLevelCounts);
                        evalResult.getPayload().put("level", result.get("level").asText());
                        evalResult.getPayload().put("riskLevel", result.get("riskLevel").asInt());
                        evalResult.getPayload().put("description", result.get("description").asText());
                        
                        // 添加配置信息
                        evalResult.getPayload().put("configId", config.getId());
                        evalResult.getPayload().put("configLevel", config.getLevel());
                        evalResult.getPayload().put("configDescription", config.getDescription());
                        evalResult.getPayload().put("configSuggestions", config.getSuggestions());
                        evalResult.getPayload().put("configComment", config.getComment());
                        
                        logger.info("多维度联动规则命中: 自身维度={}({}), 其他维度统计={}, 结果等级={}, 配置ID={}", 
                            selfDimensionCode, selfLevel, otherLevelCounts, result.get("level").asText(), config.getId());
                        
                        return evalResult;
                    }
                }
            }
            
            logger.warn("多维度联动未命中任何条件: selfLevel={}, otherLevelCounts={}", selfLevel, otherLevelCounts);
            return EvaluateResult.notMatched();
            
        } catch (Exception e) {
            logger.error("多维度联动计算失败: 配置ID={}", config.getId(), e);
            return EvaluateResult.notMatched();
        }
    }
    
    /**
     * 匹配联动条件
     */
    private boolean matchCondition(JsonNode condition, String selfLevel, Map<String, Integer> otherLevelCounts) {
        try {
            // 检查自身维度级别
            String expectedSelfLevel = condition.get("selfLevel").asText();
            if (!selfLevel.equals(expectedSelfLevel)) {
                return false;
            }
            
            // 检查其他维度级别数量
            JsonNode expectedOtherLevelCounts = condition.get("otherLevelCounts");
            if (expectedOtherLevelCounts != null) {
                // 遍历期望的级别数量配置
                for (String levelName : (Iterable<String>) () -> expectedOtherLevelCounts.fieldNames()) {
                    JsonNode expectedCount = expectedOtherLevelCounts.get(levelName);
                    int actualCount = otherLevelCounts.getOrDefault(levelName, 0);
                    
                    if (expectedCount.isInt()) {
                        // 精确匹配
                        if (actualCount != expectedCount.asInt()) {
                            return false;
                        }
                    } else if (expectedCount.isObject()) {
                        // 范围匹配
                        int min = expectedCount.get("min").asInt();
                        int max = expectedCount.get("max").asInt();
                        if (actualCount < min || actualCount > max) {
                            return false;
                        }
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.error("条件匹配失败", e);
            return false;
        }
    }
}
