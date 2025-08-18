package cn.iocoder.yudao.module.psychology.convert.questionnaireresult;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo.*;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult.vo.AppQuestionnaireResultRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 问卷结果 Convert
 *
 * 命名口径（管理端VO）：
 * - 原始分: totalScore (rawScore)
 * - 标准分: standardScore
 * - 百分位: percentileRank
 * - 报告内容: reportContent
 * - 生成错误: generationError
 *
 * 注意：部分旧VO/测试仍使用 resultContent/errorMessage 等命名，逐步迁移中。
 */
@Mapper
public interface QuestionnaireResultConvert {

    QuestionnaireResultConvert INSTANCE = Mappers.getMapper(QuestionnaireResultConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "assessmentTaskId", ignore = true)
    @Mapping(target = "assessmentResultId", ignore = true)
    @Mapping(target = "participantType", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "rawScore", ignore = true)
    @Mapping(target = "standardScore", ignore = true)
    @Mapping(target = "percentileRank", ignore = true)
    @Mapping(target = "levelDescription", source = "resultInterpretation")
    @Mapping(target = "dimensionScores", ignore = true)
    @Mapping(target = "resultData", ignore = true)
    @Mapping(target = "reportContent", ignore = true)
    @Mapping(target = "completedTime", ignore = true)
    @Mapping(target = "generationStatus", ignore = true)
    @Mapping(target = "generationTime", ignore = true)
    @Mapping(target = "generationError", ignore = true)
    @Mapping(target = "transMap", ignore = true)
    QuestionnaireResultDO convert(QuestionnaireResultSaveReqVO bean);

    @Mapping(target = "answerData", source = "answers")
    @Mapping(target = "totalScore", expression = "java(bean.getRawScore() != null ? bean.getRawScore().doubleValue() : null)")
    @Mapping(target = "standardScore", expression = "java(bean.getStandardScore() != null ? bean.getStandardScore().doubleValue() : null)")
    @Mapping(target = "percentileRank", expression = "java(bean.getPercentileRank() != null ? bean.getPercentileRank().doubleValue() : null)")
    @Mapping(target = "reportContent", source = "reportContent")
    @Mapping(target = "generationError", source = "generationError")
    @Mapping(target = "submitTime", source = "createTime")
    @Mapping(target = "questionnaireTitle", ignore = true)
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "riskLevelDesc", ignore = true)
    @Mapping(target = "generationStatusDesc", ignore = true)
    QuestionnaireResultRespVO convert(QuestionnaireResultDO bean);

    List<QuestionnaireResultRespVO> convertList(List<QuestionnaireResultDO> list);

    PageResult<QuestionnaireResultRespVO> convertPage(PageResult<QuestionnaireResultDO> page);

    // 可选：若后续在管理端 VO 中新增了 standardScore/percentileRank/reportContent/generationError 的字段映射，
    // 可在此处补充 @Mapping 注解以减少控制器层的手动映射。

    /**
     * 转换为学生端响应VO
     */
    @Mapping(target = "questionnaireTitle", ignore = true)
    @Mapping(target = "questionnaireDescription", ignore = true)
    @Mapping(target = "totalScore", ignore = true)
    @Mapping(target = "maxScore", ignore = true)
    @Mapping(target = "scoreRate", ignore = true)
    @Mapping(target = "riskLevelDesc", ignore = true)
    @Mapping(target = "dimensionScores", ignore = true)
    @Mapping(target = "resultInterpretation", source = "levelDescription")
    @Mapping(target = "detailedReport", source = "reportContent")
    @Mapping(target = "resultStatus", source = "generationStatus")
    @Mapping(target = "generateTime", source = "generationTime")
    @Mapping(target = "completeTime", source = "completedTime")
    @Mapping(target = "answerDuration", ignore = true)
    @Mapping(target = "canRetake", ignore = true)
    @Mapping(target = "nextRetakeTime", ignore = true)
    AppQuestionnaireResultRespVO convertToAppRespVO(QuestionnaireResultDO bean);

    /**
     * 转换为简单响应VO
     */
    @Mapping(target = "questionnaireTitle", ignore = true)
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "totalScore", expression = "java(bean.getRawScore() != null ? bean.getRawScore().doubleValue() : null)")
    @Mapping(target = "standardScore", expression = "java(bean.getStandardScore() != null ? bean.getStandardScore().doubleValue() : null)")
    @Mapping(target = "percentileRank", expression = "java(bean.getPercentileRank() != null ? bean.getPercentileRank().doubleValue() : null)")
    @Mapping(target = "scoreRate", ignore = true)
    @Mapping(target = "riskLevelDesc", ignore = true)
    @Mapping(target = "status", source = "generationStatus")
    @Mapping(target = "submitTime", source = "completedTime")
    QuestionnaireResultSimpleRespVO convertToSimpleRespVO(QuestionnaireResultDO bean);

    List<QuestionnaireResultSimpleRespVO> convertToSimpleRespVOList(List<QuestionnaireResultDO> list);

    /**
     * 转换为导出VO
     */
    @Mapping(target = "questionnaireTitle", ignore = true)
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "totalScore", expression = "java(bean.getRawScore() != null ? bean.getRawScore().doubleValue() : null)")
    @Mapping(target = "standardScore", expression = "java(bean.getStandardScore() != null ? bean.getStandardScore().doubleValue() : null)")
    @Mapping(target = "percentileRank", expression = "java(bean.getPercentileRank() != null ? bean.getPercentileRank().doubleValue() : null)")
    @Mapping(target = "scoreRate", ignore = true)
    @Mapping(target = "riskLevelDesc", ignore = true)
    @Mapping(target = "status", source = "generationStatus")
    @Mapping(target = "statusDesc", ignore = true)
    @Mapping(target = "submitTime", source = "completedTime")
    QuestionnaireResultExportRespVO convertToExportRespVO(QuestionnaireResultDO bean);

    List<QuestionnaireResultExportRespVO> convertToExportRespVOList(List<QuestionnaireResultDO> list);

}