package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDimensionDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireDimensionService;
import cn.iocoder.yudao.module.psychology.rule.executor.ExpressionExecutor;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateContext;
import cn.iocoder.yudao.module.psychology.rule.model.EvaluateResult;
import cn.iocoder.yudao.module.psychology.enums.QuestionnaireResultCalculateTypeEnum;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.AgeAndSexAndScoreFormulaVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.MostChooseFormulaVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.ScoreBetweenFormulaVO;
import cn.iocoder.yudao.module.psychology.util.NumberUtils;
import cn.iocoder.yudao.module.psychology.util.StudentCommentUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果计算服务层
 * @Version: 1.0
 */
@Service
public class QuestionnaireResultCalculateServiceImpl implements QuestionnaireResultCalculateService{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String INDEX_ALL = "all";

    @Resource
    private QuestionnaireResultConfigService questionnaireResultConfigService;

    @Resource
    private StudentProfileMapper studentProfileMapper;
    
    @Resource
    private QuestionnaireDimensionService questionnaireDimensionService;
    
    @Resource
    private ExpressionExecutor expressionExecutor;
    
    @Resource
    private DimensionResultService dimensionResultService;

    @Override
    public List<QuestionnaireResultVO> resultCalculate(Long questionnaireId, Long userId, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
        // 调用带问卷结果ID的方法，问卷结果ID为null表示不保存维度结果
        return resultCalculate(questionnaireId, userId, null, answerList);
    }

    @Override
    public List<QuestionnaireResultVO> resultCalculate(Long questionnaireId, Long userId, Long questionnaireResultId, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
        List<QuestionnaireResultVO> resultList = new ArrayList<>();
        List<DimensionResultDO> dimensionResults = new ArrayList<>();
        
        logger.info("开始计算问卷结果: questionnaireId={}, userId={}, questionnaireResultId={}", questionnaireId, userId, questionnaireResultId);
        
        // 1. 获取学生档案信息
        StudentProfileVO studentProfile = studentProfileMapper.selectInfoByUserId(userId);
        if (studentProfile == null) {
            logger.warn("未找到用户档案: userId={}", userId);
            return resultList;
        }
        
        // 2. 获取问卷的所有维度
        List<QuestionnaireDimensionDO> dimensions = questionnaireDimensionService.getListByQuestionnaireId(questionnaireId);
        if (dimensions.isEmpty()) {
            logger.warn("问卷没有配置维度: questionnaireId={}", questionnaireId);
            return resultList;
        }
        
        logger.info("找到{}个维度需要计算", dimensions.size());
        
        // 3. 为每个维度计算结果
        for (QuestionnaireDimensionDO dimension : dimensions) {
            try {
                DimensionResultDO dimensionResult = calculateDimensionResult(dimension, answerList, studentProfile);
                if (dimensionResult != null) {
                    // 设置问卷结果ID和维度编码
                    dimensionResult.setQuestionnaireResultId(questionnaireResultId);
                    dimensionResult.setDimensionCode(dimension.getDimensionCode());
                    
                    // 如果有问卷结果ID，收集维度结果用于批量保存
                    if (questionnaireResultId != null) {
                        dimensionResults.add(dimensionResult);
                    }
                    
                    // 转换为旧的 QuestionnaireResultVO 格式（为了兼容现有接口）
                    QuestionnaireResultVO resultVO = convertToQuestionnaireResultVO(dimension, dimensionResult);
                    resultList.add(resultVO);
                }
            } catch (Exception e) {
                logger.error("维度计算失败: dimensionId={}, dimensionName={}", 
                    dimension.getId(), dimension.getDimensionName(), e);
            }
        }
        
        // 4. 批量保存维度结果到数据库
        if (questionnaireResultId != null && !dimensionResults.isEmpty()) {
            try {
                dimensionResultService.batchSaveDimensionResults(dimensionResults);
                logger.info("维度结果保存成功: questionnaireId={}, userId={}, questionnaireResultId={}, 保存{}个维度结果", 
                    questionnaireId, userId, questionnaireResultId, dimensionResults.size());
            } catch (Exception e) {
                logger.error("维度结果保存失败: questionnaireId={}, userId={}, questionnaireResultId={}", 
                    questionnaireId, userId, questionnaireResultId, e);
            }
        }
        
        logger.info("问卷结果计算完成: questionnaireId={}, 成功计算{}个维度结果", questionnaireId, resultList.size());
        return resultList;
    }

    /**
     * 计算维度结果
     */
    private DimensionResultDO calculateDimensionResult(QuestionnaireDimensionDO dimension, 
                                                      List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList,
                                                      StudentProfileVO studentProfile) {
        logger.info("开始计算维度结果: dimensionId={}, dimensionName={}", dimension.getId(), dimension.getDimensionName());
        
        // 1. 获取该维度的所有规则配置（按优先级排序）
        List<QuestionnaireResultConfigDO> configList = null;
        try {
            logger.info("准备查询维度规则配置: dimensionId={}", dimension.getId());
            configList = questionnaireResultConfigService.getQuestionnaireResultConfigListByDimensionId(dimension.getId());
            logger.info("查询到{}条维度规则配置: dimensionId={}", configList != null ? configList.size() : "null", dimension.getId());
        } catch (Exception e) {
            logger.error("查询维度规则配置失败: dimensionId={}", dimension.getId(), e);
            return null;
        }
        
        if (configList.isEmpty()) {
            logger.warn("维度没有配置计算规则: dimensionId={}", dimension.getId());
            return null;
        }
        
        // 打印所有规则配置的详细信息
        for (int i = 0; i < configList.size(); i++) {
            QuestionnaireResultConfigDO config = configList.get(i);
            logger.info("  规则[{}]: id={}, questionIndex={}, calculateType={}, level={}, isAbnormal={}, formula={}", 
                i, config.getId(), config.getQuestionIndex(), config.getCalculateType(), 
                config.getLevel(), config.getIsAbnormal(), config.getCalculateFormula());
        }
        
        // 2. 构建评估上下文
        EvaluateContext context = buildEvaluateContext(answerList, studentProfile);
        
        // 3. 按优先级遍历规则，单命中短路
        for (QuestionnaireResultConfigDO config : configList) {
            try {
                // 检查是否使用新的JSON规则引擎
                if (isJsonRule(config.getCalculateFormula())) {
                    // 使用统一表达式引擎计算
                    DimensionResultDO result = executeJsonRule(config, context, dimension);
                    if (result != null) {
                        logger.info("维度计算成功(JSON规则): dimensionId={}, level={}, score={}", 
                            dimension.getId(), result.getLevel(), result.getScore());
                        return result;
                    }
                } else {
                    // 兼容旧的计算逻辑
                    DimensionResultDO result = executeLegacyRule(config, answerList, studentProfile, dimension);
                    if (result != null) {
                        logger.info("维度计算成功(兼容规则): dimensionId={}, level={}, score={}", 
                            dimension.getId(), result.getLevel(), result.getScore());
                        return result;
                    }
                }
            } catch (Exception e) {
                logger.error("规则计算失败: configId={}, dimensionId={}", config.getId(), dimension.getId(), e);
            }
        }
        
        logger.warn("维度没有命中任何规则: dimensionId={}", dimension.getId());
        return null;
    }
    
    /**
     * 判断是否为JSON规则
     */
    private boolean isJsonRule(String calculateFormula) {
        if (calculateFormula == null || calculateFormula.trim().isEmpty()) {
            return false;
        }
        return calculateFormula.trim().startsWith("{");
    }
    
    /**
     * 执行JSON规则
     */
    private DimensionResultDO executeJsonRule(QuestionnaireResultConfigDO config, 
                                            EvaluateContext context, 
                                            QuestionnaireDimensionDO dimension) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonRule = mapper.readTree(config.getCalculateFormula());
            
            logger.info("执行JSON规则: configId={}, formula={}", config.getId(), config.getCalculateFormula());
            logger.info("上下文变量: {}", context.getVariables());
            logger.info("上下文题目分数: {}", context.getQuestionScoreMap());

            // 计算该维度的总分（仅按本维度配置的 question_index 汇总），并在子上下文中覆盖 totalScore
            EvaluateContext sub = new EvaluateContext();
            // 复制变量
            for (java.util.Map.Entry<String, Object> e : context.getVariables().entrySet()) {
                sub.withVar(e.getKey(), e.getValue());
            }
            // 复制题目分数与选项
            for (java.util.Map.Entry<String, java.math.BigDecimal> e : context.getQuestionScoreMap().entrySet()) {
                sub.withScore(e.getKey(), e.getValue());
            }
            for (java.util.Map.Entry<String, String> e : context.getQuestionOptionTextMap().entrySet()) {
                sub.withOpt(e.getKey(), e.getValue());
            }

            // 解析维度关联题目索引
            java.util.Set<Integer> indices = new java.util.HashSet<>();
            if (config.getQuestionIndex() != null && !INDEX_ALL.equalsIgnoreCase(config.getQuestionIndex())) {
                for (String part : config.getQuestionIndex().split(",")) {
                    String s = part.trim();
                    if (!s.isEmpty()) {
                        try { indices.add(Integer.parseInt(s)); } catch (Exception ignore) {}
                    }
                }
            }

            // 计算维度总分：若未配置 question_index 或为 all，则使用所有题目；否则只累加指定题目
            java.math.BigDecimal dimensionTotal = java.math.BigDecimal.ZERO;
            if (indices.isEmpty() && config.getQuestionIndex() != null && !config.getQuestionIndex().trim().isEmpty() && !INDEX_ALL.equalsIgnoreCase(config.getQuestionIndex())) {
                // 配置非空但解析不到，保持0
            } else if (indices.isEmpty()) {
                for (java.util.Map.Entry<String, java.math.BigDecimal> e : context.getQuestionScoreMap().entrySet()) {
                    // 跳过非Q前缀（如 totalScore）
                    if (e.getKey() != null && e.getKey().startsWith("Q")) {
                        dimensionTotal = dimensionTotal.add(e.getValue());
                    }
                }
            } else {
                for (Integer idx : indices) {
                    java.math.BigDecimal v = context.getQuestionScoreMap().get("Q" + idx);
                    if (v != null) {
                        dimensionTotal = dimensionTotal.add(v);
                    }
                }
            }
            // 覆盖 totalScore 变量为“维度总分”
            sub.withVar("totalScore", dimensionTotal);

            // 使用表达式引擎评估规则（基于子上下文）
            EvaluateResult result = expressionExecutor.evaluate(jsonRule, sub);
            
            logger.info("规则评估结果: matched={}, payload={}", result.isMatched(), result.getPayload());
            
            if (result.isMatched()) {
                // 构建维度结果
                DimensionResultDO dimensionResult = new DimensionResultDO();
                dimensionResult.setDimensionId(dimension.getId());
                
                // 从payload中提取score，如果没有则使用上下文中的totalScore
                BigDecimal score = BigDecimal.ZERO;
                if (result.getPayload() != null && result.getPayload().containsKey("score")) {
                    Object scoreObj = result.getPayload().get("score");
                    if (scoreObj instanceof Number) {
                        score = new BigDecimal(scoreObj.toString());
                    }
                } else if (sub.getVariables().containsKey("totalScore")) {
                    // 如果payload中没有score，则使用子上下文中的totalScore（维度总分）
                    Object totalScoreObj = sub.getVariables().get("totalScore");
                    if (totalScoreObj instanceof Number) {
                        score = new BigDecimal(totalScoreObj.toString());
                    }
                }
                dimensionResult.setScore(score);
                
                logger.info("设置维度结果分数: configId={}, score={}", config.getId(), score);
                
                dimensionResult.setIsAbnormal(config.getIsAbnormal());
                dimensionResult.setRiskLevel(config.getRiskLevel());
                dimensionResult.setLevel(config.getLevel());
                dimensionResult.setTeacherComment(config.getTeacherComment());
                
                // 从配置的JSON数组中随机选择一条学生评语
                String selectedStudentComment = StudentCommentUtils.selectRandomComment(config.getStudentComment());
                dimensionResult.setStudentComment(selectedStudentComment);
                
                // 处理扩展数据（如收集的原因、分类等）
                if (result.getPayload() != null && !result.getPayload().isEmpty()) {
                    ObjectMapper payloadMapper = new ObjectMapper();
                    dimensionResult.setDescription(payloadMapper.writeValueAsString(result.getPayload()));
                }
                
                return dimensionResult;
            }
        } catch (Exception e) {
            logger.error("JSON规则执行失败: configId={}", config.getId(), e);
        }
        return null;
    }
    
    /**
     * 执行兼容的旧规则
     */
    private DimensionResultDO executeLegacyRule(QuestionnaireResultConfigDO config, 
                                              List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList,
                                              StudentProfileVO studentProfile,
                                              QuestionnaireDimensionDO dimension) {
        // 计算基础分数
        int score = calculateScoreByResultConfig(config, answerList);
        
        // 获取用户信息
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int age = NumberUtils.calculateAge(studentProfile.getBirthDate().format(formatter));
        int sex = studentProfile.getSex();
        
        boolean matched = false;
        
        // 年龄性别与分数计算公式
        if (config.getCalculateType().equals(QuestionnaireResultCalculateTypeEnum.AGE_SEX_SCORE.getType())) {
            List<AgeAndSexAndScoreFormulaVO> formulaList = JSONArray.parseArray(config.getCalculateFormula(), AgeAndSexAndScoreFormulaVO.class);
            for (AgeAndSexAndScoreFormulaVO formula : formulaList) {
                if (sex == formula.getSex()
                        && NumberUtils.isBetween(age, formula.getMinAge(), formula.getMaxAge())
                        && NumberUtils.isBetween(score, formula.getMinScore(), formula.getMaxScore())) {
                    matched = true;
                    break;
                }
            }
        }
        // 分数区间计算公式
        else if (config.getCalculateType().equals(QuestionnaireResultCalculateTypeEnum.SCORE.getType())) {
            ScoreBetweenFormulaVO formula = JSON.parseObject(config.getCalculateFormula(), ScoreBetweenFormulaVO.class);
            if (NumberUtils.isBetween(score, formula.getMinScore(), formula.getMaxScore())) {
                matched = true;
            }
        }
        // 最多选择计算公式
        else if (config.getCalculateType().equals(QuestionnaireResultCalculateTypeEnum.MOST_CHOOSE.getType())) {
            MostChooseFormulaVO formula = JSON.parseObject(config.getCalculateFormula(), MostChooseFormulaVO.class);
            int chooseCount = 0;
            for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : answerList) {
                if (answerItem.getScore() == formula.getQuestionScore()) {
                    chooseCount++;
                }
            }
            if (chooseCount >= formula.getChooseCount()) {
                matched = true;
            }
        }
        
        if (matched) {
            DimensionResultDO dimensionResult = new DimensionResultDO();
            dimensionResult.setDimensionId(dimension.getId());
            dimensionResult.setScore(new BigDecimal(score));
            dimensionResult.setIsAbnormal(config.getIsAbnormal());
            dimensionResult.setRiskLevel(config.getRiskLevel());
            dimensionResult.setLevel(config.getLevel());
            dimensionResult.setTeacherComment(config.getTeacherComment());
            
            // 从配置的JSON数组中随机选择一条学生评语
            String selectedStudentComment = StudentCommentUtils.selectRandomComment(config.getStudentComment());
            dimensionResult.setStudentComment(selectedStudentComment);
            
            dimensionResult.setDescription(config.getDescription());
            return dimensionResult;
        }
        
        return null;
    }
    
    /**
     * 构建评估上下文
     */
    private EvaluateContext buildEvaluateContext(List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList,
                                               StudentProfileVO studentProfile) {
        EvaluateContext context = new EvaluateContext();
        
        // 计算总分
        BigDecimal totalScore = BigDecimal.ZERO;
        
        // 添加题目分数和选项
        for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answer : answerList) {
            BigDecimal questionScore = new BigDecimal(answer.getScore());
            context.withScore("Q" + answer.getIndex(), questionScore);
            
            // 累加到总分
            totalScore = totalScore.add(questionScore);
            
            if (answer.getAnswer() != null) {
                context.withOpt("Q" + answer.getIndex(), answer.getAnswer());
            }
        }
        
        // 添加总分到上下文（这是关键！）
        context.withScore("totalScore", totalScore);
        context.withVar("totalScore", totalScore);
        
        logger.info("构建评估上下文完成，总分: {}, 题目数量: {}", totalScore, answerList.size());
        
        // 添加用户变量
        if (studentProfile != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int age = NumberUtils.calculateAge(studentProfile.getBirthDate().format(formatter));
            context.withVar("age", age);
            context.withVar("sex", studentProfile.getSex());
            context.withVar("userId", studentProfile.getUserId());
        }
        
        return context;
    }
    
    /**
     * 转换为旧的QuestionnaireResultVO格式（兼容性）
     */
    private QuestionnaireResultVO convertToQuestionnaireResultVO(QuestionnaireDimensionDO dimension, 
                                                               DimensionResultDO dimensionResult) {
        QuestionnaireResultVO resultVO = new QuestionnaireResultVO();
        resultVO.setScore(dimensionResult.getScore() != null ? dimensionResult.getScore().intValue() : 0);
        resultVO.setTeacherComment(dimensionResult.getTeacherComment());
        resultVO.setStudentComment(dimensionResult.getStudentComment());
        resultVO.setIsAbnormal(dimensionResult.getIsAbnormal());
        resultVO.setLevel(dimensionResult.getLevel());
        resultVO.setDescription(dimensionResult.getDescription());
        return resultVO;
    }

    /**
     * 根据每个问卷结果参数计算分数（兼容旧逻辑）
     */
    private int calculateScoreByResultConfig(QuestionnaireResultConfigDO resultConfigDO, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
        int score = 0;
        if (resultConfigDO.getQuestionIndex() == null) {
            return score;
        }
        
        if (resultConfigDO.getQuestionIndex().equals(INDEX_ALL)) {
            for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : answerList) {
                score += answerItem.getScore();
            }
        } else {
            int[] questionIndex = Arrays.stream(resultConfigDO.getQuestionIndex().split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            for (int index : questionIndex) {
                for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : answerList) {
                    if (answerItem.getIndex().equals(index)) {
                        score += answerItem.getScore();
                    }
                }
            }
        }
        return score;
    }

}
