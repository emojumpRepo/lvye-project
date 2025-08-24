package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果参数表
 * @Version: 1.0
 */
@Mapper
public interface QuestionnaireResultConfigMapper extends BaseMapperX<QuestionnaireResultConfigDO> {

    default List<QuestionnaireResultConfigDO> selectListByQuestionnaireId(Long questionnaireId){
        return selectList(new LambdaQueryWrapperX<QuestionnaireResultConfigDO>()
                .eq(QuestionnaireResultConfigDO::getQuestionnaireId, questionnaireId));
    }

}
