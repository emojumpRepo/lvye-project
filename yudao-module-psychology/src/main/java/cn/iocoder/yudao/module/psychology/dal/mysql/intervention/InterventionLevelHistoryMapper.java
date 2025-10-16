package cn.iocoder.yudao.module.psychology.dal.mysql.intervention;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.intervention.InterventionLevelHistoryDO;
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