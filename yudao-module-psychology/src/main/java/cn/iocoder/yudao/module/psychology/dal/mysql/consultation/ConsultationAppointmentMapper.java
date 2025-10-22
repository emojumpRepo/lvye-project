package cn.iocoder.yudao.module.psychology.dal.mysql.consultation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.ConsultationAppointmentPageReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.ConsultationAppointmentDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 咨询预约 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ConsultationAppointmentMapper extends BaseMapperX<ConsultationAppointmentDO> {

    default PageResult<ConsultationAppointmentDO> selectPage(ConsultationAppointmentPageReqVO reqVO) {
        LambdaQueryWrapperX<ConsultationAppointmentDO> wrapper = new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .eqIfPresent(ConsultationAppointmentDO::getStudentProfileId, reqVO.getStudentProfileId())
                .eqIfPresent(ConsultationAppointmentDO::getCounselorUserId, reqVO.getCounselorUserId())
                .eqIfPresent(ConsultationAppointmentDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ConsultationAppointmentDO::getOverdue, reqVO.getOverdue());

        // 按学生姓名模糊查询（关联学生档案表）
        if (StringUtils.hasText(reqVO.getStudentName())) {
            wrapper.apply("EXISTS (SELECT 1 FROM lvye_student_profile sp " +
                         "WHERE sp.id = student_profile_id " +
                         "AND sp.name LIKE CONCAT('%', {0}, '%') " +
                         "AND sp.deleted = 0)", reqVO.getStudentName());
        }

        // 按咨询日期查询（将时间戳转换为日期，匹配 appointment_start_time 的日期部分）
        if (reqVO.getConsultTime() != null) {
            LocalDate consultDate = Instant.ofEpochMilli(reqVO.getConsultTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            wrapper.apply("DATE(appointment_start_time) = {0}", consultDate);
        }

        wrapper.orderByDesc(ConsultationAppointmentDO::getId);

        return selectPage(reqVO, wrapper);
    }

    default boolean hasTimeConflict(Long counselorUserId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        LambdaQueryWrapperX<ConsultationAppointmentDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(ConsultationAppointmentDO::getCounselorUserId, counselorUserId);
        wrapper.ne(ConsultationAppointmentDO::getStatus, 4); // 排除已取消的
        wrapper.apply("( (appointment_start_time BETWEEN {0} AND {1}) OR (appointment_start_time <= {0} AND appointment_end_time > {0}) OR (appointment_start_time < {1} AND appointment_end_time >= {1}) )", startTime, endTime);

        if (excludeId != null) {
            wrapper.ne(ConsultationAppointmentDO::getId, excludeId);
        }

        return selectCount(wrapper) > 0;
    }

    /**
     * 统计今天的咨询数(基于开始时间)
     */
    default long countTodayConsultations() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        return selectCount(new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .between(ConsultationAppointmentDO::getAppointmentStartTime, startOfDay, endOfDay));
    }

    /**
     * 统计已完成的咨询数(状态为3-已闭环)
     */
    default long countCompletedConsultations() {
        return selectCount(new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .eq(ConsultationAppointmentDO::getStatus, 3));
    }

    /**
     * 统计待完成的咨询数(状态为1或2且未逾期)
     */
    default long countPendingConsultations(LocalDateTime now) {
        return selectCount(new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .in(ConsultationAppointmentDO::getStatus, 1, 2)
                .ge(ConsultationAppointmentDO::getAppointmentEndTime, now));
    }

    /**
     * 统计逾期的咨询数(结束时间超过30分钟后且未完成)
     */
    default long countOverdueConsultations(LocalDateTime now) {
        return selectCount(new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .notIn(ConsultationAppointmentDO::getStatus, 3, 4)
                .lt(ConsultationAppointmentDO::getAppointmentEndTime, now.minusMinutes(30)));
    }
}