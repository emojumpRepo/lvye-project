package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.enums.QuestionnaireStatusEnum;
import cn.iocoder.yudao.module.psychology.framework.config.SurveySystemProperties;
import cn.iocoder.yudao.module.psychology.framework.survey.client.SurveySystemClient;
import cn.iocoder.yudao.module.psychology.framework.survey.util.SurveyDataConverter;
import cn.iocoder.yudao.module.psychology.framework.survey.util.SurveyStatusComparator;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyQuestionRespVO;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 问卷同步服务实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class QuestionnaireSyncServiceImpl implements QuestionnaireSyncService {

    @Resource
    private SurveySystemClient surveySystemClient;

    @Resource
    private QuestionnaireMapper questionnaireMapper;

    @Resource
    private SurveySystemProperties surveySystemProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionnaireSyncResult syncQuestionnaires() {
        log.info("[syncQuestionnaires] 开始同步外部问卷系统数据");
        
        QuestionnaireSyncResult result = new QuestionnaireSyncResult();
        
        try {
            // 1. 获取外部问卷系统的问卷列表
            List<ExternalSurveyRespVO> externalSurveys = surveySystemClient.getSurveyListWithRetry();
            log.debug("[syncQuestionnaires] 获取到外部问卷列表，数量: {}", externalSurveys != null ? externalSurveys.size() : 0);
            
            if (CollectionUtils.isEmpty(externalSurveys)) {
                log.warn("[syncQuestionnaires] 外部问卷系统返回空列表");
                result.setSuccess(true);
                return result;
            }

            // 2. 获取本地所有问卷数据
            List<QuestionnaireDO> localQuestionnaires = questionnaireMapper.selectList();
            
            // 3. 构建本地问卷的外部ID映射
            Map<String, QuestionnaireDO> localQuestionnaireMap = new HashMap<>();
            for (QuestionnaireDO questionnaire : localQuestionnaires) {
                if (StringUtils.hasText(questionnaire.getExternalId())) {
                    localQuestionnaireMap.put(questionnaire.getExternalId(), questionnaire);
                }
            }

            // 4. 获取外部问卷的ID集合
            Set<String> externalIds = externalSurveys.stream()
                    .map(ExternalSurveyRespVO::getSurveyMetaId)
                    .collect(Collectors.toSet());

            // 5. 处理外部问卷数据
            for (ExternalSurveyRespVO externalSurvey : externalSurveys) {
                QuestionnaireDO localQuestionnaire = localQuestionnaireMap.get(externalSurvey.getSurveyMetaId());
                
                if (localQuestionnaire == null) {
                    // 新增问卷
                    if (createNewQuestionnaire(externalSurvey)) {
                        result.setNewAdded(result.getNewAdded() + 1);
                    } else {
                        result.setFailed(result.getFailed() + 1);
                    }
                } else {
                    // 更新现有问卷
                    int updateResult = updateExistingQuestionnaire(localQuestionnaire, externalSurvey);
                    if (updateResult == 1) {
                        result.setUpdated(result.getUpdated() + 1);
                    } else if (updateResult == -1) {
                        result.setFailed(result.getFailed() + 1);
                    }
                    // updateResult == 0 表示无需更新，不计入任何统计
                }
            }

            // 6. 标记本地存在但外部不存在的问卷为失效状态
            for (QuestionnaireDO localQuestionnaire : localQuestionnaires) {
                if (StringUtils.hasText(localQuestionnaire.getExternalId())) {
                    String externalId = localQuestionnaire.getExternalId();
                    
                    if (!externalIds.contains(externalId) && 
                        !QuestionnaireStatusEnum.CLOSED.getStatus().equals(localQuestionnaire.getStatus())) {
                        // 标记为关闭
                        localQuestionnaire.setStatus(QuestionnaireStatusEnum.CLOSED.getStatus());
                        localQuestionnaire.setUpdateTime(LocalDateTime.now());
                        questionnaireMapper.updateById(localQuestionnaire);
                        result.setInvalidated(result.getInvalidated() + 1);
                        
                        log.info("[syncQuestionnaires] 标记问卷为关闭状态，ID: {}, 标题: {}", 
                                localQuestionnaire.getId(), localQuestionnaire.getTitle());
                    }
                }
            }

            result.setTotalProcessed(externalSurveys.size());
            result.setSuccess(true);
            
            log.info("[syncQuestionnaires] 同步完成，结果: {}", result);
            
        } catch (Exception e) {
            log.error("[syncQuestionnaires] 同步过程中发生异常", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }

    @Override
    public ExternalSurveyQuestionRespVO getSurveyQuestions(String surveyId) {
        try {
            return surveySystemClient.getSurveyQuestion(surveyId);
        } catch (Exception e) {
            log.error("[getSurveyQuestions] 获取外部问卷题目失败，surveyId: {}", surveyId, e);
            return null;
        }
    }

    /**
     * 创建新问卷
     * 
     * @param externalSurvey 外部问卷数据
     * @return true-创建成功，false-创建失败
     */
    private boolean createNewQuestionnaire(ExternalSurveyRespVO externalSurvey) {
        try {
            QuestionnaireDO questionnaire = new QuestionnaireDO();

            // 设置必填字段，提供默认值防止null
            questionnaire.setTitle(StringUtils.hasText(externalSurvey.getTitle()) ?
                    externalSurvey.getTitle() : "未命名问卷");
            questionnaire.setDescription(SurveyDataConverter.generateDescription(externalSurvey));
            questionnaire.setExternalLink(externalSurvey.getSurveyPath());
            questionnaire.setExternalId(externalSurvey.getSurveyMetaId());
            questionnaire.setSurveyCode(externalSurvey.getSurveyCode());

            // 设置其他字段，使用转换工具类
            questionnaire.setQuestionnaireType(SurveyDataConverter.convertSurveyType(externalSurvey.getSurveyType()));
            questionnaire.setQuestionCount(externalSurvey.getQuestionCount());
            questionnaire.setStatus(SurveyDataConverter.convertStatus(externalSurvey));
            questionnaire.setTargetAudience(SurveyDataConverter.generateTargetAudience(externalSurvey));
            questionnaire.setEstimatedDuration(SurveyDataConverter.estimateDuration(externalSurvey.getSurveyType()));
            questionnaire.setAccessCount(0);
            questionnaire.setCompletionCount(externalSurvey.getSubmitCount() != null ? externalSurvey.getSubmitCount() : 0);
            questionnaire.setSupportIndependentUse(1);
            questionnaire.setCreateTime(LocalDateTime.now());
            questionnaire.setUpdateTime(LocalDateTime.now());
            questionnaire.setSyncStatus(1); // 设置为已同步
            questionnaire.setLastSyncTime(LocalDateTime.now());

            questionnaireMapper.insert(questionnaire);

            log.info("[createNewQuestionnaire] 新增问卷成功，外部ID: {}, 标题: {}, 状态: {}, 类型: {}, 完成次数: {}",
                    externalSurvey.getSurveyMetaId(), questionnaire.getTitle(),
                    externalSurvey.getCurrentStatus(), externalSurvey.getSurveyType(),
                    externalSurvey.getSubmitCount());
            
            return true;
            
        } catch (Exception e) {
            log.error("[createNewQuestionnaire] 新增问卷失败，外部ID: {}, 错误: {}", 
                    externalSurvey.getSurveyMetaId(), e.getMessage(), e);
            
            // 尝试创建失败记录
            try {
                QuestionnaireDO failedQuestionnaire = new QuestionnaireDO();
                failedQuestionnaire.setTitle(StringUtils.hasText(externalSurvey.getTitle()) ?
                        externalSurvey.getTitle() : "未命名问卷");
                failedQuestionnaire.setExternalId(externalSurvey.getSurveyMetaId());
                failedQuestionnaire.setDescription("同步失败：" + e.getMessage());
                failedQuestionnaire.setQuestionnaireType(1); // 默认类型
                failedQuestionnaire.setTargetAudience(1); // 默认学生
                failedQuestionnaire.setStatus(0); // 草稿状态
                failedQuestionnaire.setSupportIndependentUse(1); // 不开放
                failedQuestionnaire.setAccessCount(0);
                failedQuestionnaire.setCompletionCount(0);
                failedQuestionnaire.setCreateTime(LocalDateTime.now());
                failedQuestionnaire.setUpdateTime(LocalDateTime.now());
                failedQuestionnaire.setSyncStatus(2); // 设置为同步失败
                failedQuestionnaire.setLastSyncTime(LocalDateTime.now());
                
                questionnaireMapper.insert(failedQuestionnaire);
                log.info("[createNewQuestionnaire] 已创建失败记录，外部ID: {}", externalSurvey.getSurveyMetaId());
                
            } catch (Exception insertFailedException) {
                log.error("[createNewQuestionnaire] 创建失败记录也失败，外部ID: {}, 错误: {}", 
                        externalSurvey.getSurveyMetaId(), insertFailedException.getMessage());
            }
            
            return false;
        }
    }

    /**
     * 更新现有问卷
     * 
     * @param localQuestionnaire 本地问卷
     * @param externalSurvey 外部问卷数据
     * @return 1-更新成功，0-无需更新，-1-更新失败
     */
    private int updateExistingQuestionnaire(QuestionnaireDO localQuestionnaire, ExternalSurveyRespVO externalSurvey) {
        try {
            boolean needUpdate = false;

            // 生成新的字段值
            String newTitle = StringUtils.hasText(externalSurvey.getTitle()) ? externalSurvey.getTitle() : "未命名问卷";
            String newExternalLink = externalSurvey.getSurveyPath();
            String newSurveyCode = externalSurvey.getSurveyCode();
            Integer newQuestionnaireType = SurveyDataConverter.convertSurveyType(externalSurvey.getSurveyType());
            Integer newStatus = SurveyDataConverter.convertStatus(externalSurvey);
            Integer newTargetAudience = SurveyDataConverter.generateTargetAudience(externalSurvey);
            Integer newEstimatedDuration = SurveyDataConverter.estimateDuration(externalSurvey.getSurveyType());
            Integer newCompletionCount = externalSurvey.getSubmitCount() != null ? externalSurvey.getSubmitCount() : 0;
            Integer newIsOpen = SurveyDataConverter.isOpen(externalSurvey);
            Integer newQuestionCount = externalSurvey.getQuestionCount() != null ? externalSurvey.getQuestionCount() : 0;

            // 使用状态比较工具检查状态变化
            SurveyStatusComparator.StatusChangeResult statusChangeResult =
                    SurveyStatusComparator.compareStatus(localQuestionnaire, externalSurvey);

            // 检查是否需要更新
            if (!Objects.equals(localQuestionnaire.getTitle(), newTitle) ||
                !Objects.equals(localQuestionnaire.getExternalLink(), newExternalLink) ||
                !Objects.equals(localQuestionnaire.getSurveyCode(), newSurveyCode) ||
                !Objects.equals(localQuestionnaire.getQuestionnaireType(), newQuestionnaireType) ||
                !Objects.equals(localQuestionnaire.getStatus(), newStatus) ||
                !Objects.equals(localQuestionnaire.getTargetAudience(), newTargetAudience) ||
                !Objects.equals(localQuestionnaire.getEstimatedDuration(), newEstimatedDuration) ||
                !Objects.equals(localQuestionnaire.getCompletionCount(), newCompletionCount) ||
                !Objects.equals(localQuestionnaire.getQuestionCount(), newQuestionCount)
            ) {

                needUpdate = true;
            }

            if (needUpdate) {
                // 更新字段
                localQuestionnaire.setTitle(newTitle);
                localQuestionnaire.setExternalLink(newExternalLink);
                localQuestionnaire.setQuestionnaireType(newQuestionnaireType);
                localQuestionnaire.setStatus(newStatus);
                localQuestionnaire.setTargetAudience(newTargetAudience);
                localQuestionnaire.setEstimatedDuration(newEstimatedDuration);
                localQuestionnaire.setCompletionCount(newCompletionCount);
                localQuestionnaire.setUpdateTime(LocalDateTime.now());
                localQuestionnaire.setSyncStatus(1); // 更新同步状态
                localQuestionnaire.setLastSyncTime(LocalDateTime.now());
                localQuestionnaire.setQuestionCount(newQuestionCount);
                localQuestionnaire.setSurveyCode(newSurveyCode);

                questionnaireMapper.updateById(localQuestionnaire);

                // 增强的日志记录
                StringBuilder logMessage = new StringBuilder();
                logMessage.append(String.format("[updateExistingQuestionnaire] 更新问卷成功，ID: %d, 标题: %s",
                        localQuestionnaire.getId(), localQuestionnaire.getTitle()));

                if (statusChangeResult.isChanged()) {
                    logMessage.append(String.format(", 状态变化: %s", statusChangeResult.getChangeDescription()));
                }

                logMessage.append(String.format(", 完成次数: %d", newCompletionCount));

                // 添加暂停状态的特殊标记
                if (externalSurvey.isPaused()) {
                    logMessage.append(", [暂停状态检测]");
                }

                log.info(logMessage.toString());
                return 1; // 更新成功
            }

            return 0; // 无需更新
            
        } catch (Exception e) {
            log.error("[updateExistingQuestionnaire] 更新问卷失败，ID: {}, 外部ID: {}, 错误: {}", 
                    localQuestionnaire.getId(), externalSurvey.getSurveyMetaId(), e.getMessage(), e);
            
            // 标记为同步失败
            try {
                localQuestionnaire.setSyncStatus(2); // 设置为同步失败
                localQuestionnaire.setLastSyncTime(LocalDateTime.now());
                localQuestionnaire.setUpdateTime(LocalDateTime.now());
                questionnaireMapper.updateById(localQuestionnaire);
                
                log.info("[updateExistingQuestionnaire] 已标记问卷为同步失败，ID: {}, 外部ID: {}", 
                        localQuestionnaire.getId(), externalSurvey.getSurveyMetaId());
                
            } catch (Exception markFailedException) {
                log.error("[updateExistingQuestionnaire] 标记同步失败状态也失败，ID: {}, 错误: {}", 
                        localQuestionnaire.getId(), markFailedException.getMessage());
            }
            
            return -1; // 更新失败
        }
    }

}
