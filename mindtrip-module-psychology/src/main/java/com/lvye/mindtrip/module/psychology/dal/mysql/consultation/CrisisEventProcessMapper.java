package com.lvye.mindtrip.module.psychology.dal.mysql.consultation;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.consultation.CrisisEventProcessDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 危机事件处理过程 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrisisEventProcessMapper extends BaseMapperX<CrisisEventProcessDO> {

    default List<CrisisEventProcessDO> selectListByEventId(Long eventId) {
        return selectList(CrisisEventProcessDO::getEventId, eventId);
    }
}