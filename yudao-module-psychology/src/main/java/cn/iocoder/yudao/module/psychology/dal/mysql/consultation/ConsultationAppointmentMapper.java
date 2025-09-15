package cn.iocoder.yudao.module.psychology.dal.mysql.consultation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.ConsultationAppointmentPageReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAppointmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询预约 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ConsultationAppointmentMapper extends BaseMapperX<ConsultationAppointmentDO> {

    default PageResult<ConsultationAppointmentDO> selectPage(ConsultationAppointmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .eqIfPresent(ConsultationAppointmentDO::getStudentProfileId, reqVO.getStudentProfileId())
                .eqIfPresent(ConsultationAppointmentDO::getCounselorUserId, reqVO.getCounselorUserId())
                .eqIfPresent(ConsultationAppointmentDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ConsultationAppointmentDO::getAppointmentTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(ConsultationAppointmentDO::getId));
    }

    default List<ConsultationAppointmentDO> selectTodayList(Long counselorUserId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        return selectList(new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .eq(ConsultationAppointmentDO::getCounselorUserId, counselorUserId)
                .between(ConsultationAppointmentDO::getAppointmentTime, startOfDay, endOfDay)
                .orderByAsc(ConsultationAppointmentDO::getAppointmentTime));
    }

    default boolean hasTimeConflict(Long counselorUserId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        LambdaQueryWrapperX<ConsultationAppointmentDO> wrapper = new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .eq(ConsultationAppointmentDO::getCounselorUserId, counselorUserId)
                .ne(ConsultationAppointmentDO::getStatus, 4) // 排除已取消的
                .and(w -> w.between(ConsultationAppointmentDO::getAppointmentTime, startTime, endTime)
                        .or()
                        .le(ConsultationAppointmentDO::getAppointmentTime, startTime)
                        .apply("DATE_ADD(appointment_time, INTERVAL duration_minutes MINUTE) > {0}", startTime));
        
        if (excludeId != null) {
            wrapper.ne(ConsultationAppointmentDO::getId, excludeId);
        }
        
        return selectCount(wrapper) > 0;
    }
}