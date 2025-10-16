package cn.iocoder.yudao.module.psychology.dal.mysql.consultation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisEventProcessDO;
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