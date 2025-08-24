package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentTaskUserVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-11
 * @Description:用户测评关联任务数据库组件
 * @Version: 1.0
 */
@Mapper
public interface AssessmentUserTaskMapper extends BaseMapperX<AssessmentUserTaskDO> {

    default AssessmentUserTaskDO selectByTaskNoAndUserId(String taskNo, Long userId) {
        return selectOne(new LambdaQueryWrapperX<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .eq(AssessmentUserTaskDO::getUserId, userId));
    }

    default void deleteByTaskNoAndUserId(String taskNo, Long userId) {
        delete(new LambdaQueryWrapperX<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .eq(AssessmentUserTaskDO::getUserId, userId));
    }

    default void deleteByTaskNo(String taskNo) {
        delete(new LambdaQueryWrapperX<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo));
    }

    default int updateStatusById(Long id, Integer status) {
        return update(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getId, id)
                .set(AssessmentUserTaskDO::getStatus, status)
                .set(AssessmentUserTaskDO::getStartTime, new Date()));
    }

    default int updateFinishStatusById(String taskNo) {
        return update(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .set(AssessmentUserTaskDO::getStatus, ParticipantCompletionStatusEnum.COMPLETED.getStatus())
                .set(AssessmentUserTaskDO::getSubmitTime, new Date()));
    }

    List<AssessmentTaskUserVO> selectListByTaskNo(@Param("taskNo") String taskNo);

    Long selectCountByTaskNo(@Param("taskNo") String taskNo);

    Long selectCountByTaskNoAndStatus(@Param("taskNo") String taskNo, @Param("status") Integer status);

}
