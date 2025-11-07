package com.lvye.mindtrip.module.psychology.dal.mysql.assessment;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentTaskQuestionnaireDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentTaskQuestionnaireMapper extends BaseMapperX<AssessmentTaskQuestionnaireDO> {

    default void deleteByTaskNo(String taskNo) {
        delete(new LambdaQueryWrapperX<AssessmentTaskQuestionnaireDO>()
                .eq(AssessmentTaskQuestionnaireDO::getTaskNo, taskNo));
    }

    List<Long> selectQuestionnaireIdsByTaskNo(@Param("taskNo") String taskNo, @Param("tenantId") Long tenantId);

    void insertBatch(@Param("list") List<AssessmentTaskQuestionnaireDO> list);

    int insert(AssessmentTaskQuestionnaireDO entity);

    List<AssessmentTaskQuestionnaireDO> selectListByTaskNo(@Param("taskNo") String taskNo, @Param("tenantId") Long tenantId);
}


