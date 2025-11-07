package com.lvye.mindtrip.module.psychology.dal.mysql.consultation;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.consultation.CrisisEventAssessmentDO;
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

    default List<CrisisEventAssessmentDO> selectListByEventIdOrder(Long eventId) {
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

    /**
     * 根据学生档案ID查询所有评估记录（按创建时间倒序）
     *
     * @param studentProfileId 学生档案ID
     * @return 评估记录列表
     */
    default List<CrisisEventAssessmentDO> selectListByStudentProfileId(Long studentProfileId) {
        return selectList(new LambdaQueryWrapperX<CrisisEventAssessmentDO>()
                .eq(CrisisEventAssessmentDO::getStudentProfileId, studentProfileId)
                .orderByDesc(CrisisEventAssessmentDO::getCreateTime));
    }
}