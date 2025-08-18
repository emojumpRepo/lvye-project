package cn.iocoder.yudao.module.psychology.framework.resultgenerator;

import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.AnswerVO;
import cn.iocoder.yudao.module.psychology.framework.resultgenerator.vo.QuestionnaireResultVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 结果生成上下文
 *
 * @author 芋道源码
 */
@Data
@Builder
public class ResultGenerationContext {

    /**
     * 生成类型
     */
    private ResultGeneratorTypeEnum generationType;

    /**
     * 问卷ID（单问卷结果生成）
     */
    private Long questionnaireId;

    /**
     * 测评ID（组合测评结果生成）
     */
    private Long assessmentId;

    /**
     * 学生档案ID
     */
    private Long studentProfileId;

    /**
     * 答题数据（单问卷）
     */
    private List<AnswerVO> answers;

    /**
     * 问卷结果列表（组合测评）
     */
    private List<QuestionnaireResultVO> questionnaireResults;

    /**
     * 参与者类型
     */
    private Integer participantType;

    /**
     * 生成配置版本
     */
    private String configVersion;

    /**
     * 扩展参数
     */
    private Map<String, Object> extraParams;

}