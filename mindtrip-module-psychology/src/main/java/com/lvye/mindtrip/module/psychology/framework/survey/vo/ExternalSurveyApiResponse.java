package com.lvye.mindtrip.module.psychology.framework.survey.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 外部问卷系统API响应包装类
 *
 * @author 芋道源码
 */
@Data
public class ExternalSurveyApiResponse {

    /**
     * 响应码
     */
    @JsonProperty("code")
    private Integer code;

    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 响应数据
     */
    @JsonProperty("data")
    private List<ExternalSurveyRespVO> data;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

}
