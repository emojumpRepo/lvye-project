package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultDO;
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
