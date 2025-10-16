package cn.iocoder.yudao.module.psychology.dal.mysql.consultation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAssessmentDO;
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