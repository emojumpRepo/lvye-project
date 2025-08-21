package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskQuestionnaireDO;
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


