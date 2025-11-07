package com.lvye.mindtrip.module.psychology.service.questionnaire;

import com.lvye.mindtrip.module.psychology.framework.survey.vo.ExternalSurveyQuestionRespVO;
import com.lvye.mindtrip.module.psychology.framework.survey.vo.ExternalSurveyRespVO;

import java.util.List;

/**
 * 问卷同步服务接口
 *
 * @author 芋道源码
 */
public interface QuestionnaireSyncService {

    /**
     * 同步外部问卷系统的问卷数据
     *
     * @return 同步结果统计
     */
    QuestionnaireSyncResult syncQuestionnaires();

    /**
     * 获取外部问卷题目
     *
     * @param surveyId 外部问卷ID
     * @return 题目响应
     */
    ExternalSurveyQuestionRespVO getSurveyQuestions(String surveyId);

    /**
     * 同步结果统计类
     */
    class QuestionnaireSyncResult {
        /**
         * 总共处理的问卷数量
         */
        private int totalProcessed;

        /**
         * 新增的问卷数量
         */
        private int newAdded;

        /**
         * 更新的问卷数量
         */
        private int updated;

        /**
         * 标记为失效的问卷数量
         */
        private int invalidated;

        /**
         * 同步失败的问卷数量
         */
        private int failed;

        /**
         * 同步是否成功
         */
        private boolean success;

        /**
         * 错误信息
         */
        private String errorMessage;

        public QuestionnaireSyncResult() {
        }

        public QuestionnaireSyncResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        // Getters and Setters
        public int getTotalProcessed() {
            return totalProcessed;
        }

        public void setTotalProcessed(int totalProcessed) {
            this.totalProcessed = totalProcessed;
        }

        public int getNewAdded() {
            return newAdded;
        }

        public void setNewAdded(int newAdded) {
            this.newAdded = newAdded;
        }

        public int getUpdated() {
            return updated;
        }

        public void setUpdated(int updated) {
            this.updated = updated;
        }

        public int getInvalidated() {
            return invalidated;
        }

        public void setInvalidated(int invalidated) {
            this.invalidated = invalidated;
        }

        public int getFailed() {
            return failed;
        }

        public void setFailed(int failed) {
            this.failed = failed;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return String.format("QuestionnaireSyncResult{totalProcessed=%d, newAdded=%d, updated=%d, invalidated=%d, failed=%d, success=%s, errorMessage='%s'}",
                    totalProcessed, newAdded, updated, invalidated, failed, success, errorMessage);
        }
    }

}
