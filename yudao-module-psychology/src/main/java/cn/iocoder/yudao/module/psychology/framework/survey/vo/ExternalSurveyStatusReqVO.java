package cn.iocoder.yudao.module.psychology.framework.survey.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 外部问卷系统状态操作请求VO
 *
 * @author 芋道源码
 */
@Data
public class ExternalSurveyStatusReqVO {

    /**
     * 问卷ID（外部系统的ID）
     */
    @JsonProperty("surveyId")
    private String surveyId;

    /**
     * 构造发布问卷请求
     */
    public static ExternalSurveyStatusReqVO publishRequest(String surveyId) {
        ExternalSurveyStatusReqVO request = new ExternalSurveyStatusReqVO();
        request.setSurveyId(surveyId);
        return request;
    }

    /**
     * 构造暂停问卷请求
     */
    public static ExternalSurveyStatusReqVO pauseRequest(String surveyId) {
        ExternalSurveyStatusReqVO request = new ExternalSurveyStatusReqVO();
        request.setSurveyId(surveyId);
        return request;
    }

}
