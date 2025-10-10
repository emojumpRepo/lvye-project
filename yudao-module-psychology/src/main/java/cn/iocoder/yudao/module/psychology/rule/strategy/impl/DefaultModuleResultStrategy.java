package cn.iocoder.yudao.module.psychology.rule.strategy.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;
import cn.iocoder.yudao.module.psychology.rule.executor.ExpressionExecutor;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateContext;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateResult;
import cn.iocoder.yudao.module.psychology.rule.strategy.ModuleResultStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 默认模块结果计算策略
 */
@Slf4j
@Component
public class DefaultModuleResultStrategy implements ModuleResultStrategy {

    @Resource
    private ExpressionExecutor expressionExecutor;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public EvaluateResult calculateModuleResult(ModuleResultConfigDO config, EvaluateContext context) {
        try {
            // 解析JSON规则
            JsonNode expression = objectMapper.readTree(config.getCalculateFormula());
            
            // 执行表达式
            EvaluateResult result = expressionExecutor.evaluate(expression, context);
            
            // 如果命中规则，将配置信息加入到结果中
            if (result.isMatched()) {
                result.getPayload().put("configId", config.getId());
                result.getPayload().put("configLevel", config.getLevel());
                result.getPayload().put("configDescription", config.getDescription());
                result.getPayload().put("configSuggestions", config.getSuggestions());
                result.getPayload().put("configComments", config.getComments());
                
                log.info("模块规则命中：配置ID={}, 等级={}, 描述={}", 
                    config.getId(), config.getLevel(), config.getDescription());
            }
            
            return result;
        } catch (Exception e) {
            log.error("模块结果计算失败：配置ID={}", config.getId(), e);
            return EvaluateResult.notMatched();
        }
    }
}
