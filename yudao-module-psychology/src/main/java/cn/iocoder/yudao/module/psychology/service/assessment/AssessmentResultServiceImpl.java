package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.result.AssessmentResultDetailRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.result.RiskLevelInterventionVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentResultMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.QuestionnaireResultVO;

import java.util.ArrayList;
import java.util.List;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorFactory;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.AssessmentResultVO;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.QuestionnaireResultVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 组合测评结果保存服务实现
 */
@Slf4j
@Service
public class AssessmentResultServiceImpl implements AssessmentResultService {

    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;
    @Resource
    private AssessmentUserTaskMapper assessmentUserTaskMapper;
    @Resource
    private AssessmentResultMapper assessmentResultMapper;
    @Resource
    private QuestionnaireMapper questionnaireMapper;
    @Resource
    private StudentProfileMapper studentProfileMapper;
    @Resource
    private ResultGeneratorFactory resultGeneratorFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateAndSaveCombinedResult(String taskNo, Long studentProfileId) {
        // 1) 通过studentProfileId获取userId
        StudentProfileDO studentProfile = studentProfileMapper.selectById(studentProfileId);
        if (studentProfile == null) {
            log.warn("未找到学生档案, studentProfileId={}", studentProfileId);
            return null;
        }
        Long userId = studentProfile.getUserId();

        // 2) 拉取该任务下该学生的全部问卷结果
        // 注意：lvye_questionnaire_result表中存储的是userId，不是studentProfileId
        List<QuestionnaireResultDO> resultDOList = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, userId);
        log.info("查询问卷结果, taskNo={}, studentProfileId={}, userId={}, 查询到{}条记录",
                 taskNo, studentProfileId, userId, resultDOList != null ? resultDOList.size() : 0);

        if (resultDOList != null && !resultDOList.isEmpty()) {
            log.info("问卷结果详情:");
            for (int i = 0; i < resultDOList.size(); i++) {
                QuestionnaireResultDO result = resultDOList.get(i);
                log.info("  [{}] ID={}, 问卷ID={}, 用户ID={}, 任务编号={}, 风险等级={}, 生成状态={}",
                         i + 1, result.getId(), result.getQuestionnaireId(), result.getUserId(),
                         result.getAssessmentTaskNo(), result.getRiskLevel(), result.getGenerationStatus());
            }
        }

        if (CollUtil.isEmpty(resultDOList)) {
            log.info("无问卷结果可用于生成测评结果, taskNo={}, studentProfileId={}", taskNo, studentProfileId);
            return null;
        }

        // 组装问卷结果 VO 列表
        List<QuestionnaireResultVO> questionnaireResults = new ArrayList<>();
        for (QuestionnaireResultDO r : resultDOList) {
            Map<String, BigDecimal> dimScores = null;
            if (r.getDimensionScores() != null) {
                dimScores = JsonUtils.parseObjectQuietly(r.getDimensionScores(), new TypeReference<Map<String, BigDecimal>>() {});
            }
            QuestionnaireResultVO vo = QuestionnaireResultVO.builder()
                    .questionnaireId(r.getQuestionnaireId())
                    .rawScore(r.getScore() == null ? BigDecimal.ZERO : r.getScore())
                    .standardScore(r.getScore()) // 现有 DO 仅有 score 字段，这里先用作标准分占位
                    .riskLevel(r.getRiskLevel())
                    .levelDescription(r.getEvaluate())
                    .dimensionScores(dimScores)
                    .reportContent(r.getResultData())
                    .suggestions(r.getSuggestions())
                    .build();
            questionnaireResults.add(vo);
        }

        // 获取参与者记录，以便拿到任务信息
        AssessmentUserTaskDO userTask = assessmentUserTaskMapper.selectByTaskNoAndUserId(taskNo, userId);
        // participantId 使用学生档案ID
        Long participantId = studentProfileId;

        // 2) 调用生成器生成组合测评结果
        ResultGenerationContext context = ResultGenerationContext.builder()
                .generationType(ResultGeneratorTypeEnum.COMBINED_ASSESSMENT)
                .assessmentId(userTask != null ? userTask.getId() : null) // 暂以参与者任务ID代替assessmentId概念
                .userId(userId) // 这里使用系统用户ID
                .questionnaireResults(questionnaireResults)
                .build();
        AssessmentResultVO resultVO = resultGeneratorFactory.generateResult(ResultGeneratorTypeEnum.COMBINED_ASSESSMENT, context);

        // 3) 将 VO 映射到 DO（综合报告由生成器统一拼接 evaluate_config 结果）
        AssessmentResultDO save = AssessmentResultDO.builder()
                .participantId(participantId)
                .taskNo(taskNo)  // 添加taskNo字段
                .dimensionCode("total")
                .score(resultVO.getCombinedScore() == null ? null : resultVO.getCombinedScore().intValue())
                .combinedRiskLevel(resultVO.getCombinedRiskLevel())
                .suggestion(resultVO.getComprehensiveReport())
                .questionnaireResults(JsonUtils.toJsonString(resultVO.getQuestionnaireResults()))
                .riskFactors(JsonUtils.toJsonString(resultVO.getRiskFactors()))
                .interventionSuggestions(JsonUtils.toJsonString(resultVO.getInterventionSuggestions()))
                .generationConfigVersion(context.getConfigVersion())
                .build();

        // 4) 幂等保存：根据 (taskNo, participantId, dimensionCode) 存在则更新，否则插入
        AssessmentResultDO exist = assessmentResultMapper.selectOne(
            new LambdaQueryWrapperX<AssessmentResultDO>()
                .eq(AssessmentResultDO::getTaskNo, taskNo)
                .eq(AssessmentResultDO::getParticipantId, participantId)
                .eq(AssessmentResultDO::getDimensionCode, save.getDimensionCode())
        );
        if (exist == null) {
            assessmentResultMapper.insert(save);
            log.info("已保存组合测评结果(新增), participantId={}, id={}", participantId, save.getId());
        } else {
            save.setId(exist.getId());
            assessmentResultMapper.updateById(save);
            log.info("已保存组合测评结果(更新), participantId={}, id={}", participantId, save.getId());
        }
        return save.getId();
    }

    @Override
    public AssessmentResultDetailRespVO getAssessmentResult(String taskNo, Long studentProfileId) {
        // 1. 查询测评结果基本信息 - 通过taskNo和studentProfileId查询
        // 注意：这里的participantId存储的是学生档案ID
        List<AssessmentResultDO> assessmentResults = assessmentResultMapper.selectList(
            new LambdaQueryWrapperX<AssessmentResultDO>()
                .eq(AssessmentResultDO::getTaskNo, taskNo)  // 直接通过taskNo字段查询
                .eq(AssessmentResultDO::getParticipantId, studentProfileId)
                .orderByDesc(AssessmentResultDO::getCreateTime)
                .last("LIMIT 1")
        );

        if (assessmentResults.isEmpty()) {
            log.warn("未找到测评结果, taskNo={}, studentProfileId={}", taskNo, studentProfileId);
            return null;
        }

        AssessmentResultDO assessmentResult = assessmentResults.get(0);

        // 2. 构建响应VO
        AssessmentResultDetailRespVO respVO = new AssessmentResultDetailRespVO();
        respVO.setId(assessmentResult.getId());
        respVO.setParticipantId(assessmentResult.getParticipantId());
        respVO.setTaskNo(assessmentResult.getTaskNo());
        respVO.setDimensionCode(assessmentResult.getDimensionCode());
        respVO.setScore(assessmentResult.getScore());
        respVO.setSuggestion(assessmentResult.getSuggestion());
        respVO.setCombinedRiskLevel(assessmentResult.getCombinedRiskLevel());
        respVO.setRiskLevelDescription(getRiskLevelDescription(assessmentResult.getCombinedRiskLevel()));
        respVO.setRiskFactors(assessmentResult.getRiskFactors());
        respVO.setInterventionSuggestions(assessmentResult.getInterventionSuggestions());
        respVO.setGenerationConfigVersion(assessmentResult.getGenerationConfigVersion());

        // 生成当前风险等级的结构化干预建议
        respVO.setRiskLevelIntervention(generateCurrentRiskLevelIntervention(assessmentResult.getCombinedRiskLevel()));
        respVO.setCreateTime(assessmentResult.getCreateTime());
        respVO.setUpdateTime(assessmentResult.getUpdateTime());

        // 3. 解析问卷结果JSON并添加问卷名称和答题结果
        List<AssessmentResultDetailRespVO.QuestionnaireResultDetailVO> questionnaireResults = new ArrayList<>();

        // 首先获取userId用于查询原始问卷结果
        StudentProfileDO studentProfile = studentProfileMapper.selectById(studentProfileId);
        Long userId = studentProfile != null ? studentProfile.getUserId() : null;

        if (assessmentResult.getQuestionnaireResults() != null && userId != null) {
            try {
                List<QuestionnaireResultVO> resultVOList = JsonUtils.parseArray(
                    assessmentResult.getQuestionnaireResults(), QuestionnaireResultVO.class);

                for (QuestionnaireResultVO resultVO : resultVOList) {
                    AssessmentResultDetailRespVO.QuestionnaireResultDetailVO detailVO =
                        new AssessmentResultDetailRespVO.QuestionnaireResultDetailVO();

                    detailVO.setQuestionnaireId(resultVO.getQuestionnaireId());
                    detailVO.setRawScore(resultVO.getRawScore());
                    detailVO.setStandardScore(resultVO.getStandardScore());
                    detailVO.setRiskLevel(resultVO.getRiskLevel());
                    detailVO.setLevelDescription(resultVO.getLevelDescription());
                    detailVO.setSuggestions(resultVO.getSuggestions());
                    detailVO.setReportContent(resultVO.getReportContent());
                    detailVO.setPercentileRank(resultVO.getPercentileRank());

                    // 转换维度得分为JSON字符串
                    if (resultVO.getDimensionScores() != null) {
                        detailVO.setDimensionScores(JsonUtils.toJsonString(resultVO.getDimensionScores()));
                    }

                    // 获取问卷名称
                    QuestionnaireDO questionnaire = questionnaireMapper.selectById(resultVO.getQuestionnaireId());
                    if (questionnaire != null) {
                        detailVO.setQuestionnaireName(questionnaire.getTitle());
                    } else {
                        detailVO.setQuestionnaireName("未知问卷");
                    }

                    // 查询原始问卷结果获取答题详情
                    List<QuestionnaireResultDO> originalResults = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, userId);
                    for (QuestionnaireResultDO originalResult : originalResults) {
                        if (originalResult.getQuestionnaireId().equals(resultVO.getQuestionnaireId())) {
                            detailVO.setAnswers(originalResult.getAnswers());
                            detailVO.setResultId(originalResult.getId());
                            detailVO.setCompletedTime(originalResult.getCompletedTime());
                            detailVO.setGenerationStatus(originalResult.getGenerationStatus());
                            detailVO.setGenerationStatusDescription(getGenerationStatusDescription(originalResult.getGenerationStatus()));
                            break;
                        }
                    }

                    questionnaireResults.add(detailVO);
                }
            } catch (Exception e) {
                log.error("解析问卷结果JSON失败, taskNo={}, studentProfileId={}", taskNo, studentProfileId, e);
            }
        }

        respVO.setQuestionnaireResults(questionnaireResults);

        log.info("获取测评结果详情成功, taskNo={}, studentProfileId={}, 包含{}个问卷结果",
            taskNo, studentProfileId, questionnaireResults.size());
        return respVO;
    }

    /**
     * 生成当前风险等级的结构化干预建议
     */
    private RiskLevelInterventionVO generateCurrentRiskLevelIntervention(Integer currentRiskLevel) {
        List<RiskLevelInterventionVO> interventions = RiskLevelInterventionVO.getStandardInterventions();

        // 找到当前风险等级对应的干预建议
        RiskLevelInterventionVO currentIntervention = interventions.stream()
            .filter(intervention -> intervention.getRiskLevel().equals(currentRiskLevel))
            .findFirst()
            .orElse(null);

        if (currentIntervention != null) {
            currentIntervention.setIsCurrent(true);
        }

        return currentIntervention;
    }

    /**
     * 获取风险等级描述
     */
    private String getRiskLevelDescription(Integer riskLevel) {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case 1:
                return "正常";
            case 2:
                return "关注";
            case 3:
                return "预警";
            case 4:
                return "高风险";
            default:
                return "未知";
        }
    }

    /**
     * 获取生成状态描述
     */
    private String getGenerationStatusDescription(Integer generationStatus) {
        if (generationStatus == null) {
            return "未知";
        }
        switch (generationStatus) {
            case 0:
                return "待生成";
            case 1:
                return "生成中";
            case 2:
                return "已生成";
            case 3:
                return "生成失败";
            default:
                return "未知";
        }
    }
}

