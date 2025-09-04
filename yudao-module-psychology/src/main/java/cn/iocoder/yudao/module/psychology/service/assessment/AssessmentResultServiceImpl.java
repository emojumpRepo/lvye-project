package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentResultMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentUserTaskMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
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
    private ResultGeneratorFactory resultGeneratorFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateAndSaveCombinedResult(String taskNo, Long userId) {
        // 1) 拉取该任务下该用户的全部问卷结果
        List<QuestionnaireResultDO> resultDOList = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, userId);
        log.info("查询问卷结果, taskNo={}, userId={}, 查询到{}条记录", taskNo, userId,
                 resultDOList != null ? resultDOList.size() : 0);

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
            log.info("无问卷结果可用于生成测评结果, taskNo={}, userId={}", taskNo, userId);
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

        // 获取参与者记录，以便拿到 participantId
        AssessmentUserTaskDO userTask = assessmentUserTaskMapper.selectByTaskNoAndUserId(taskNo, userId);
        Long participantId = userTask != null ? userTask.getId() : null;

        // 2) 调用生成器生成组合测评结果
        ResultGenerationContext context = ResultGenerationContext.builder()
                .generationType(ResultGeneratorTypeEnum.COMBINED_ASSESSMENT)
                .assessmentId(userTask != null ? userTask.getId() : null) // 暂以参与者任务ID代替assessmentId概念
                .userId(userId)
                .questionnaireResults(questionnaireResults)
                .build();
        AssessmentResultVO resultVO = resultGeneratorFactory.generateResult(ResultGeneratorTypeEnum.COMBINED_ASSESSMENT, context);

        // 3) 将 VO 映射到 DO（综合报告由生成器统一拼接 evaluate_config 结果）
        AssessmentResultDO save = AssessmentResultDO.builder()
                .participantId(participantId)
                .dimensionCode("total")
                .score(resultVO.getCombinedScore() == null ? null : resultVO.getCombinedScore().intValue())
                .combinedRiskLevel(resultVO.getCombinedRiskLevel())
                .suggestion(resultVO.getComprehensiveReport())
                .questionnaireResults(JsonUtils.toJsonString(resultVO.getQuestionnaireResults()))
                .riskFactors(JsonUtils.toJsonString(resultVO.getRiskFactors()))
                .interventionSuggestions(JsonUtils.toJsonString(resultVO.getInterventionSuggestions()))
                .generationConfigVersion(context.getConfigVersion())
                .build();

        // 4) 幂等保存：根据 (participantId, dimensionCode) 存在则更新，否则插入
        AssessmentResultDO exist = assessmentResultMapper.selectByParticipantAndDim(participantId, save.getDimensionCode());
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
}

