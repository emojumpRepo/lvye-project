package cn.iocoder.yudao.module.psychology.dal.mysql.consultation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
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

    default CrisisEventAssessmentDO selectLatestByEventId(Long eventId) {
        return selectOne(CrisisEventAssessmentDO::getEventId, eventId, 
            "assessment_type", "DESC", "id", "DESC");
    }
}