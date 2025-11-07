package com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireDimensionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionnaireDimensionMapper extends BaseMapperX<QuestionnaireDimensionDO> {

    @Select("SELECT * FROM lvye_questionnaire_dimension WHERE questionnaire_id = #{questionnaireId} AND deleted = 0 ORDER BY sort_order ASC")
    List<QuestionnaireDimensionDO> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
}
