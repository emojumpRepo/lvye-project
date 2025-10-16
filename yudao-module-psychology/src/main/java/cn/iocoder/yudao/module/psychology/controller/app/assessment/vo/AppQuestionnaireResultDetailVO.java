package cn.iocoder.yudao.module.psychology.controller.app.assessment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AppQuestionnaireResultDetailVO {

    private Long id;
    private Long questionnaireId;
    private Long userId;
    private String assessmentTaskNo;
    private String answers;
    private BigDecimal score;
    private Integer riskLevel;
    private String evaluate;
    private String suggestions;
    private String dimensionScores;
    // 原始 JSON 字符串，便于兼容
    private String resultData;
    // 解析后的 JSON 对象
    private Object resultDataParsed;
    private Date completedTime;
    private Integer generationStatus;
}
