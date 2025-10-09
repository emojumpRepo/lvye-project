package cn.iocoder.yudao.module.psychology.dal.mysql.consultation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.CrisisEventPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.CrisisEventStatusStatisticsVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisInterventionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CrisisInterventionMapper extends BaseMapperX<CrisisInterventionDO> {

    default PageResult<CrisisInterventionDO> selectPage(CrisisEventPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrisisInterventionDO>()
                .eqIfPresent(CrisisInterventionDO::getStudentProfileId, reqVO.getStudentProfileId())
                .eqIfPresent(CrisisInterventionDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CrisisInterventionDO::getRiskLevel, reqVO.getRiskLevel())
                .eqIfPresent(CrisisInterventionDO::getPriority, reqVO.getPriority())
                .eqIfPresent(CrisisInterventionDO::getHandlerUserId, reqVO.getHandlerUserId())
                .eqIfPresent(CrisisInterventionDO::getProcessStatus, reqVO.getProcessStatus())
                .betweenIfPresent(CrisisInterventionDO::getReportedAt, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(CrisisInterventionDO::getPriority)
                .orderByDesc(CrisisInterventionDO::getReportedAt));
    }

    default List<CrisisInterventionDO> selectListByStudentId(Long studentProfileId) {
        return selectList(CrisisInterventionDO::getStudentProfileId, studentProfileId);
    }

    default boolean hasDuplicateEvent(Long studentProfileId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<CrisisInterventionDO>()
                .eq(CrisisInterventionDO::getStudentProfileId, studentProfileId)
                .between(CrisisInterventionDO::getReportedAt, startTime, endTime)
                .ne(CrisisInterventionDO::getStatus, 4)) > 0; // 排除已结案的
    }

    @Select("SELECT COUNT(1) FROM lvye_crisis_intervention WHERE status = #{status} AND deleted = 0")
    Long countByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(1) FROM lvye_crisis_intervention WHERE risk_level = #{riskLevel} AND deleted = 0")
    Long countByRiskLevel(@Param("riskLevel") Integer riskLevel);

    @Select("SELECT status, COUNT(*) as count FROM lvye_crisis_intervention WHERE deleted = 0 GROUP BY status")
    List<CrisisEventStatusStatisticsVO> selectStatusStatistics();

    @Select("SELECT COUNT(1) FROM lvye_crisis_intervention WHERE process_status = #{processStatus} AND deleted = 0")
    Long countByProcessStatus(@Param("processStatus") Integer processStatus);
}



