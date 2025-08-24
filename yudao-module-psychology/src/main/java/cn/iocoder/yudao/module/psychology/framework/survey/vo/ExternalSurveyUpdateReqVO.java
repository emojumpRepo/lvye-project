package cn.iocoder.yudao.module.psychology.framework.survey.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 外部问卷系统配置更新请求VO
 *
 * @author 芋道源码
 */
@Data
public class ExternalSurveyUpdateReqVO {

    /**
     * 问卷ID（外部系统的ID）
     */
    @JsonProperty("surveyId")
    private String surveyId;

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

}
