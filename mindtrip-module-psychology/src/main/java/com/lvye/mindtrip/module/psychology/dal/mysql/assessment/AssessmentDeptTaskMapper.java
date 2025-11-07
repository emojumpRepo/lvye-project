package com.lvye.mindtrip.module.psychology.dal.mysql.assessment;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentDeptTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-11
 * @Description:部门测评关联任务数据库组件
 * @Version: 1.0
 */
@Mapper
public interface AssessmentDeptTaskMapper extends BaseMapperX<AssessmentDeptTaskDO> {

    default void deleteByTaskNo(String taskNo) {
        delete(new LambdaQueryWrapperX<AssessmentDeptTaskDO>()
                .eq(AssessmentDeptTaskDO::getTaskNo, taskNo));
    }

    List<String> selectTaskListByDeptIds(@Param("deptIds") Set<Long> deptIds);



}
