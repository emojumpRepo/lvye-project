package com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultEvaluateConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果评价参数表
 * @Version: 1.0
 */
@Mapper
public interface QuestionnaireResultEvaluateConfigMapper extends BaseMapperX<QuestionnaireResultEvaluateConfigDO> {

    /**
     * 根据问卷id和不正常因子数量查询评价配置
     *
     * @param questionnaireId
     * @param abnormalCount
     * @return
     */
    default QuestionnaireResultEvaluateConfigDO selectByQuestionnaireIdAndAbnormalCount(Long questionnaireId, int abnormalCount) {
        return selectOne(new LambdaQueryWrapperX<QuestionnaireResultEvaluateConfigDO>()
                .eq(QuestionnaireResultEvaluateConfigDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireResultEvaluateConfigDO::getAbnormalCount, abnormalCount));
    }

}
