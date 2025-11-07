package com.lvye.mindtrip.module.psychology.convert.questionnaire;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.QuestionnaireDimensionRespVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireDimensionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 问卷维度转换
 */
@Mapper
public interface QuestionnaireDimensionConvert {

    QuestionnaireDimensionConvert INSTANCE = Mappers.getMapper(QuestionnaireDimensionConvert.class);

    @Mapping(target = "participateModuleCalc", expression = "java(integerToBoolean(bean.getParticipateModuleCalc()))")
    @Mapping(target = "participateAssessmentCalc", expression = "java(integerToBoolean(bean.getParticipateAssessmentCalc()))")
    @Mapping(target = "participateRanking", expression = "java(integerToBoolean(bean.getParticipateRanking()))")
    @Mapping(target = "showScore", expression = "java(integerToBoolean(bean.getShowScore()))")
    QuestionnaireDimensionRespVO convert(QuestionnaireDimensionDO bean);

    List<QuestionnaireDimensionRespVO> convertList(List<QuestionnaireDimensionDO> list);

    PageResult<QuestionnaireDimensionRespVO> convertPage(PageResult<QuestionnaireDimensionDO> page);

    /**
     * Integer转Boolean的映射方法
     */
    default Boolean integerToBoolean(Integer value) {
        if (value == null) {
            return false;
        }
        return value == 1;
    }

    /**
     * Boolean转Integer的映射方法
     */
    default Integer booleanToInteger(Boolean value) {
        if (value == null || !value) {
            return 0;
        }
        return 1;
    }
}
