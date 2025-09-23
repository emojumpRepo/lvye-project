package cn.iocoder.yudao.module.psychology.convert.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo.AppQuestionnaireSimpleRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 问卷 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface QuestionnaireConvert {

    QuestionnaireConvert INSTANCE = new QuestionnaireConvertImpl();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "accessCount", ignore = true)
    @Mapping(target = "completionCount", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "lastSyncTime", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "transMap", ignore = true)
    QuestionnaireDO convert(QuestionnaireCreateReqVO bean);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "accessCount", ignore = true)
    @Mapping(target = "completionCount", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "lastSyncTime", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "transMap", ignore = true)
    QuestionnaireDO convert(QuestionnaireUpdateReqVO bean);

    QuestionnaireRespVO convert(QuestionnaireDO bean);

    List<QuestionnaireRespVO> convertList(List<QuestionnaireDO> list);

    PageResult<QuestionnaireRespVO> convertPage(PageResult<QuestionnaireDO> page);

    // === 包含 surveyCode 的分页项转换 ===
    QuestionnaireWithSurveyRespVO convertWithSurvey(QuestionnaireDO bean);

    List<QuestionnaireWithSurveyRespVO> convertWithSurveyList(List<QuestionnaireDO> list);

    PageResult<QuestionnaireWithSurveyRespVO> convertWithSurveyPage(PageResult<QuestionnaireDO> page);

    @Mapping(target = "assessmentDimensionLabels", ignore = true)
    QuestionnaireSimpleRespVO convertSimple(QuestionnaireDO bean);

    List<QuestionnaireSimpleRespVO> convertSimpleList(List<QuestionnaireDO> list);

    /**
     * 转换为学生端简单响应VO
     */
    @Mapping(target = "type", source = "questionnaireType")
    @Mapping(target = "typeDesc", ignore = true)
    @Mapping(target = "statusDesc", ignore = true)
    @Mapping(target = "completionRate", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "accessible", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "isOpen", expression = "java(bean.getSupportIndependentUse() != null && bean.getSupportIndependentUse() == 1)")
    AppQuestionnaireSimpleRespVO convertToAppSimpleRespVO(QuestionnaireDO bean);

    List<AppQuestionnaireSimpleRespVO> convertToAppSimpleRespVOList(List<QuestionnaireDO> list);

}