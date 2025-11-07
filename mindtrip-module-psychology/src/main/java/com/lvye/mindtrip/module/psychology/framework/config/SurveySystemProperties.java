package com.lvye.mindtrip.module.psychology.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 外部问卷系统配置属性
 *
 * @author 芋道源码
 */
@Data
@Component
@ConfigurationProperties(prefix = "mindtrip.survey-system")
public class SurveySystemProperties {

    /**
     * 是否启用外部问卷系统同步
     */
    private Boolean enabled = true;

    /**
     * 外部问卷系统基础URL
     */
    private String baseUrl = "http://8.130.43.71:8080/api/survey";

    /**
     * 获取问卷列表的API路径
     */
    private String surveyListPath = "/getSurveyList";

    /**
     * 更新问卷简单配置的API路径
     */
    private String updateConfigPath = "/updateSimpleConf";

    /**
     * 发布问卷的API路径
     */
    private String publishSurveyPath = "/publishSurvey";

    /**
     * 暂停问卷的API路径
     */
    private String pauseSurveyPath = "/pausingSurvey";

    /**
     * 问卷题目的API路径
     */
    private String getSurveyQuestionsPath = "/getFormattedQuestions";

    /**
     * 获取问卷系统管理员的永久token
     */
    private String surveyAdminToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2OGE4NTYzNjEzNWQyOGEzOTM1Nzc5ZGIiLCJ1c2VybmFtZSI6ImFkbWluIiwiaWF0IjoxNzU2OTgzMTkzfQ.udZwlCVAsPfNdbFADU3eUVjtsxonO3mrkKZMphT00FU";

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout = 10000;

    /**
     * 重试次数
     */
    private Integer retryCount = 3;

    /**
     * API密钥（如果需要认证）
     */
    private String apiKey;

    /**
     * 获取完整的问卷列表URL
     */
    public String getSurveyListUrl() {
        return baseUrl + surveyListPath;
    }

    /**
     * 获取完整的更新配置URL
     */
    public String getUpdateConfigUrl() {
        return baseUrl + updateConfigPath;
    }

    /**
     * 获取完整的发布问卷URL
     */
    public String getPublishSurveyUrl() {
        return baseUrl + publishSurveyPath;
    }

    /**
     * 获取完整的暂停问卷URL
     */
    public String getPauseSurveyUrl() {
        return baseUrl + pauseSurveyPath;
    }

    /**
     * 获取完整的获取问卷题目URL
     */
    public String getGetQuestionUrl() {
        return baseUrl + getSurveyQuestionsPath;
    }
}
