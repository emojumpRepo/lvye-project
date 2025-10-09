package cn.iocoder.yudao.module.psychology.dal.mysql.consultation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisEventAssessmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 危机事件评估 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrisisEventAssessmentMapper extends BaseMapperX<CrisisEventAssessmentDO> {

    default List<CrisisEventAssessmentDO> selectListByEventId(Long eventId) {
        return selectList(CrisisEventAssessmentDO::getEventId, eventId);
    }

    default List<CrisisEventAssessmentDO> selectListByEventIdOrderByCreateTimeDesc(Long eventId) {
        return selectList(new LambdaQueryWrapperX<CrisisEventAssessmentDO>()
                .eq(CrisisEventAssessmentDO::getEventId, eventId)
                .orderByDesc(CrisisEventAssessmentDO::getCreateTime));
    }

    default CrisisEventAssessmentDO selectLatestByEventId(Long eventId) {
        return selectOne(new LambdaQueryWrapperX<CrisisEventAssessmentDO>()
                .eq(CrisisEventAssessmentDO::getEventId, eventId)
                .orderByDesc(CrisisEventAssessmentDO::getAssessmentType)
                .orderByDesc(CrisisEventAssessmentDO::getId)
                .last("LIMIT 1"));
    }
}