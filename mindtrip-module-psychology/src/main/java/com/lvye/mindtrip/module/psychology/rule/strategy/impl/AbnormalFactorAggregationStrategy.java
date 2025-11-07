package com.lvye.mindtrip.module.psychology.rule.strategy.impl;

import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentResultConfigDO;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateContext;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;
import com.lvye.mindtrip.module.psychology.rule.strategy.AssessmentResultStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 异常因子叠加策略
 */
@Slf4j
@Component
public class AbnormalFactorAggregationStrategy implements AssessmentResultStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public EvaluateResult calculateAssessmentResult(AssessmentResultConfigDO config, EvaluateContext context) {
        try {
            // 解析JSON规则
            JsonNode expression = objectMapper.readTree(config.getCalculateFormula());
            
            // 检查是否为异常因子叠加规则
            if (!expression.has("abnormalFactorAggregation")) {
                return EvaluateResult.notMatched();
            }
            
            JsonNode aggregation = expression.get("abnormalFactorAggregation");
            
            // 统计异常因子数量：优先从上下文变量获取（由上游已计算），不存在再回退其他来源
            int abnormalCount = 0;
            Object abnormalVar = context.getVariables().get("abnormalCount");
            if (abnormalVar instanceof Number) {
                abnormalCount = ((Number) abnormalVar).intValue();
            }
            
            // 根据阈值配置判断等级
            JsonNode thresholds = aggregation.get("thresholds");
            String resultLevel = null;
            Integer resultRiskLevel = null;
            String resultDescription = null;
            
            if (thresholds != null && thresholds.isArray()) {
                for (JsonNode threshold : thresholds) {
                    JsonNode range = threshold.get("range");
                    int min = range != null && range.has("min") ? range.get("min").asInt(Integer.MIN_VALUE) : Integer.MIN_VALUE;
                    int max = range != null && range.has("max") ? range.get("max").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE;

                    if (abnormalCount >= min && abnormalCount <= max) {
                        resultLevel = threshold.has("level") ? threshold.get("level").asText() : null;
                        resultRiskLevel = threshold.has("riskLevel") && threshold.get("riskLevel").isInt() ? threshold.get("riskLevel").asInt() : null;
                        resultDescription = threshold.has("description") ? threshold.get("description").asText() : null;
                        break;
                    }
                }
            }
            
            if (resultLevel != null) {
                EvaluateResult result = EvaluateResult.matched();
                result.getPayload().put("abnormalCount", abnormalCount);
                // 供上层直接设置 AssessmentResultDO 的关键字段
                result.getPayload().put("level", resultLevel);
                if (resultRiskLevel != null) {
                    result.getPayload().put("riskLevel", resultRiskLevel);
                }
                if (resultDescription != null) {
                    result.getPayload().put("description", resultDescription);
                }
                
                // 添加配置信息
                result.getPayload().put("configId", config.getId());
                result.getPayload().put("configLevel", config.getLevel());
                result.getPayload().put("configDescription", config.getDescription());
                result.getPayload().put("configSuggestions", config.getSuggestions());
                result.getPayload().put("configComment", config.getComment());
                
                log.info("异常因子叠加规则命中：异常数量={}, 命中等级={}, 命中风险={}, 配置ID={}", 
                    abnormalCount, resultLevel, resultRiskLevel, config.getId());
                
                return result;
            }
            
            return EvaluateResult.notMatched();
        } catch (Exception e) {
            log.error("异常因子叠加计算失败：配置ID={}", config.getId(), e);
            return EvaluateResult.notMatched();
        }
    }
}
