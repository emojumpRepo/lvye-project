package com.lvye.mindtrip.module.psychology.dal.mysql.intervention;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.intervention.InterventionLevelHistoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 干预等级变更历史 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InterventionLevelHistoryMapper extends BaseMapperX<InterventionLevelHistoryDO> {

    default List<InterventionLevelHistoryDO> selectListByStudentId(Long studentProfileId) {
        return selectList(new LambdaQueryWrapperX<InterventionLevelHistoryDO>()
                .eq(InterventionLevelHistoryDO::getStudentProfileId, studentProfileId)
                .orderByDesc(InterventionLevelHistoryDO::getCreateTime));
    }

    default InterventionLevelHistoryDO selectLatestByStudentId(Long studentProfileId) {
        return selectOne(new LambdaQueryWrapperX<InterventionLevelHistoryDO>()
                .eq(InterventionLevelHistoryDO::getStudentProfileId, studentProfileId)
                .orderByDesc(InterventionLevelHistoryDO::getCreateTime)
                .last("LIMIT 1"));
    }
}