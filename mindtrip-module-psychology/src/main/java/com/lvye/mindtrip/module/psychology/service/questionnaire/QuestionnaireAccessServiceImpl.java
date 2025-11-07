package com.lvye.mindtrip.module.psychology.service.questionnaire;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireAccessDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire.QuestionnaireAccessMapper;
import com.lvye.mindtrip.module.psychology.enums.ErrorCodeConstants;
import com.lvye.mindtrip.module.psychology.enums.QuestionnaireStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

import static com.lvye.mindtrip.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 问卷访问服务实现（简化版本）
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class QuestionnaireAccessServiceImpl implements QuestionnaireAccessService {

    @Autowired
    private QuestionnaireAccessMapper questionnaireAccessMapper;

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Override
    public QuestionnaireAccessService.QuestionnaireAvailabilityResult checkQuestionnaireAvailability(Long questionnaireId) {
        log.info("检查问卷可用性（简化实现），问卷ID: {}", questionnaireId);

        try {
            QuestionnaireDO questionnaire = validateQuestionnaireExists(questionnaireId);
            boolean available = QuestionnaireStatusEnum.PUBLISHED.getStatus().equals(questionnaire.getStatus());
            String reason = available ? "问卷可用" : "问卷未发布";
            return new QuestionnaireAccessService.QuestionnaireAvailabilityResult(available, reason);
        } catch (Exception e) {
            log.error("检查问卷可用性失败", e);
            return new QuestionnaireAccessService.QuestionnaireAvailabilityResult(false, "问卷不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordQuestionnaireAccess(Long questionnaireId, Long userId,
                                         String accessIp, String userAgent, Integer accessSource) {
        log.info("记录问卷访问（简化实现），问卷ID: {}, 用户ID: {}, IP: {}", questionnaireId, userId, accessIp);
        
        // 验证问卷存在
        QuestionnaireDO questionnaire = validateQuestionnaireExists(questionnaireId);
        
        // 检查问卷状态
        if (!QuestionnaireStatusEnum.PUBLISHED.getStatus().equals(questionnaire.getStatus())) {
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        
        // 创建访问记录
        QuestionnaireAccessDO accessRecord = new QuestionnaireAccessDO();
        accessRecord.setQuestionnaireId(questionnaireId);
        accessRecord.setUserId(userId);
        accessRecord.setAccessIp(accessIp);
        accessRecord.setUserAgent(userAgent);
        accessRecord.setAccessSource(accessSource);
        accessRecord.setAccessTime(LocalDateTime.now());
        
        questionnaireAccessMapper.insert(accessRecord);
        
        // 更新问卷访问次数
        questionnaireMapper.updateAccessCount(questionnaireId);
        
        return accessRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSessionDuration(Long accessId, Integer sessionDuration) {
        log.info("更新会话时长（简化实现），访问ID: {}, 会话时长: {}秒", accessId, sessionDuration);

        QuestionnaireAccessDO accessRecord = questionnaireAccessMapper.selectById(accessId);
        if (accessRecord != null) {
            accessRecord.setSessionDuration(sessionDuration);
            questionnaireAccessMapper.updateById(accessRecord);
        }
    }

    @Override
    public boolean checkQuestionnaireAccess(Long questionnaireId, Long userId) {
        log.info("检查访问权限（简化实现），问卷ID: {} 用户ID: {}", questionnaireId, userId);
        try {
            QuestionnaireDO questionnaire = validateQuestionnaireExists(questionnaireId);
            // 简化策略：仅当问卷已发布才允许访问
            return QuestionnaireStatusEnum.PUBLISHED.getStatus().equals(questionnaire.getStatus());
        } catch (Exception e) {
            log.warn("检查访问权限失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public QuestionnaireAccessService.QuestionnaireAccessStats getQuestionnaireAccessStats(Long questionnaireId,
                                                               LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取问卷访问统计（简化实现），问卷ID: {}, 时间范围: {} - {}", questionnaireId, startTime, endTime);

        QuestionnaireAccessService.QuestionnaireAccessStats stats = new QuestionnaireAccessService.QuestionnaireAccessStats();
        
        try {
            // 使用基础查询替代复杂的自定义查询
            Long totalAccess = questionnaireAccessMapper.selectCount(
                new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                    .eq(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId)
                    .between(QuestionnaireAccessDO::getAccessTime, startTime, endTime)
            );
            stats.setTotalAccess(totalAccess);
            
            // 简化实现：独立用户数暂时等于总访问数
            stats.setUniqueUsers(totalAccess);
            
            // 简化实现：平均会话时长设为默认值
            stats.setAverageSessionDuration(0.0);
            
            // 简化实现：完成次数设为0
            stats.setCompletionCount(0L);
            
            // 完成率
            if (totalAccess > 0) {
                stats.setCompletionRate((double) stats.getCompletionCount() / totalAccess * 100);
            } else {
                stats.setCompletionRate(0.0);
            }

            // 增加今日、本周、本月访问统计（简化实现）
            stats.setTodayAccess(getTodayAccessCount(questionnaireId));
            stats.setWeekAccess(getThisWeekAccessCount(questionnaireId));
            stats.setMonthAccess(0L); // TODO: 可按需实现本月统计
            
        } catch (Exception e) {
            log.error("获取问卷访问统计失败", e);
            // 返回默认统计数据
            stats.setTotalAccess(0L);
            stats.setUniqueUsers(0L);
            stats.setAverageSessionDuration(0.0);
            stats.setCompletionCount(0L);
            stats.setCompletionRate(0.0);
            stats.setTodayAccess(0L);
            stats.setWeekAccess(0L);
            stats.setMonthAccess(0L);
        }
        
        return stats;
    }

    public Long getTodayAccessCount(Long questionnaireId) {
        log.info("获取今日访问次数（简化实现），问卷ID: {}", questionnaireId);
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return questionnaireAccessMapper.selectCount(
            new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                .eq(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId)
                .between(QuestionnaireAccessDO::getAccessTime, startOfDay, endOfDay)
        );
    }

    public Long getYesterdayAccessCount(Long questionnaireId) {
        log.info("获取昨日访问次数（简化实现），问卷ID: {}", questionnaireId);
        
        LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = startOfYesterday.plusDays(1);
        
        return questionnaireAccessMapper.selectCount(
            new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                .eq(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId)
                .between(QuestionnaireAccessDO::getAccessTime, startOfYesterday, endOfYesterday)
        );
    }

    public Long getThisWeekAccessCount(Long questionnaireId) {
        log.info("获取本周访问次数（简化实现），问卷ID: {}", questionnaireId);
        
        LocalDateTime startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        
        return questionnaireAccessMapper.selectCount(
            new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                .eq(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId)
                .between(QuestionnaireAccessDO::getAccessTime, startOfWeek, now)
        );
    }

    @Override
    public List<Map<String, Object>> getQuestionnaireAccessTrend(Long questionnaireId, Integer days) {
        log.info("获取问卷访问趋势（简化实现），问卷ID: {}, 天数: {}", questionnaireId, days);

        // TODO: 实现具体的趋势数据分析逻辑
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getPopularQuestionnaires(Integer limit, Integer days) {
        log.info("获取热门问卷（简化实现），限制数量: {}, 天数: {}", limit, days);

        // TODO: 实现具体的热门问卷查询逻辑
        return new ArrayList<>();
    }

    @Override
    public PageResult<QuestionnaireAccessDO> getQuestionnaireAccessPage(Object pageReqVO) {
        log.info("获取问卷访问记录分页（简化实现）");

        // TODO: 实现具体的分页查询逻辑
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public List<QuestionnaireAccessDO> getQuestionnaireAccessList(Long questionnaireId, Long userId) {
        log.info("获取问卷访问记录列表（简化实现），问卷ID: {}, 用户ID: {}", questionnaireId, userId);

        return questionnaireAccessMapper.selectList(
            new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                .eq(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId)
                .eqIfPresent(QuestionnaireAccessDO::getUserId, userId)
                .orderByDesc(QuestionnaireAccessDO::getAccessTime)
        );
    }

    /**
     * 验证问卷存在
     */
    private QuestionnaireDO validateQuestionnaireExists(Long questionnaireId) {
        QuestionnaireDO questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_NOT_EXISTS);
        }
        return questionnaire;
    }

}
