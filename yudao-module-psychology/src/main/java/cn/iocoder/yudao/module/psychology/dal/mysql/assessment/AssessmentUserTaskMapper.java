package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import org.apache.ibatis.annotations.Mapper;

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

}
