package com.lvye.mindtrip.module.psychology.service.questionnaire;

import com.lvye.mindtrip.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import com.lvye.mindtrip.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireDimensionDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.profile.StudentProfileMapper;
import com.lvye.mindtrip.module.psychology.rule.executor.ExpressionExecutor;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateContext;
import com.lvye.mindtrip.module.psychology.rule.model.EvaluateResult;
import com.lvye.mindtrip.module.psychology.enums.QuestionnaireResultCalculateTypeEnum;
import com.lvye.mindtrip.module.psychology.service.questionnaire.vo.AgeAndSexAndScoreFormulaVO;
import com.lvye.mindtrip.module.psychology.service.questionnaire.vo.MostChooseFormulaVO;
import com.lvye.mindtrip.module.psychology.service.questionnaire.vo.QuestionnaireResultVO;
import com.lvye.mindtrip.module.psychology.service.questionnaire.vo.ScoreBetweenFormulaVO;
import com.lvye.mindtrip.module.psychology.util.NumberUtils;
import com.lvye.mindtrip.module.psychology.util.StudentCommentUtils;
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
        
        // 3. 遍历规则：先确定“单命中”主结果，再叠加“特殊规则”（抑郁障碍/自伤意念）
        DimensionResultDO primary = null;
        List<String> multiLevels = new ArrayList<>();
        List<String> multiTeacherComments = new ArrayList<>();
        List<String> multiStudentComments = new ArrayList<>();
        int multiRiskMax = 0;
        int multiAbnormalMax = 0;

        for (QuestionnaireResultConfigDO config : configList) {
            try {
                // 判断当前规则是否为多命中规则
                boolean isConfiguredMulti = config.getIsMultiHit() != null && config.getIsMultiHit() == 1;
                
                // 如果主结果已确定，且当前规则不是多命中规则，则跳过（避免重复评估普通规则）
                if (primary != null && !isConfiguredMulti) {
                    continue;
                }
                
                DimensionResultDO hit = null;
                if (isJsonRule(config.getCalculateFormula())) {
                    hit = executeJsonRule(config, context, dimension, answerList);
                } else {
                    hit = executeLegacyRule(config, answerList, studentProfile, dimension);
                }

                if (hit == null) {
                    continue;
                }

                String level = hit.getLevel();

                if (isConfiguredMulti) {
                    if (level != null && !level.trim().isEmpty()) {
                        multiLevels.add(level.trim());
                    }
                    if (hit.getTeacherComment() != null && !hit.getTeacherComment().trim().isEmpty()) {
                        multiTeacherComments.add(hit.getTeacherComment().trim());
                    }
                    if (hit.getStudentComment() != null && !hit.getStudentComment().trim().isEmpty()) {
                        multiStudentComments.add(hit.getStudentComment().trim());
                    }
                    if (hit.getRiskLevel() != null) multiRiskMax = Math.max(multiRiskMax, hit.getRiskLevel());
                    if (hit.getIsAbnormal() != null) multiAbnormalMax = Math.max(multiAbnormalMax, hit.getIsAbnormal());
                    // 多命中规则不作为主结果，继续匹配下一条
                    continue;
                }

                // 非特殊规则：若尚未确定主结果，则采用首个命中作为主结果（单命中）
                if (primary == null) {
                    primary = hit;
                    logger.info("维度主结果命中: dimensionId={}, level={}, score={}, 后续仅匹配多命中规则",
                            dimension.getId(), primary.getLevel(), primary.getScore());
                }
                // 命中主结果后，继续循环以便叠加可能命中的特殊规则（通过上面的 continue 检查确保只处理多命中规则）
            } catch (Exception e) {
                logger.error("规则计算失败: configId={}, dimensionId={}", config.getId(), dimension.getId(), e);
            }
        }

        // 若既无主结果也无特殊命中，则返回空
        if (primary == null && multiLevels.isEmpty()) {
            logger.warn("维度没有命中任何规则: dimensionId={}", dimension.getId());
            return null;
        }

        // 组装最终结果：以主结果为基础，叠加特殊规则的等级与评语
        DimensionResultDO finalResult = new DimensionResultDO();
        finalResult.setDimensionId(dimension.getId());

        if (primary != null) {
            finalResult.setScore(primary.getScore());
            finalResult.setIsAbnormal(primary.getIsAbnormal());
            finalResult.setRiskLevel(primary.getRiskLevel());
            finalResult.setLevel(primary.getLevel());
            finalResult.setTeacherComment(primary.getTeacherComment());
            finalResult.setStudentComment(primary.getStudentComment());
            finalResult.setDescription(primary.getDescription());
        } else {
            // 无主结果，仅有多命中规则命中时的兜底
            finalResult.setScore(BigDecimal.ZERO);
            finalResult.setRiskLevel(multiRiskMax);
            finalResult.setIsAbnormal(multiAbnormalMax);
        }

        // 叠加特殊等级
        if (!multiLevels.isEmpty()) {
            String baseLevel = finalResult.getLevel();
            String specialJoined = String.join("，", deduplicatePreserveOrder(multiLevels));
            finalResult.setLevel(baseLevel == null || baseLevel.trim().isEmpty()
                    ? specialJoined
                    : baseLevel + "，" + specialJoined);
        }

        // 叠加教师评语（以主结果在前，多命中规则在后），以分号隔开
        if (!multiTeacherComments.isEmpty()) {
            String baseTeacher = finalResult.getTeacherComment();
            String appended = String.join("", deduplicatePreserveOrder(multiTeacherComments));
            finalResult.setTeacherComment(baseTeacher == null || baseTeacher.trim().isEmpty()
                    ? appended
                    : baseTeacher + appended);
        }

        // 叠加学生评语（以主结果在前，多命中规则在后），以分号隔开
        if (!multiStudentComments.isEmpty()) {
            String baseStudent = finalResult.getStudentComment();
            String appended = String.join("；", deduplicatePreserveOrder(multiStudentComments));
            finalResult.setStudentComment(baseStudent == null || baseStudent.trim().isEmpty()
                    ? appended
                    : baseStudent + "；" + appended);
        }

        logger.info("维度结果合成完成: dimensionId={}, level={}, riskLevel={}, score={}",
                dimension.getId(), finalResult.getLevel(), finalResult.getRiskLevel(), finalResult.getScore());
        return finalResult;
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
                                            QuestionnaireDimensionDO dimension,
                                            List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonRule = mapper.readTree(config.getCalculateFormula());
            
            logger.info("执行JSON规则: configId={}, formula={}", config.getId(), config.getCalculateFormula());
            logger.info("上下文变量: {}", context.getVariables());
            logger.info("上下文题目分数: {}", context.getQuestionScoreMap());
            
            // 识别并处理 qfc (Question Filter Condition) 规则
            if (jsonRule.has("qfc")) {
                DimensionResultDO qfcResult = evaluateQfcRule(jsonRule, config, context, dimension, answerList);
                if (qfcResult != null) {
                    return qfcResult;
                }
            }

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

            // 计算维度总分与题目数量：若未配置 question_index 或为 all，则使用所有题目；否则只累加指定题目
            java.math.BigDecimal dimensionTotal = java.math.BigDecimal.ZERO;
            int includedCount = 0;
            if (indices.isEmpty() && config.getQuestionIndex() != null && !config.getQuestionIndex().trim().isEmpty() && !INDEX_ALL.equalsIgnoreCase(config.getQuestionIndex())) {
                // 配置非空但解析不到，保持0
            } else if (indices.isEmpty()) {
                for (java.util.Map.Entry<String, java.math.BigDecimal> e : context.getQuestionScoreMap().entrySet()) {
                    // 跳过非Q前缀（如 totalScore）
                    if (e.getKey() != null && e.getKey().startsWith("Q")) {
                        if (e.getValue() != null) {
                            dimensionTotal = dimensionTotal.add(e.getValue());
                            includedCount++;
                        }
                    }
                }
            } else {
                for (Integer idx : indices) {
                    java.math.BigDecimal v = context.getQuestionScoreMap().get("Q" + idx);
                    if (v != null) {
                        dimensionTotal = dimensionTotal.add(v);
                        includedCount++;
                    }
                }
            }
            // 覆盖 totalScore 变量为“维度总分”
            sub.withVar("totalScore", dimensionTotal);
            // 提供维度总分、题目数量、均值变量，供 JSON 表达式使用
            sub.withVar("dimensionTotal", dimensionTotal);
            sub.withVar("questionCount", includedCount);
            java.math.BigDecimal avgScore = java.math.BigDecimal.ZERO;
            if (includedCount > 0) {
                avgScore = dimensionTotal.divide(new java.math.BigDecimal(includedCount), 8, java.math.RoundingMode.HALF_UP);
            }
            sub.withVar("avgScore", avgScore);

			// 若为七因子规则，预先计算并注入七因子总分
			try {
				if (jsonRule.has("meta") && jsonRule.get("meta").has("sevenFactors")
						&& jsonRule.get("meta").get("sevenFactors").asBoolean()) {
					injectSevenFactorsVariables(sub);
				}
			} catch (Exception e) {
				logger.error("七因子计算注入失败: configId={}", config.getId(), e);
			}

			// 识别并处理 part-sum-compare 规则
			if (jsonRule.has("type") && "part-sum-compare".equalsIgnoreCase(jsonRule.get("type").asText())) {
				DimensionResultDO partCompareResult = evaluatePartSumCompare(jsonRule, sub, dimension);
				if (partCompareResult != null) {
					return partCompareResult;
				}
			}

			// 使用表达式引擎评估规则（基于子上下文）
			EvaluateResult result = expressionExecutor.evaluate(jsonRule, sub);
            
            logger.info("规则评估结果: matched={}, payload={}", result.isMatched(), result.getPayload());
            
            if (result.isMatched()) {
                // 构建维度结果
                DimensionResultDO dimensionResult = new DimensionResultDO();
                dimensionResult.setDimensionId(dimension.getId());
                
                // 从 payload 中提取 score；否则根据规则目标优先取 avgScore，再回退总分
                BigDecimal score = BigDecimal.ZERO;
                if (result.getPayload() != null && result.getPayload().containsKey("score")) {
                    Object scoreObj = result.getPayload().get("score");
                    if (scoreObj instanceof Number) {
                        score = new BigDecimal(scoreObj.toString());
                    }
                } else {
                    BigDecimal avgVar = extractNumericVar(sub, "avgScore");
                    BigDecimal sevenVar = extractNumericVar(sub, "sevenFactors.totalScore");
                    // 维度总分可从 dimensionTotal 或 totalScore 变量中获取
                    BigDecimal totalVar = extractNumericVar(sub, "dimensionTotal");
                    if (totalVar == null) {
                        totalVar = extractNumericVar(sub, "totalScore");
                    }

                    boolean preferSeven = ruleTargetsSevenFactors(jsonRule);
                    boolean preferAvg = ruleTargetsAvgScore(jsonRule);
                    if (preferSeven && sevenVar != null) {
                        score = sevenVar;
                    } else if (preferAvg && avgVar != null) {
                        score = avgVar;
                    } else if (totalVar != null) {
                        score = totalVar;
                    } else if (sevenVar != null) {
                        score = sevenVar;
                    } else if (avgVar != null) {
                        // 两者都不可用时，尽量保留平均分
                        score = avgVar;
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
                
                // 处理扩展数据：合并规则 payload，并附带七因子各分值（若存在）
                Map<String, Object> descPayload = new LinkedHashMap<>();
                if (result.getPayload() != null && !result.getPayload().isEmpty()) {
                    descPayload.putAll(result.getPayload());
                }
                Map<String, Object> seven = buildSevenFactorsPayload(sub);
                if (seven != null && !seven.isEmpty()) {
                    descPayload.put("sevenFactors", seven);
                }
                if (!descPayload.isEmpty()) {
                    ObjectMapper payloadMapper = new ObjectMapper();
                    dimensionResult.setDescription(payloadMapper.writeValueAsString(descPayload));
                }
                
                return dimensionResult;
            }
        } catch (Exception e) {
            logger.error("JSON规则执行失败: configId={}", config.getId(), e);
        }
        return null;
    }

    /**
     * 从子上下文变量中提取数值变量，优先解析为 BigDecimal。
     */
    private BigDecimal extractNumericVar(EvaluateContext ctx, String key) {
        if (ctx == null || key == null) return null;
        Object v = ctx.getVariables().get(key);
        if (v == null) return null;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return new BigDecimal(v.toString());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 粗粒度判断规则是否以平均分为判断目标：
     * - 若规则中出现对 avgScore 的显式范围判断或作为 target/field，则认为偏向平均分。
     * - 或者 result 中显式声明 "scoreTarget":"avg"。
     */
    private boolean ruleTargetsAvgScore(JsonNode jsonRule) {
        if (jsonRule == null || !jsonRule.isObject()) return false;
        try {
            // 检查 result.scoreTarget
            JsonNode result = jsonRule.get("result");
            if (result != null && result.isObject()) {
                JsonNode scoreTarget = result.get("scoreTarget");
                if (scoreTarget != null && "avg".equalsIgnoreCase(scoreTarget.asText())) return true;
            }
            // 递归查找是否有对 avgScore 的范围判断或字段引用
            return containsTextKey(jsonRule, "avgScore");
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * 判断是否为七因子规则优先：
     * - meta.sevenFactors=true，或出现 sevenFactors.totalScore 的字段引用，
     * - 或 result.scoreTarget=seven。
     */
    private boolean ruleTargetsSevenFactors(JsonNode jsonRule) {
        if (jsonRule == null || !jsonRule.isObject()) return false;
        try {
            JsonNode meta = jsonRule.get("meta");
            if (meta != null && meta.isObject() && meta.has("sevenFactors") && meta.get("sevenFactors").asBoolean()) {
                return true;
            }
            JsonNode result = jsonRule.get("result");
            if (result != null && result.isObject()) {
                JsonNode scoreTarget = result.get("scoreTarget");
                if (scoreTarget != null && ("seven".equalsIgnoreCase(scoreTarget.asText()) ||
                        "sevenFactors".equalsIgnoreCase(scoreTarget.asText()))) {
                    return true;
                }
            }
            return containsTextKey(jsonRule, "sevenFactors.totalScore");
        } catch (Exception ignore) {
            return false;
        }
    }

    private boolean containsTextKey(JsonNode node, String key) {
        if (node == null) return false;
        if (node.isTextual() && key.equals(node.asText())) return true;
        if (node.isObject()) {
            java.util.Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                if (key.equals(name)) return true;
                JsonNode child = node.get(name);
                if (containsTextKey(child, key)) return true;
            }
        } else if (node.isArray()) {
            for (JsonNode n : node) {
                if (containsTextKey(n, key)) return true;
            }
        }
        return false;
    }

	/**
	 * 处理 "part-sum-compare" 类型规则：
	 * - 对 parts 中每个部分按 questionIndex 求和
	 * - 依 ranges 匹配等级/评语
	 * - 按 compare.strategy 和 tieBreak 选出优胜部分，或按 onTie 输出并列结果
	 */
	private DimensionResultDO evaluatePartSumCompare(JsonNode rule, EvaluateContext ctx, QuestionnaireDimensionDO dimension) {
		try {
			JsonNode partsNode = rule.get("parts");
			if (partsNode == null || !partsNode.isArray() || partsNode.size() == 0) return null;

			List<PartEval> parts = new ArrayList<>();
			for (JsonNode pn : partsNode) {
				String key = pn.has("key") ? pn.get("key").asText() : "";
				String name = pn.has("name") ? pn.get("name").asText() : key;
				String qIndex = pn.has("questionIndex") ? pn.get("questionIndex").asText() : "";
				int sum = 0;
				for (Integer idx : parseIndices(qIndex)) {
					BigDecimal v = ctx.getQuestionScoreMap().get("Q" + idx);
					if (v != null) sum += v.intValue();
				}

				JsonNode ranges = pn.get("ranges");
				JsonNode matched = findRange(ranges, sum);
				PartEval pe = new PartEval();
				pe.key = key;
				pe.name = name;
				pe.sum = sum;
				if (matched != null) {
					pe.band = matched.has("band") ? matched.get("band").asText() : null;
					pe.level = matched.has("level") ? matched.get("level").asText() : null;
					pe.rank = matched.has("rank") ? matched.get("rank").asInt() : 0;
					pe.riskLevel = matched.has("riskLevel") ? matched.get("riskLevel").asInt() : 0;
					pe.teacherComment = matched.has("teacherComment") ? matched.get("teacherComment").asText() : null;
					pe.studentComment = matched.has("studentComment") ? matched.get("studentComment").asText() : null;
					pe.isAbnormal = matched.has("isAbnormal") ? matched.get("isAbnormal").asInt() : 0;
				}
				parts.add(pe);
			}

			JsonNode compare = rule.get("compare");
			String strategy = compare != null && compare.has("strategy") ? compare.get("strategy").asText() : "byRank";
			String tieBreak = compare != null && compare.has("tieBreak") ? compare.get("tieBreak").asText() : "byScore";
			JsonNode onTie = compare != null ? compare.get("onTie") : null;

			// 检查是否所有部分的 band 都相同
			boolean allSameBand = false;
			String commonBand = null;
			if (!parts.isEmpty()) {
				final String firstBand = parts.get(0).band;
				commonBand = firstBand;
				allSameBand = parts.stream().allMatch(p ->
					p.band != null && p.band.equalsIgnoreCase(firstBand)
				);
			}

			boolean tieApplied = false;
			PartEval winner = null;

			// 如果所有部分的 band 相同，且配置了 onTie.byBand，则直接应用 onTie 配置
			if (allSameBand && onTie != null && onTie.has("mode") && "byBand".equalsIgnoreCase(onTie.get("mode").asText())) {
				tieApplied = true;
				JsonNode byBand = onTie.get("byBand");
				JsonNode tieRange = findBand(byBand, commonBand);
				if (tieRange != null) {
					winner = new PartEval();
					winner.key = "TIE";
					winner.name = "并列";
					winner.band = commonBand;
					winner.sum = parts.size() > 0 ? parts.get(0).sum : 0;
					winner.level = tieRange.has("level") ? tieRange.get("level").asText() : null;
					winner.rank = parts.size() > 0 ? parts.get(0).rank : 0;
					winner.riskLevel = tieRange.has("riskLevel") ? tieRange.get("riskLevel").asInt() : 0;
					winner.teacherComment = tieRange.has("teacherComment") ? tieRange.get("teacherComment").asText() : null;
					winner.studentComment = tieRange.has("studentComment") ? tieRange.get("studentComment").asText() : null;
					winner.isAbnormal = tieRange.has("isAbnormal") ? tieRange.get("isAbnormal").asInt() : 0;
				}
			} else {
				// 原有逻辑：选择优胜部分
				List<PartEval> candidates = new ArrayList<>(parts);
				if ("byRank".equalsIgnoreCase(strategy)) {
					int maxRank = candidates.stream().mapToInt(p -> p.rank).max().orElse(0);
					candidates.removeIf(p -> p.rank != maxRank);
				}

				if (candidates.size() > 1 && "byScore".equalsIgnoreCase(tieBreak)) {
					int maxScore = candidates.stream().mapToInt(p -> p.sum).max().orElse(0);
					candidates.removeIf(p -> p.sum != maxScore);
				}

				if (candidates.size() == 1) {
					winner = candidates.get(0);
				} else {
					// 并列处理
					tieApplied = true;
					if (onTie != null && onTie.has("mode") && "byBand".equalsIgnoreCase(onTie.get("mode").asText())) {
						String band = candidates.size() > 0 ? candidates.get(0).band : null;
						JsonNode byBand = onTie.get("byBand");
						JsonNode tieRange = findBand(byBand, band);
						if (tieRange != null) {
							winner = new PartEval();
							winner.key = "TIE";
							winner.name = "并列";
							winner.band = band;
							winner.sum = candidates.size() > 0 ? candidates.get(0).sum : 0;
							winner.level = tieRange.has("level") ? tieRange.get("level").asText() : null;
							winner.rank = candidates.size() > 0 ? candidates.get(0).rank : 0;
							winner.riskLevel = tieRange.has("riskLevel") ? tieRange.get("riskLevel").asInt() : 0;
							winner.teacherComment = tieRange.has("teacherComment") ? tieRange.get("teacherComment").asText() : null;
							winner.studentComment = tieRange.has("studentComment") ? tieRange.get("studentComment").asText() : null;
							winner.isAbnormal = tieRange.has("isAbnormal") ? tieRange.get("isAbnormal").asInt() : 0;
						}
					}
					// 若仍为空，回退为任一候选（稳定输出）
					if (winner == null && !candidates.isEmpty()) winner = candidates.get(0);
				}
			}

			// 输出
			JsonNode output = rule.get("output");
			boolean inheritWinner = output != null && output.has("inheritWinner") && output.get("inheritWinner").asBoolean();
			boolean showWinnerPartName = output != null && output.has("extra") && output.get("extra").has("showWinnerPartName")
					&& output.get("extra").get("showWinnerPartName").asBoolean();

			if (winner == null) return null;

			DimensionResultDO dimensionResult = new DimensionResultDO();
			dimensionResult.setDimensionId(dimension.getId());
			dimensionResult.setScore(new BigDecimal(winner.sum));
			if (inheritWinner) {
				dimensionResult.setIsAbnormal(winner.isAbnormal);
				dimensionResult.setRiskLevel(winner.riskLevel);
				dimensionResult.setLevel(winner.level);
				dimensionResult.setTeacherComment(winner.teacherComment);
				// 学生评语优先使用 winner 的
				dimensionResult.setStudentComment(winner.studentComment);
			}

			// 组织描述信息，包含各部分详情与优胜信息
			Map<String, Object> desc = new LinkedHashMap<>();
			List<Map<String, Object>> partDesc = new ArrayList<>();
			for (PartEval p : parts) {
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("key", p.key);
				item.put("name", p.name);
				item.put("sum", p.sum);
				item.put("band", p.band);
				item.put("level", p.level);
				item.put("rank", p.rank);
				partDesc.add(item);
			}
			desc.put("parts", partDesc);
			Map<String, Object> win = new LinkedHashMap<>();
			win.put("key", winner.key);
			win.put("name", showWinnerPartName ? winner.name : null);
			win.put("sum", winner.sum);
			win.put("band", winner.band);
			win.put("level", winner.level);
			win.put("rank", winner.rank);
			desc.put("winner", win);
			desc.put("tieApplied", tieApplied);
			if (tieApplied && onTie != null && onTie.has("mode")) desc.put("tieMode", onTie.get("mode").asText());

			ObjectMapper payloadMapper = new ObjectMapper();
			dimensionResult.setDescription(payloadMapper.writeValueAsString(desc));
			return dimensionResult;
		} catch (Exception e) {
			logger.error("part-sum-compare 计算失败: {}", e.getMessage(), e);
			return null;
		}
	}

	private List<Integer> parseIndices(String csv) {
		List<Integer> res = new ArrayList<>();
		if (csv == null || csv.trim().isEmpty()) return res;
		for (String s : csv.split(",")) {
			String t = s.trim();
			if (t.isEmpty()) continue;
			try { res.add(Integer.parseInt(t)); } catch (Exception ignore) {}
		}
		return res;
	}

	private JsonNode findRange(JsonNode ranges, int sum) {
		if (ranges == null || !ranges.isArray()) return null;
		for (JsonNode r : ranges) {
			int min = r.has("min") ? r.get("min").asInt(Integer.MIN_VALUE) : Integer.MIN_VALUE;
			int max = r.has("max") ? r.get("max").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE;
			if (sum >= min && sum <= max) return r;
		}
		return null;
	}

	private JsonNode findBand(JsonNode byBandArr, String band) {
		if (byBandArr == null || !byBandArr.isArray() || band == null) return null;
		for (JsonNode n : byBandArr) {
			if (n.has("band") && band.equalsIgnoreCase(n.get("band").asText())) return n;
		}
		return null;
	}

	private static class PartEval {
		String key;
		String name;
		int sum;
		String band;
		String level;
		int rank;
		int riskLevel;
		String teacherComment;
		String studentComment;
		int isAbnormal;
	}

	/**
	 * 计算七因子并注入到上下文变量中：
	 * - 主观睡眠质量：Q15 得分
	 * - 睡眠潜伏期：Q2 + Q5 的和，映射到 0/1/2/3 区间
	 * - 睡眠持续性：Q4 得分
	 * - 习惯性睡眠效率：根据 Q1/Q3 的时间差 与 Q4 选项映射的小时数 计算百分比映射到 0/1/2/3
	 * - 睡眠紊乱：Q6~Q14 的和映射到 0/1/2/3
	 * - 使用睡眠药物：Q16 得分
	 * - 白天功能紊乱：Q17 + Q18 的和映射到 0/1/2/3
	 */
	private void injectSevenFactorsVariables(EvaluateContext ctx) {
		try {
			int f1 = bdToInt(ctx.getQuestionScoreMap().getOrDefault("Q15", BigDecimal.ZERO));

			int latencySum = bdToInt(ctx.getQuestionScoreMap().getOrDefault("Q2", BigDecimal.ZERO))
					+ bdToInt(ctx.getQuestionScoreMap().getOrDefault("Q5", BigDecimal.ZERO));
			int f2 = mapByRanges(latencySum, new int[]{0, 2, 4}, new int[]{0, 1, 2, 3});

			int f3 = bdToInt(ctx.getQuestionScoreMap().getOrDefault("Q4", BigDecimal.ZERO));

			// 习惯性睡眠效率
			double durationHours = computeDurationHours(
					ctx.getQuestionOptionTextMap().get("Q1"), // 入睡/上床时间
					ctx.getQuestionOptionTextMap().get("Q3")  // 起床时间
			);
			double sleepHours = mapQ4OptionToHours(ctx.getQuestionOptionTextMap().get("Q4"));
			int efficiencyScore = mapEfficiencyToScore(durationHours, sleepHours);
			int f4 = efficiencyScore;

			int disturbSum = 0;
			for (int i = 6; i <= 14; i++) {
				disturbSum += bdToInt(ctx.getQuestionScoreMap().getOrDefault("Q" + i, BigDecimal.ZERO));
			}
			int f5 = mapByRanges(disturbSum, new int[]{0, 9, 18}, new int[]{0, 1, 2, 3});

			int f6 = bdToInt(ctx.getQuestionScoreMap().getOrDefault("Q16", BigDecimal.ZERO));

			int daytimeSum = bdToInt(ctx.getQuestionScoreMap().getOrDefault("Q17", BigDecimal.ZERO))
					+ bdToInt(ctx.getQuestionScoreMap().getOrDefault("Q18", BigDecimal.ZERO));
			int f7 = mapByRanges(daytimeSum, new int[]{0, 2, 4}, new int[]{0, 1, 2, 3});

			int total = clamp(f1, 0, 3) + clamp(f2, 0, 3) + clamp(f3, 0, 3) + clamp(f4, 0, 3)
					+ clamp(f5, 0, 3) + clamp(f6, 0, 3) + clamp(f7, 0, 3);

            ctx.withVar("sevenFactors.f1", f1);
            ctx.withVar("sevenFactors.f2", f2);
            ctx.withVar("sevenFactors.f3", f3);
            ctx.withVar("sevenFactors.f4", f4);
            ctx.withVar("sevenFactors.f5", f5);
            ctx.withVar("sevenFactors.f6", f6);
            ctx.withVar("sevenFactors.f7", f7);
            ctx.withVar("sevenFactors.totalScore", new BigDecimal(total));

			logger.info("七因子: f1={}, f2={}, f3={}, f4={}, f5={}, f6={}, f7={}, total={}", f1, f2, f3, f4, f5, f6, f7, total);
		} catch (Exception e) {
			logger.warn("七因子计算出现异常，将不影响规则继续评估: {}", e.getMessage());
		}
	}

    /**
     * 组织七因子各项分值，用于写入 description 进行事后校验
     */
    private Map<String, Object> buildSevenFactorsPayload(EvaluateContext ctx) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            Object f1 = ctx.getVariables().get("sevenFactors.f1");
            Object f2 = ctx.getVariables().get("sevenFactors.f2");
            Object f3 = ctx.getVariables().get("sevenFactors.f3");
            Object f4 = ctx.getVariables().get("sevenFactors.f4");
            Object f5 = ctx.getVariables().get("sevenFactors.f5");
            Object f6 = ctx.getVariables().get("sevenFactors.f6");
            Object f7 = ctx.getVariables().get("sevenFactors.f7");
            Object total = ctx.getVariables().get("sevenFactors.totalScore");
            if (f1 != null) m.put("f1", f1);
            if (f2 != null) m.put("f2", f2);
            if (f3 != null) m.put("f3", f3);
            if (f4 != null) m.put("f4", f4);
            if (f5 != null) m.put("f5", f5);
            if (f6 != null) m.put("f6", f6);
            if (f7 != null) m.put("f7", f7);
            if (total != null) m.put("totalScore", total);
            return m;
        } catch (Exception ignore) {
            return null;
        }
    }

	private int bdToInt(BigDecimal v) {
		return v == null ? 0 : v.intValue();
	}

	/**
	 * 将和值映射到 0/1/2/3 打分。
	 * thresholds 形如 {0, 2, 4} 表示分段：0 ->0；1~2 ->1；3~4 ->2；>=5 ->3
	 * scores 对应每段的得分，固定 {0,1,2,3}
	 */
	private int mapByRanges(int value, int[] thresholds, int[] scores) {
		if (thresholds == null || thresholds.length != 3 || scores == null || scores.length != 4) return 0;
		if (value <= thresholds[0]) return scores[0];
		if (value <= thresholds[1]) return scores[1];
		if (value <= thresholds[2]) return scores[2];
		return scores[3];
	}

	private int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}

	/**
	 * 计算从 Q1(上床/入睡时间) 到 Q3(起床时间) 的小时差，跨天自动加 24 小时。
	 * 文本格式："HH,mm"，如 "23,30"。
	 */
	private double computeDurationHours(String startHHmm, String endHHmm) {
		Integer start = parseHHmmToMinutes(startHHmm);
		Integer end = parseHHmmToMinutes(endHHmm);
		if (start == null || end == null) return 0.0;
		int e = end;
		if (e < start) e += 24 * 60;
		int diff = e - start;
		if (diff <= 0) return 0.0;
		return diff / 60.0;
	}

	private Integer parseHHmmToMinutes(String hhmm) {
		try {
			if (hhmm == null) return null;
			String[] parts = hhmm.split(",");
			if (parts.length != 2) return null;
			int h = Integer.parseInt(parts[0].trim());
			int m = Integer.parseInt(parts[1].trim());
			if (h < 0 || h > 23 || m < 0 || m > 59) return null;
			return h * 60 + m;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 将 Q4 的中文选项映射为用于计算的小时数：
	 * ">7小时"->8，"6-7小时"->7，"5-6小时"->6，"≤5小时"->5。
	 */
	private double mapQ4OptionToHours(String q4OptionText) {
		if (q4OptionText == null) return 0.0;
		String t = q4OptionText.replace(" ", "");
		if (t.contains(">7")) return 8.0;
		if (t.contains("6-7")) return 7.0;
		if (t.contains("5-6")) return 6.0;
		if (t.contains("≤5") || t.contains("<=5") || t.contains("≦5")) return 5.0;
		// 兜底：无法识别则返回 0
		return 0.0;
	}

	/**
	 * 睡眠效率得分区间：
	 * - >=85% -> 0
	 * - 75%~84% -> 1
	 * - 65%~74% -> 2
	 * - 0%~64% -> 3
	 */
	private int mapEfficiencyToScore(double durationHours, double sleepHours) {
		if (durationHours <= 0 || sleepHours <= 0) return 3;
		double percent = (sleepHours / durationHours) * 100.0;
		if (percent >= 85.0) return 0;
		if (percent >= 75.0) return 1;
		if (percent >= 65.0) return 2;
		return 3;
	}

	/**
	 * 评估 qfc (Question Filter Condition) 规则：题干拼接筛选
	 * 格式：{"qfc":{"op":"<","rhs":4,"all":true}}
	 * - op: 比较操作符 (<, <=, >, >=, ==, !=)
	 * - rhs: 比较的右值（分数阈值）
	 * - all: true=所有关联题目都满足才命中；false/null=任意一题满足即命中
	 */
	private DimensionResultDO evaluateQfcRule(JsonNode jsonRule, QuestionnaireResultConfigDO config,
	                                          EvaluateContext context, QuestionnaireDimensionDO dimension,
	                                          List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
		try {
			JsonNode qfcNode = jsonRule.get("qfc");
			if (qfcNode == null || !qfcNode.isObject()) {
				return null;
			}

			String op = qfcNode.has("op") ? qfcNode.get("op").asText() : "==";
			int rhs = qfcNode.has("rhs") ? qfcNode.get("rhs").asInt() : 0;
			boolean all = qfcNode.has("all") && qfcNode.get("all").asBoolean();

			logger.info("[QFC规则] configId={}, op={}, rhs={}, all={}", config.getId(), op, rhs, all);

			// 解析关联题目索引
			Set<Integer> indices = new HashSet<>();
			if (config.getQuestionIndex() != null && !INDEX_ALL.equalsIgnoreCase(config.getQuestionIndex())) {
				for (String part : config.getQuestionIndex().split(",")) {
					String s = part.trim();
					if (!s.isEmpty()) {
						try {
							indices.add(Integer.parseInt(s));
						} catch (Exception ignore) {}
					}
				}
			}

			if (indices.isEmpty()) {
				logger.warn("[QFC规则] 未配置关联题目索引, configId={}", config.getId());
				return null;
			}

			// 检查每个关联题目是否满足条件，并收集满足条件的题干
			List<String> matchedTitles = new ArrayList<>();
			int matchedCount = 0;
			int totalCount = 0;

			for (Integer idx : indices) {
				BigDecimal score = context.getQuestionScoreMap().get("Q" + idx);
				if (score == null) {
					logger.warn("[QFC规则] 题目Q{}无分数, configId={}", idx, config.getId());
					continue;
				}
				totalCount++;

				boolean satisfied = compareScore(score.intValue(), op, rhs);
				logger.info("[QFC规则] Q{} score={}, {}{}? {}", idx, score, score, op + rhs, satisfied);

				if (satisfied) {
					matchedCount++;
					// 从 answerList 中找到对应题目的题干
					for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answer : answerList) {
						if (answer.getIndex().equals(idx) && answer.getTitle() != null && !answer.getTitle().trim().isEmpty()) {
							matchedTitles.add(answer.getTitle().trim());
							break;
						}
					}
				}
			}

			// 判断是否命中规则
			boolean matched = false;
			if (all) {
				// 所有题目都满足
				matched = (totalCount > 0 && matchedCount == totalCount);
			} else {
				// 任意一题满足
				matched = (matchedCount > 0);
			}

			logger.info("[QFC规则] 命中判断: matched={}, matchedCount={}, totalCount={}, all={}",
					matched, matchedCount, totalCount, all);

			if (!matched) {
				return null;
			}

			// 构建维度结果
			DimensionResultDO dimensionResult = new DimensionResultDO();
			dimensionResult.setDimensionId(dimension.getId());

			// 计算分数（使用关联题目的总分）
			BigDecimal totalScore = BigDecimal.ZERO;
			for (Integer idx : indices) {
				BigDecimal score = context.getQuestionScoreMap().get("Q" + idx);
				if (score != null) {
					totalScore = totalScore.add(score);
				}
			}
			dimensionResult.setScore(totalScore);

			dimensionResult.setIsAbnormal(config.getIsAbnormal());
			dimensionResult.setRiskLevel(config.getRiskLevel());
			dimensionResult.setTeacherComment(config.getTeacherComment());

			// 从配置的JSON数组中随机选择一条学生评语
			String selectedStudentComment = StudentCommentUtils.selectRandomComment(config.getStudentComment());
			dimensionResult.setStudentComment(selectedStudentComment);

			// 处理 level：如果配置的 level 为空，则拼接满足条件的题干
			String level = config.getLevel();
			if (level == null || level.trim().isEmpty()) {
				if (!matchedTitles.isEmpty()) {
					level = String.join("；", matchedTitles);
					logger.info("[QFC规则] level为空，拼接题干: {}", level);
				}
			}
			dimensionResult.setLevel(level);

			// 记录描述信息
			Map<String, Object> desc = new LinkedHashMap<>();
			desc.put("qfcMatched", true);
			desc.put("matchedCount", matchedCount);
			desc.put("totalCount", totalCount);
			desc.put("matchedTitles", matchedTitles);
			ObjectMapper payloadMapper = new ObjectMapper();
			dimensionResult.setDescription(payloadMapper.writeValueAsString(desc));

			logger.info("[QFC规则] 命中成功: configId={}, level={}, score={}", config.getId(), level, totalScore);
			return dimensionResult;
		} catch (Exception e) {
			logger.error("[QFC规则] 执行失败: configId={}", config.getId(), e);
			return null;
		}
	}

	/**
	 * 比较分数是否满足条件
	 */
	private boolean compareScore(int score, String op, int rhs) {
		switch (op) {
			case "<": return score < rhs;
			case "<=": return score <= rhs;
			case ">": return score > rhs;
			case ">=": return score >= rhs;
			case "==": return score == rhs;
			case "!=": return score != rhs;
			default:
				logger.warn("未知的比较操作符: {}", op);
				return false;
		}
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
            String qKey = "Q" + answer.getIndex();
            context.withScore(qKey, questionScore);
            
            // 累加到总分
            totalScore = totalScore.add(questionScore);
            
            if (answer.getAnswer() != null) {
                context.withOpt(qKey, answer.getAnswer());
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
     * 去重并保持原有顺序
     */
    private List<String> deduplicatePreserveOrder(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (String s : list) {
            if (s == null) continue;
            String t = s.trim();
            if (!t.isEmpty()) set.add(t);
        }
        return new ArrayList<>(set);
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
