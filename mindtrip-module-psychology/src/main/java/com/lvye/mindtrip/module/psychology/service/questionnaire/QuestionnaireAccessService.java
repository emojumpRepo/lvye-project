package com.lvye.mindtrip.module.psychology.service.questionnaire;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireAccessDO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问卷访问服务接口
 *
 * @author 芋道源码
 */
public interface QuestionnaireAccessService {

    /**
     * 记录问卷访问
     *
     * @param questionnaireId 问卷ID
     * @param userId 用户ID
     * @param accessIp 访问IP
     * @param userAgent 用户代理
     * @param accessSource 访问来源
     * @return 访问记录ID
     */
    Long recordQuestionnaireAccess(Long questionnaireId, Long userId, 
                                  String accessIp, String userAgent, Integer accessSource);

    /**
     * 更新会话时长
     *
     * @param accessId 访问记录ID
     * @param sessionDuration 会话时长（秒）
     */
    void updateSessionDuration(Long accessId, Integer sessionDuration);

    /**
     * 获取问卷访问记录分页
     *
     * @param pageReqVO 分页查询条件
     * @return 访问记录分页
     */
    PageResult<QuestionnaireAccessDO> getQuestionnaireAccessPage(Object pageReqVO);

    /**
     * 获取问卷访问记录列表
     *
     * @param questionnaireId 问卷ID
     * @param userId 用户ID
     * @return 访问记录列表
     */
    List<QuestionnaireAccessDO> getQuestionnaireAccessList(Long questionnaireId, Long userId);

    /**
     * 检查问卷访问权限
     *
     * @param questionnaireId 问卷ID
     * @param userId 用户ID
     * @return 是否有访问权限
     */
    boolean checkQuestionnaireAccess(Long questionnaireId, Long userId);

    /**
     * 获取问卷访问统计
     *
     * @param questionnaireId 问卷ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 访问统计信息
     */
    QuestionnaireAccessStats getQuestionnaireAccessStats(Long questionnaireId, 
                                                        LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取问卷访问趋势
     *
     * @param questionnaireId 问卷ID
     * @param days 天数
     * @return 访问趋势数据
     */
    List<Map<String, Object>> getQuestionnaireAccessTrend(Long questionnaireId, Integer days);

    /**
     * 获取热门问卷排行
     *
     * @param limit 限制数量
     * @param days 统计天数
     * @return 热门问卷列表
     */
    List<Map<String, Object>> getPopularQuestionnaires(Integer limit, Integer days);

    /**
     * 检查问卷可用性
     *
     * @param questionnaireId 问卷ID
     * @return 可用性检查结果
     */
    QuestionnaireAvailabilityResult checkQuestionnaireAvailability(Long questionnaireId);

    /**
     * 问卷访问统计信息
     */
    class QuestionnaireAccessStats {
        private Long totalAccess;
        private Long uniqueUsers;
        private Double averageSessionDuration;
        private Long completionCount;
        private Double completionRate;
        private Long todayAccess;
        private Long weekAccess;
        private Long monthAccess;

        // Getters and Setters
        public Long getTotalAccess() {
            return totalAccess;
        }

        public void setTotalAccess(Long totalAccess) {
            this.totalAccess = totalAccess;
        }

        public Long getUniqueUsers() {
            return uniqueUsers;
        }

        public void setUniqueUsers(Long uniqueUsers) {
            this.uniqueUsers = uniqueUsers;
        }

        public Double getAverageSessionDuration() {
            return averageSessionDuration;
        }

        public void setAverageSessionDuration(Double averageSessionDuration) {
            this.averageSessionDuration = averageSessionDuration;
        }

        public Long getCompletionCount() {
            return completionCount;
        }

        public void setCompletionCount(Long completionCount) {
            this.completionCount = completionCount;
        }

        public Double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(Double completionRate) {
            this.completionRate = completionRate;
        }

        public Long getTodayAccess() {
            return todayAccess;
        }

        public void setTodayAccess(Long todayAccess) {
            this.todayAccess = todayAccess;
        }

        public Long getWeekAccess() {
            return weekAccess;
        }

        public void setWeekAccess(Long weekAccess) {
            this.weekAccess = weekAccess;
        }

        public Long getMonthAccess() {
            return monthAccess;
        }

        public void setMonthAccess(Long monthAccess) {
            this.monthAccess = monthAccess;
        }
    }

    /**
     * 问卷可用性检查结果
     */
    class QuestionnaireAvailabilityResult {
        private boolean available;
        private String reason;
        private Integer statusCode;
        private Long responseTime;

        public QuestionnaireAvailabilityResult() {
        }

        public QuestionnaireAvailabilityResult(boolean available, String reason) {
            this.available = available;
            this.reason = reason;
        }

        // Getters and Setters
        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public Long getResponseTime() {
            return responseTime;
        }

        public void setResponseTime(Long responseTime) {
            this.responseTime = responseTime;
        }
    }

}