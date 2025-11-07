package com.lvye.mindtrip.module.psychology.convert.questionnaire;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigSaveReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 问卷结果配置 Convert
 *
 * @author MinGoo
 */
@Mapper
public interface QuestionnaireResultConfigConvert {

    QuestionnaireResultConfigConvert INSTANCE = Mappers.getMapper(QuestionnaireResultConfigConvert.class);

    QuestionnaireResultConfigDO convert(QuestionnaireResultConfigSaveReqVO bean);

    QuestionnaireResultConfigRespVO convert(QuestionnaireResultConfigDO bean);

    PageResult<QuestionnaireResultConfigRespVO> convertPage(PageResult<QuestionnaireResultConfigDO> page);

    List<QuestionnaireResultConfigRespVO> convertList(List<QuestionnaireResultConfigDO> list);

}
