package com.lvye.mindtrip.module.psychology.framework.resultgenerator.vo;

import lombok.Data;

/**
 * 答题数据VO
 *
 * @author 芋道源码
 */
@Data
public class AnswerVO {

    /**
     * 题目ID
     */
    private String questionId;

    /**
     * 题目类型
     */
    private String questionType;

    /**
     * 答案内容
     */
    private String answerContent;

    /**
     * 答案选项
     */
    private String answerOption;

    /**
     * 答案分值
     */
    private Integer answerScore;

    /**
     * 维度标识
     */
    private String dimensionCode;

}