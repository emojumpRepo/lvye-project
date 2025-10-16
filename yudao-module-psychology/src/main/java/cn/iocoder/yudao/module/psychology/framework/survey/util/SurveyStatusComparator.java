package cn.iocoder.yudao.module.psychology.framework.survey.util;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyRespVO;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 问卷状态比较工具类
 *
 * @author 芋道源码
 */
@Slf4j
public class SurveyStatusComparator {

    /**
     * 状态变化结果
     */
    public static class StatusChangeResult {
        private boolean changed;
        private String changeDescription;
        private Integer oldStatus;
        private Integer newStatus;
        private Integer oldIsOpen;
        private Integer newIsOpen;

        public StatusChangeResult(boolean changed) {
            this.changed = changed;
        }

        public StatusChangeResult(boolean changed, String changeDescription) {
            this.changed = changed;
            this.changeDescription = changeDescription;
        }

        // Getters and Setters
        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }

        public String getChangeDescription() {
            return changeDescription;
        }

        public void setChangeDescription(String changeDescription) {
            this.changeDescription = changeDescription;
        }

        public Integer getOldStatus() {
            return oldStatus;
        }

        public void setOldStatus(Integer oldStatus) {
            this.oldStatus = oldStatus;
        }

        public Integer getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(Integer newStatus) {
            this.newStatus = newStatus;
        }

        public Integer getOldIsOpen() {
            return oldIsOpen;
        }

        public void setOldIsOpen(Integer oldIsOpen) {
            this.oldIsOpen = oldIsOpen;
        }

        public Integer getNewIsOpen() {
            return newIsOpen;
        }

        public void setNewIsOpen(Integer newIsOpen) {
            this.newIsOpen = newIsOpen;
        }
    }

    /**
     * 比较问卷状态是否发生变化
     *
     * @param localQuestionnaire 本地问卷数据
     * @param externalSurvey 外部问卷数据
     * @return 状态变化结果
     */
    public static StatusChangeResult compareStatus(QuestionnaireDO localQuestionnaire, ExternalSurveyRespVO externalSurvey) {
        Integer newStatus = SurveyDataConverter.convertStatus(externalSurvey);
        
        Integer oldStatus = localQuestionnaire.getStatus();

        StatusChangeResult result = new StatusChangeResult(false);
        result.setOldStatus(oldStatus);
        result.setNewStatus(newStatus);

        // 检查状态是否发生变化
        boolean statusChanged = !Objects.equals(oldStatus, newStatus);

        if (statusChanged) {
            result.setChanged(true);
            
            StringBuilder description = new StringBuilder();
            
            if (statusChanged) {
                description.append(String.format("状态: %s -> %s", 
                    getStatusDescription(oldStatus), getStatusDescription(newStatus)));
            }

            // 添加暂停状态的特殊说明
            if (externalSurvey.isPaused()) {
                if (description.length() > 0) {
                    description.append(", ");
                }
                description.append("检测到暂停状态(subStatus: pausing)");
            }

            result.setChangeDescription(description.toString());
            
            log.info("[compareStatus] 问卷状态发生变化，surveyId: {}, 变化: {}", 
                    externalSurvey.getSurveyMetaId(), description.toString());
        }

        return result;
    }

    /**
     * 获取状态描述
     */
    private static String getStatusDescription(Integer status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status) {
            case 0:
                return "草稿";
            case 1:
                return "已发布";
            case 2:
                return "已下线";
            case 3:
                return "已失效";
            default:
                return "未知(" + status + ")";
        }
    }

}
