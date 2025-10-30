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
     * 统计逾期的咨询数
     * 包含两种情况：
     * 1. 状态为1（已预约）且预约结束时间超过30分钟
     * 2. 状态为2（已完成）且预约结束时间超过配置的小时数
     *
     * @param now 当前时间
     * @param reportExpireHours 报告逾期时间（小时数），用于判断状态2的逾期
     * @return 逾期咨询数量
     */
    default long countOverdueConsultations(LocalDateTime now, int reportExpireHours) {
        return selectCount(new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .and(wrapper -> wrapper
                        // 情况1：状态为1（已预约）且预约结束时间 < 当前时间 - 30分钟
                        .or(w -> w.eq(ConsultationAppointmentDO::getStatus, 1)
                                .lt(ConsultationAppointmentDO::getAppointmentEndTime, now.minusMinutes(30)))
                        // 情况2：状态为2（已完成）且预约结束时间 < 当前时间 - 配置的小时数
                        .or(w -> w.eq(ConsultationAppointmentDO::getStatus, 2)
                                .lt(ConsultationAppointmentDO::getAppointmentEndTime, now.minusHours(reportExpireHours)))
                )
        );
    }

    /**
     * 根据日期范围查询预约数据（排除已取消的）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预约列表
     */
    default List<ConsultationAppointmentDO> selectByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return selectList(new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .between(ConsultationAppointmentDO::getAppointmentStartTime, startDate, endDate)
                .ne(ConsultationAppointmentDO::getStatus, 4) // 排除已取消的
                .orderByAsc(ConsultationAppointmentDO::getAppointmentStartTime));
    }

    /**
     * 根据日期和咨询师查询预约数据（排除已取消的）
     *
     * @param date 查询日期
     * @param counselorUserId 咨询师用户ID（可为null，表示查询所有咨询师）
     * @return 预约列表
     */
    default List<ConsultationAppointmentDO> selectByDateAndCounselor(LocalDate date, Long counselorUserId) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(23, 59, 59);

        LambdaQueryWrapperX<ConsultationAppointmentDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.between(ConsultationAppointmentDO::getAppointmentStartTime, startDateTime, endDateTime);
        wrapper.ne(ConsultationAppointmentDO::getStatus, 4); // 排除已取消的

        if (counselorUserId != null) {
            wrapper.eq(ConsultationAppointmentDO::getCounselorUserId, counselorUserId);
        }

        wrapper.orderByAsc(ConsultationAppointmentDO::getAppointmentStartTime);

        return selectList(wrapper);
    }

    /**
     * 根据学生档案ID查询咨询记录（排除已取消的）
     *
     * @param studentProfileId 学生档案ID
     * @return 咨询记录列表
     */
    default List<ConsultationAppointmentDO> selectListByStudentProfileId(Long studentProfileId) {
        return selectList(new LambdaQueryWrapperX<ConsultationAppointmentDO>()
                .eq(ConsultationAppointmentDO::getStudentProfileId, studentProfileId)
                .ne(ConsultationAppointmentDO::getStatus, 4) // 排除已取消的
                .orderByDesc(ConsultationAppointmentDO::getAppointmentStartTime));
    }

    /**
     * 获取今日咨询预约分页
     *
     * @param reqVO 分页查询请求
     * @return 今日咨询预约分页结果
     */
    default PageResult<ConsultationAppointmentDO> selectTodayPage(cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.ConsultationAppointmentTodayPageReqVO reqVO) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        LambdaQueryWrapperX<ConsultationAppointmentDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.between(ConsultationAppointmentDO::getAppointmentStartTime, startOfDay, endOfDay);
        wrapper.ne(ConsultationAppointmentDO::getStatus, 4); // 排除已取消的
        wrapper.eqIfPresent(ConsultationAppointmentDO::getCounselorUserId, reqVO.getCounselorUserId());
        wrapper.eqIfPresent(ConsultationAppointmentDO::getStatus, reqVO.getStatus());
        wrapper.orderByAsc(ConsultationAppointmentDO::getAppointmentStartTime);

        return selectPage(reqVO, wrapper);
    }
}