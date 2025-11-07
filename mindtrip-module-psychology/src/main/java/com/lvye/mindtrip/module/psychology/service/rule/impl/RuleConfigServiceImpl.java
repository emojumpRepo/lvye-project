package com.lvye.mindtrip.module.psychology.service.rule.impl;

import com.lvye.mindtrip.module.psychology.controller.admin.rule.vo.*;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.assessment.AssessmentResultConfigMapper;
import com.lvye.mindtrip.module.psychology.dal.mysql.assessment.ModuleResultConfigMapper;
import com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire.QuestionnaireResultConfigMapper;
import com.lvye.mindtrip.module.psychology.rule.executor.ExpressionExecutor;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateContext;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;
import com.lvye.mindtrip.module.psychology.service.rule.RuleConfigService;
import com.lvye.mindtrip.module.psychology.service.rule.RuleConverterService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 规则配置服务实现
 */
@Service
@Slf4j
public class RuleConfigServiceImpl implements RuleConfigService {

    @Resource
    private RuleConverterService ruleConverterService;
    
    @Resource
    private ExpressionExecutor expressionExecutor;

    @Resource
    private QuestionnaireResultConfigMapper questionnaireResultConfigMapper;
    
    @Resource
    private ModuleResultConfigMapper moduleResultConfigMapper;
    
    @Resource
    private AssessmentResultConfigMapper assessmentResultConfigMapper;

    @Override
    public List<RuleTemplateRespVO> getTemplates() {
        List<RuleTemplateRespVO> templates = new ArrayList<>();
        
        // 维度规则模板
        templates.addAll(getDimensionRuleTemplates());
        
        // 模块规则模板
        templates.addAll(getModuleRuleTemplates());
        
        // 测评规则模板
        templates.addAll(getAssessmentRuleTemplates());
        
        return templates;
    }

    @Override
    @Transactional
    public void saveDimensionRule(DimensionRuleConfigReqVO request) {
        try {
            // 转换为JSON表达式
            JsonNode jsonExpression = ruleConverterService.convertToJsonExpression(request);
            
            // 构建DO对象
            QuestionnaireResultConfigDO configDO = new QuestionnaireResultConfigDO();
            configDO.setDimensionId(request.getDimensionId());
            configDO.setCalculateFormula(jsonExpression.toString());
            configDO.setLevel(request.getOutput().getLevel());
            configDO.setIsAbnormal(booleanToInteger(request.getOutput().getIsAbnormal()));
            configDO.setTeacherComment(request.getOutput().getTeacherComment());
            
            // 处理学生评语JSON数组
            if (request.getOutput().getStudentComments() != null) {
                configDO.setStudentComment(request.getOutput().getStudentComments().toString());
            }
            
            configDO.setStatus(request.getEnabled() ? 1 : 0);
            configDO.setCreateTime(LocalDateTime.now());
            configDO.setUpdateTime(LocalDateTime.now());
            
            // 保存到数据库
            questionnaireResultConfigMapper.insert(configDO);
            
            log.info("维度规则保存成功: dimensionId={}, ruleName={}", request.getDimensionId(), request.getName());
        } catch (Exception e) {
            log.error("保存维度规则失败", e);
            throw new RuntimeException("保存维度规则失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void saveModuleRule(ModuleRuleConfigReqVO request) {
        try {
            // 转换为JSON表达式
            JsonNode jsonExpression = ruleConverterService.convertToJsonExpression(request);
            
            // 构建DO对象
            ModuleResultConfigDO configDO = new ModuleResultConfigDO();
            configDO.setScenarioSlotId(request.getScenarioSlotId());
            configDO.setConfigName(request.getName());
            configDO.setCalculateFormula(jsonExpression.toString());
            configDO.setDescription(request.getDescription());
            configDO.setLevel(request.getOutput().getLevel());
            configDO.setSuggestions(request.getOutput().getSuggestions());
            
            // 处理评语JSON数组
            if (request.getOutput().getStudentComments() != null) {
                configDO.setComments(request.getOutput().getStudentComments().toString());
            }
            
            configDO.setStatus(request.getEnabled() ? 1 : 0);
            configDO.setCreateTime(LocalDateTime.now());
            configDO.setUpdateTime(LocalDateTime.now());
            
            // 保存到数据库
            moduleResultConfigMapper.insert(configDO);
            
            log.info("模块规则保存成功: scenarioSlotId={}, ruleName={}", request.getScenarioSlotId(), request.getName());
        } catch (Exception e) {
            log.error("保存模块规则失败", e);
            throw new RuntimeException("保存模块规则失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void saveAssessmentRule(AssessmentRuleConfigReqVO request) {
        try {
            // 转换为JSON表达式
            JsonNode jsonExpression = ruleConverterService.convertToJsonExpression(request);
            
            // 构建DO对象
            AssessmentResultConfigDO configDO = new AssessmentResultConfigDO();
            configDO.setScenarioId(request.getScenarioId());
            configDO.setConfigName(request.getName());
            configDO.setCalculateFormula(jsonExpression.toString());
            configDO.setDescription(request.getDescription());
            configDO.setLevel(request.getOutput().getLevel());
            configDO.setSuggestions(request.getOutput().getSuggestions());
            configDO.setComment(request.getOutput().getTeacherComment());
            
            configDO.setStatus(request.getEnabled() ? 1 : 0);
            configDO.setCreateTime(LocalDateTime.now());
            configDO.setUpdateTime(LocalDateTime.now());
            
            // 保存到数据库
            assessmentResultConfigMapper.insert(configDO);
            
            log.info("测评规则保存成功: scenarioId={}, ruleName={}", request.getScenarioId(), request.getName());
        } catch (Exception e) {
            log.error("保存测评规则失败", e);
            throw new RuntimeException("保存测评规则失败: " + e.getMessage());
        }
    }

    @Override
    public RulePreviewRespVO previewRule(RulePreviewReqVO request) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 转换为JSON表达式
            JsonNode jsonExpression = ruleConverterService.convertToJsonExpression(request.getRuleConfig());
            
            // 构建评估上下文
            EvaluateContext context = buildEvaluateContext(request.getTestData());
            
            // 执行规则
            EvaluateResult result = expressionExecutor.evaluate(jsonExpression, context);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 构建响应
            RulePreviewRespVO response = new RulePreviewRespVO();
            response.setMatched(result.isMatched());
            response.setRuleName(request.getRuleConfig().getName());
            response.setJsonExpression(jsonExpression);
            response.setExecutionTime(executionTime);
            
            if (result.isMatched()) {
                RulePreviewRespVO.PreviewResult previewResult = new RulePreviewRespVO.PreviewResult();
                previewResult.setLevel(request.getRuleConfig().getOutput().getLevel());
                previewResult.setIsAbnormal(request.getRuleConfig().getOutput().getIsAbnormal());
                previewResult.setMessage(request.getRuleConfig().getOutput().getMessage());
                previewResult.setCollectedData(result.getPayload());
                response.setResult(previewResult);
            }
            
            return response;
        } catch (Exception e) {
            log.error("规则预览失败", e);
            throw new RuntimeException("规则预览失败: " + e.getMessage());
        }
    }

    @Override
    public RuleValidationRespVO validateRule(RuleValidationReqVO request) {
        try {
            RuleValidationRespVO response = new RuleValidationRespVO();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            // 基础校验
            RuleConfigBaseVO config = request.getRuleConfig();
            if (config.getName() == null || config.getName().trim().isEmpty()) {
                errors.add("规则名称不能为空");
            }
            
            if (config.getPriority() == null || config.getPriority() < 1 || config.getPriority() > 999) {
                errors.add("优先级必须在1-999之间");
            }
            
            // JSON表达式校验
            try {
                JsonNode jsonExpression = ruleConverterService.convertToJsonExpression(config);
                if (jsonExpression == null) {
                    errors.add("无法生成有效的JSON表达式");
                }
            } catch (Exception e) {
                errors.add("JSON表达式生成失败: " + e.getMessage());
            }
            
            // 规则冲突检测
            List<String> conflicts = checkRuleConflicts(request);
            warnings.addAll(conflicts);
            
            response.setValid(errors.isEmpty());
            response.setErrors(errors);
            response.setWarnings(warnings);
            
            return response;
        } catch (Exception e) {
            log.error("规则校验失败", e);
            throw new RuntimeException("规则校验失败: " + e.getMessage());
        }
    }

    @Override
    public List<DimensionRuleConfigRespVO> getDimensionRules(Long dimensionId) {
        // TODO: 实现获取维度规则配置列表
        return new ArrayList<>();
    }

    @Override
    public List<ModuleRuleConfigRespVO> getModuleRules(Long scenarioSlotId) {
        // TODO: 实现获取模块规则配置列表
        return new ArrayList<>();
    }

    @Override
    public List<AssessmentRuleConfigRespVO> getAssessmentRules(Long scenarioId) {
        // TODO: 实现获取测评规则配置列表
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void deleteDimensionRule(Long id) {
        questionnaireResultConfigMapper.deleteById(id);
        log.info("维度规则删除成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteModuleRule(Long id) {
        moduleResultConfigMapper.deleteById(id);
        log.info("模块规则删除成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteAssessmentRule(Long id) {
        assessmentResultConfigMapper.deleteById(id);
        log.info("测评规则删除成功: id={}", id);
    }

    /**
     * 获取维度规则模板
     */
    private List<RuleTemplateRespVO> getDimensionRuleTemplates() {
        List<RuleTemplateRespVO> templates = new ArrayList<>();
        
        // 简单阈值模板
        RuleTemplateRespVO simpleTemplate = new RuleTemplateRespVO();
        simpleTemplate.setTemplateId("dimension_simple_threshold");
        simpleTemplate.setTemplateName("简单阈值规则");
        simpleTemplate.setRuleType("simple_threshold");
        simpleTemplate.setDescription("单题目分数阈值判断");
        simpleTemplate.setApplicableScope("DIMENSION");
        templates.add(simpleTemplate);
        
        // 求和阈值模板
        RuleTemplateRespVO sumTemplate = new RuleTemplateRespVO();
        sumTemplate.setTemplateId("dimension_sum_threshold");
        sumTemplate.setTemplateName("求和阈值规则");
        sumTemplate.setRuleType("sum_threshold");
        sumTemplate.setDescription("多题目分数求和阈值判断");
        sumTemplate.setApplicableScope("DIMENSION");
        templates.add(sumTemplate);
        
        // 虐待分类模板
        RuleTemplateRespVO abuseTemplate = new RuleTemplateRespVO();
        abuseTemplate.setTemplateId("dimension_abuse_category");
        abuseTemplate.setTemplateName("虐待分类规则");
        abuseTemplate.setRuleType("abuse_category");
        abuseTemplate.setDescription("五类虐待分类检测规则");
        abuseTemplate.setApplicableScope("DIMENSION");
        templates.add(abuseTemplate);
        
        return templates;
    }

    /**
     * 获取模块规则模板
     */
    private List<RuleTemplateRespVO> getModuleRuleTemplates() {
        List<RuleTemplateRespVO> templates = new ArrayList<>();
        
        // 模块联动模板
        RuleTemplateRespVO interlockTemplate = new RuleTemplateRespVO();
        interlockTemplate.setTemplateId("module_interlock");
        interlockTemplate.setTemplateName("模块联动规则");
        interlockTemplate.setRuleType("module_interlock");
        interlockTemplate.setDescription("模块间结果联动判断");
        interlockTemplate.setApplicableScope("MODULE");
        templates.add(interlockTemplate);
        
        return templates;
    }

    /**
     * 获取测评规则模板
     */
    private List<RuleTemplateRespVO> getAssessmentRuleTemplates() {
        List<RuleTemplateRespVO> templates = new ArrayList<>();
        
        // 异常因子聚合模板
        RuleTemplateRespVO aggregationTemplate = new RuleTemplateRespVO();
        aggregationTemplate.setTemplateId("assessment_abnormal_aggregation");
        aggregationTemplate.setTemplateName("异常因子叠加规则");
        aggregationTemplate.setRuleType("abnormal_aggregation");
        aggregationTemplate.setDescription("基于异常维度数量的风险等级判断");
        aggregationTemplate.setApplicableScope("ASSESSMENT");
        templates.add(aggregationTemplate);
        
        return templates;
    }

    /**
     * 构建评估上下文
     */
    private EvaluateContext buildEvaluateContext(RulePreviewReqVO.PreviewTestData testData) {
        EvaluateContext context = new EvaluateContext();
        
        if (testData != null) {
            // 添加题目分数
            if (testData.getQuestionScores() != null) {
                for (Map.Entry<String, Object> entry : testData.getQuestionScores().entrySet()) {
                    context.withScore(entry.getKey(), new BigDecimal(entry.getValue().toString()));
                }
            }
            
            // 添加题目选项
            if (testData.getQuestionOptions() != null) {
                for (Map.Entry<String, String> entry : testData.getQuestionOptions().entrySet()) {
                    context.withOpt(entry.getKey(), entry.getValue());
                }
            }
            
            // 添加用户变量
            if (testData.getUserVariables() != null) {
                for (Map.Entry<String, Object> entry : testData.getUserVariables().entrySet()) {
                    context.withVar(entry.getKey(), entry.getValue());
                }
            }
        }
        
        return context;
    }

    /**
     * 检查规则冲突
     */
    private List<String> checkRuleConflicts(RuleValidationReqVO request) {
        List<String> conflicts = new ArrayList<>();
        
        // TODO: 实现规则冲突检测逻辑
        // 1. 检查同一维度下的规则优先级是否冲突
        // 2. 检查规则条件是否存在重叠
        // 3. 检查规则的互斥性
        
        return conflicts;
    }

    /**
     * Boolean转Integer的辅助方法
     */
    private Integer booleanToInteger(Boolean value) {
        if (value == null || !value) {
            return 0;
        }
        return 1;
    }
}
