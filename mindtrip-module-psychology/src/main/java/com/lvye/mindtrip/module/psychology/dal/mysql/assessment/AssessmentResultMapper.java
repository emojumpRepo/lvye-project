package com.lvye.mindtrip.module.psychology.dal.mysql.assessment;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentResultDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssessmentResultMapper extends BaseMapperX<AssessmentResultDO> {

    default AssessmentResultDO selectByParticipantAndDim(Long participantId, String dimensionCode) {
        return selectOne(new LambdaQueryWrapperX<AssessmentResultDO>()
                .eq(AssessmentResultDO::getParticipantId, participantId)
                .eq(AssessmentResultDO::getDimensionCode, dimensionCode));
    }

    default AssessmentResultDO selectByTaskNoAndParticipantId(String taskNo, Long participantId) {
        return selectOne(new LambdaQueryWrapperX<AssessmentResultDO>()
                .eq(AssessmentResultDO::getTaskNo, taskNo)
                .eq(AssessmentResultDO::getParticipantId, participantId)
                .orderByDesc(AssessmentResultDO::getCreateTime)
                .last("LIMIT 1"));
    }
}
