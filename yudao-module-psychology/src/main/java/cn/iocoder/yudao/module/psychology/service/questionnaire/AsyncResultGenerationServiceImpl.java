package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGenerationContext;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.ResultGeneratorFactory;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.AnswerVO;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.QuestionnaireResultVO;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 异步结果生成服务实现
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class AsyncResultGenerationServiceImpl implements AsyncResultGenerationService {

    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;

    @Resource
    private QuestionnaireMapper questionnaireMapper;

    @Resource
    private ResultGeneratorFactory resultGeneratorFactory;

    // 任务状态缓存
    private final ConcurrentMap<Long, TaskStatus> taskStatusCache = new ConcurrentHashMap<>();

    @Override
    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void generateResultAsync(QuestionnaireResultDO result) {
        Long resultId = result.getId();
        log.info("开始异步生成问卷结果，结果ID: {}", resultId);
        
        try {
            // 更新任务状态
            taskStatusCache.put(resultId, TaskStatus.RUNNING);
            updateResultStatus(resultId, 2, "开始生成结果", null);
            
            // 获取问卷信息
            QuestionnaireDO questionnaire = questionnaireMapper.selectById(result.getQuestionnaireId());
            if (questionnaire == null) {
                throw new RuntimeException("问卷不存在，ID: " + result.getQuestionnaireId());
            }
            
            // 创建结果生成上下文（新框架）
            ResultGenerationContext context = ResultGenerationContext.builder()
                    .generationType(ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE)
                    .questionnaireId(result.getQuestionnaireId())
                    .userId(result.getUserId())
                    .answers(JsonUtils.parseArray(result.getAnswers(), AnswerVO.class))
                    .participantType(result.getParticipantType())
                    .build();

            // 生成结果
            updateResultStatus(resultId, 2, "正在分析答案", null);
            QuestionnaireResultVO generatedResult = resultGeneratorFactory.generateResult(
                    ResultGeneratorTypeEnum.SINGLE_QUESTIONNAIRE, context);

            // 更新结果到数据库
            updateResultStatus(resultId, 2, "正在保存结果", null);
            updateQuestionnaireResult(resultId, generatedResult);
            
            // 标记完成
            updateResultStatus(resultId, 3, "生成完成", null);
            taskStatusCache.put(resultId, TaskStatus.COMPLETED);
            
            log.info("异步生成问卷结果完成，结果ID: {}", resultId);
            
        } catch (Exception e) {
            log.error("异步生成问卷结果失败，结果ID: {}", resultId, e);
            
            // 标记失败
            updateResultStatus(resultId, 4, "生成失败", e.getMessage());
            taskStatusCache.put(resultId, TaskStatus.FAILED);
            
            throw e;
        }
    }

    @Override
    public TaskStatus checkTaskStatus(Long resultId) {
        // 先从缓存中获取
        TaskStatus cachedStatus = taskStatusCache.get(resultId);
        if (cachedStatus != null) {
            return cachedStatus;
        }
        
        // 从数据库获取状态
        QuestionnaireResultDO result = questionnaireResultMapper.selectById(resultId);
        if (result == null) {
            return TaskStatus.FAILED;
        }
        
        TaskStatus status;
        switch (result.getGenerationStatus()) {
            case 1:
                status = TaskStatus.PENDING;
                break;
            case 2:
                status = TaskStatus.RUNNING;
                break;
            case 3:
                status = TaskStatus.COMPLETED;
                break;
            case 4:
                status = TaskStatus.FAILED;
                break;
            default:
                status = TaskStatus.PENDING;
        }
        
        // 更新缓存
        taskStatusCache.put(resultId, status);
        return status;
    }

    @Override
    public boolean cancelTask(Long resultId) {
        log.info("取消生成任务，结果ID: {}", resultId);
        
        try {
            TaskStatus currentStatus = checkTaskStatus(resultId);
            
            // 只有待处理和运行中的任务可以取消
            if (currentStatus == TaskStatus.PENDING || currentStatus == TaskStatus.RUNNING) {
                updateResultStatus(resultId, 4, "任务已取消", "用户取消");
                taskStatusCache.put(resultId, TaskStatus.CANCELLED);
                
                log.info("取消生成任务成功，结果ID: {}", resultId);
                return true;
            } else {
                log.warn("任务状态不允许取消，结果ID: {}, 状态: {}", resultId, currentStatus);
                return false;
            }
            
        } catch (Exception e) {
            log.error("取消生成任务失败，结果ID: {}", resultId, e);
            return false;
        }
    }

    @Override
    public boolean retryTask(Long resultId) {
        log.info("重试生成任务，结果ID: {}", resultId);
        
        try {
            TaskStatus currentStatus = checkTaskStatus(resultId);
            
            // 只有失败和取消的任务可以重试
            if (currentStatus == TaskStatus.FAILED || currentStatus == TaskStatus.CANCELLED) {
                QuestionnaireResultDO result = questionnaireResultMapper.selectById(resultId);
                if (result != null) {
                    // 重置状态
                    updateResultStatus(resultId, 1, "准备重试", null);
                    taskStatusCache.put(resultId, TaskStatus.PENDING);
                    
                    // 重新提交异步任务
                    generateResultAsync(result);
                    
                    log.info("重试生成任务成功，结果ID: {}", resultId);
                    return true;
                }
            } else {
                log.warn("任务状态不允许重试，结果ID: {}, 状态: {}", resultId, currentStatus);
                return false;
            }
            
        } catch (Exception e) {
            log.error("重试生成任务失败，结果ID: {}", resultId, e);
            return false;
        }
        
        return false;
    }

    /**
     * 更新结果状态
     */
    private void updateResultStatus(Long resultId, Integer status, String message, String errorMessage) {
        QuestionnaireResultDO updateResult = new QuestionnaireResultDO();
        updateResult.setId(resultId);
        updateResult.setGenerationStatus(status);
        
        if (status == 3) { // 完成
            updateResult.setGenerationTime(LocalDateTime.now());
        }
        
        if (errorMessage != null) {
            // 修复字段名：QuestionnaireResultDO 使用 generationError 存储错误信息
            updateResult.setGenerationError(errorMessage);
        }
        
        questionnaireResultMapper.updateById(updateResult);
        
        log.debug("更新结果状态，结果ID: {}, 状态: {}, 消息: {}", resultId, status, message);
    }

    /**
     * 更新问卷结果
     */
    private void updateQuestionnaireResult(Long resultId, QuestionnaireResultVO resultVO) {
        QuestionnaireResultDO updateResult = new QuestionnaireResultDO();
        updateResult.setId(resultId);
        updateResult.setRawScore(resultVO.getRawScore());
        updateResult.setStandardScore(resultVO.getStandardScore());
        updateResult.setPercentileRank(resultVO.getPercentileRank());
        updateResult.setRiskLevel(resultVO.getRiskLevel());
        updateResult.setLevelDescription(resultVO.getLevelDescription());
        updateResult.setDimensionScores(JsonUtils.toJsonString(resultVO.getDimensionScores()));
        updateResult.setReportContent(resultVO.getReportContent());
        updateResult.setSuggestions(resultVO.getSuggestions());
        updateResult.setGenerationTime(LocalDateTime.now());

        questionnaireResultMapper.updateById(updateResult);

        log.debug("更新问卷结果数据，结果ID: {}", resultId);
    }

}