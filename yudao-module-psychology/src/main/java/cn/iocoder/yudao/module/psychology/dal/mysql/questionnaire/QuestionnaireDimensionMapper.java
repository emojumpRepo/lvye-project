package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDimensionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionnaireDimensionMapper extends BaseMapperX<QuestionnaireDimensionDO> {

    List<QuestionnaireDimensionDO> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
}


