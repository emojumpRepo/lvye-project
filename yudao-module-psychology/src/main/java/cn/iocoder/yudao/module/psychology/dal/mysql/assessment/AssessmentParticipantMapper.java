package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentParticipantDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssessmentParticipantMapper extends BaseMapperX<AssessmentParticipantDO> {

    default AssessmentParticipantDO selectByTaskIdAndStudentId(Long taskId, Long studentProfileId) {
        return selectOne(new LambdaQueryWrapperX<AssessmentParticipantDO>()
                .eq(AssessmentParticipantDO::getTaskId, taskId)
                .eq(AssessmentParticipantDO::getStudentProfileId, studentProfileId));
    }

    default void deleteByTaskId(Long taskId) {
        delete(AssessmentParticipantDO::getTaskId, taskId);
    }

    default void deleteByTaskIdAndStudentId(Long taskId, Long studentProfileId) {
        delete(new LambdaQueryWrapperX<AssessmentParticipantDO>()
                .eq(AssessmentParticipantDO::getTaskId, taskId)
                .eq(AssessmentParticipantDO::getStudentProfileId, studentProfileId));
    }

    default Long countByTaskId(Long taskId) {
        return selectCount(AssessmentParticipantDO::getTaskId, taskId);
    }

    default Long countByTaskIdAndStatus(Long taskId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<AssessmentParticipantDO>()
                .eq(AssessmentParticipantDO::getTaskId, taskId)
                .eq(AssessmentParticipantDO::getCompletionStatus, status));
    }

}



