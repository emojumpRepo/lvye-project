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
     * 场景ID（若绑定场景）
     */
    private Long scenarioId;

    /**
     * 用户ID
     */
    private Long userId;

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

    /**
     * 场景编码（如 CAMPUS_TRIP）
     */
    private String scenarioCode;

    /**
     * 问卷ID到问卷编码的映射
     * key: 问卷ID
     * value: 问卷编码（如 mental_health_survey）
     */
    private Map<Long, String> questionnaireCodeMap;

    /**
     * 当前测评任务编号（用于限定本次任务的问卷结果）
     */
    private String taskNo;

    // 显式提供getter，避免部分环境下Lombok处理不及时
    public Long getScenarioId() {
        return scenarioId;
    }

    public Long getStudentProfileId() {
        return studentProfileId;
    }

    public String getTaskNo() {
        return taskNo;
    }
 
}