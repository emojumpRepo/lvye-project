package cn.iocoder.yudao.module.psychology.convert.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo.AppQuestionnaireAccessRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireAccessDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 问卷访问记录 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface QuestionnaireAccessConvert {

    QuestionnaireAccessConvert INSTANCE = Mappers.getMapper(QuestionnaireAccessConvert.class);

    /**
     * 转换为学生端访问响应VO
     */
    @Mapping(target = "accessId", source = "id")
    @Mapping(target = "questionnaireTitle", ignore = true)
    @Mapping(target = "externalLink", ignore = true)
    @Mapping(target = "accessible", ignore = true)
    @Mapping(target = "statusMessage", ignore = true)
    @Mapping(target = "estimatedDuration", ignore = true)
    @Mapping(target = "questionCount", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "progress", ignore = true)
    AppQuestionnaireAccessRespVO convertToAppRespVO(QuestionnaireAccessDO bean);

    List<AppQuestionnaireAccessRespVO> convertToAppRespVOList(List<QuestionnaireAccessDO> list);

    /**
     * 转换分页结果
     */
    PageResult<AppQuestionnaireAccessRespVO> convertToAppRespVOPage(PageResult<QuestionnaireAccessDO> page);

}