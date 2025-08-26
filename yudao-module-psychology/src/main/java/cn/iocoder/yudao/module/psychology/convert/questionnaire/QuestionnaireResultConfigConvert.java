package cn.iocoder.yudao.module.psychology.convert.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigSaveReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import org.mapstruct.Mapper;
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
