package com.lvye.mindtrip.module.psychology.dal.mysql.interventionplan;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.interventionplan.InterventionEventStepDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 干预事件步骤 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InterventionEventStepMapper extends BaseMapperX<InterventionEventStepDO> {

    /**
     * 根据干预事件ID查询步骤列表
     *
     * @param interventionId 干预事件ID
     * @return 步骤列表
     */
    default List<InterventionEventStepDO> selectListByInterventionId(Long interventionId) {
        return selectList(InterventionEventStepDO::getInterventionId, interventionId);
    }

    /**
     * 根据干预事件ID删除所有步骤
     *
     * @param interventionId 干预事件ID
     */
    default void deleteBatchByInterventionId(Long interventionId) {
        delete(InterventionEventStepDO::getInterventionId, interventionId);
    }

}
