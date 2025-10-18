package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDimensionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionnaireDimensionMapper extends BaseMapperX<QuestionnaireDimensionDO> {

    @Select("SELECT * FROM lvye_questionnaire_dimension WHERE questionnaire_id = #{questionnaireId} AND deleted = 0 ORDER BY sort_order ASC")
    List<QuestionnaireDimensionDO> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
}
