package cn.iocoder.yudao.module.psychology.framework.survey.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 外部问卷题目响应 VO
 */
@Data
public class ExternalSurveyQuestionRespVO {

    /**
     * 响应码
     */
    @JsonProperty("code")
    private Integer code;

    /**
     * 响应消息（可能为空）
     */
    @JsonProperty("message")
    private String message;

    /**
     * 题目数据
     */
    @JsonProperty("data")
    private List<QuestionItem> data;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    @Data
    public static class QuestionItem {
        /** 题目标题 */
        @JsonProperty("title")
        private String title;

        /** 题目类型，例如 radio、checkbox 等 */
        @JsonProperty("type")
        private String type;

        /** 选项，可能是字符串或对象（带输入框定义），因此用 Object 承接 */
        @JsonProperty("options")
        private List<Object> options;
    }
}


