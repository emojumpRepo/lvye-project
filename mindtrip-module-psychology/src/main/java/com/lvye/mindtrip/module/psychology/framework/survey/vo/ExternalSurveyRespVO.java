package com.lvye.mindtrip.module.psychology.framework.survey.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 外部问卷系统响应VO
 *
 * @author 芋道源码
 */
@Data
public class ExternalSurveyRespVO {

    /**
     * 问卷ID（外部系统的ID）
     */
    @JsonProperty("surveyMetaId")
    private String surveyMetaId;

    /**
     * 问卷标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 备注
     */
    @JsonProperty("remark")
    private String remark;

    /**
     * 完成次数
     */
    @JsonProperty("submitCount")
    private Integer submitCount;

    /**
     * 问卷类型
     */
    @JsonProperty("surveyType")
    private String surveyType;

    /**
     * 问卷路径
     */
    @JsonProperty("surveyPath")
    private String surveyPath;

    /**
     * 问卷编码
     */
    @JsonProperty("surveyCode")
    private String surveyCode;

    /**
     * 当前状态
     */
    @JsonProperty("curStatus")
    private SurveyStatus curStatus;

    /**
     * 暂停状态
     */
    @JsonProperty("subStatus")
    private SurveyStatus subStatus;

    /**
     * 状态历史列表
     */
    @JsonProperty("statusList")
    private List<SurveyStatus> statusList;

    /**
     * 开始时间
     */
    @JsonProperty("beginTime")
    private String beginTime;

    /**
     * 结束时间
     */
    @JsonProperty("endTime")
    private String endTime;

    /**
     * 答题开始时间
     */
    @JsonProperty("answerBegTime")
    private String answerBegTime;

    /**
     * 答题结束时间
     */
    @JsonProperty("answerEndTime")
    private String answerEndTime;

    /**
     * 问卷配置ID
     */
    @JsonProperty("surveyConfId")
    private String surveyConfId;

    /**
     * 创建时间
     */
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;

    /**
     * 问卷题目数量
     */
    @JsonProperty("questionCount")
    private Integer questionCount;

    /**
     * 问卷状态内部类
     */
    @Data
    public static class SurveyStatus {
        /**
         * 状态值
         */
        @JsonProperty("status")
        private String status;

        /**
         * 状态时间戳
         */
        @JsonProperty("date")
        private Long date;
    }

    // 便捷方法：获取外部ID
    public String getExternalId() {
        return this.surveyMetaId;
    }

    // 便捷方法：获取当前状态字符串
    public String getCurrentStatus() {
        return curStatus != null ? curStatus.getStatus() : null;
    }

    // 便捷方法：获取暂停状态字符串
    public String getSubStatusValue() {
        return subStatus != null ? subStatus.getStatus() : null;
    }

    // 便捷方法：判断是否处于暂停状态
    public boolean isPaused() {
        return subStatus != null && "pausing".equals(subStatus.getStatus());
    }

}
