package com.lvye.mindtrip.module.psychology.dal.mysql.consultation;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.consultation.ConsultationAssessmentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咨询评估 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ConsultationAssessmentMapper extends BaseMapperX<ConsultationAssessmentDO> {

    default ConsultationAssessmentDO selectByAppointmentId(Long appointmentId) {
        return selectOne(ConsultationAssessmentDO::getAppointmentId, appointmentId);
    }
}