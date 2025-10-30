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
import java.util.ArrayList;
import java.util.List;

@Mapper
public interface CrisisInterventionMapper extends BaseMapperX<CrisisInterventionDO> {

    /**
     * 带学生信息关联查询的分页方法
     * 支持按学生学号、学生姓名、班级ID查询（需要关联学生档案表）
     * 注意：counselorUserId 直接查询 handler_user_id，不在此方法中
     */
    List<CrisisInterventionDO> selectPageWithStudentInfo(@Param("reqVO") CrisisEventPageReqVO reqVO,
                                                          @Param("offset") Integer offset,
                                                          @Param("limit") Integer limit);

    /**
     * 统计带学生信息关联查询的总数
     */
    Long countPageWithStudentInfo(@Param("reqVO") CrisisEventPageReqVO reqVO);

    default PageResult<CrisisInterventionDO> selectPage(CrisisEventPageReqVO reqVO) {
        // 只有当查询条件中包含学生学号、学生姓名或班级ID时，才使用XML关联查询
        // counselorUserId直接匹配handler_user_id，不需要关联查询
        if (reqVO.getStudentNo() != null || reqVO.getStudentName() != null || reqVO.getClassId() != null) {
            // 计算分页参数
            Integer offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
            Integer limit = reqVO.getPageSize();

            // 查询数据和总数
            List<CrisisInterventionDO> list = selectPageWithStudentInfo(reqVO, offset, limit);
            Long total = countPageWithStudentInfo(reqVO);

            return new PageResult<>(list, total);
        }

        // 否则使用原有的简单查询
        return selectPage(reqVO, new LambdaQueryWrapperX<CrisisInterventionDO>()
                .eqIfPresent(CrisisInterventionDO::getStudentProfileId, reqVO.getStudentProfileId())
                .eqIfPresent(CrisisInterventionDO::getPriority, reqVO.getPriority())
                .eqIfPresent(CrisisInterventionDO::getProcessStatus, reqVO.getProcessStatus())
                .eqIfPresent(CrisisInterventionDO::getHandlerUserId, reqVO.getCounselorUserId())
                .eqIfPresent(CrisisInterventionDO::getSourceType, reqVO.getSourceType())
                .eqIfPresent(CrisisInterventionDO::getStatus, reqVO.getStatus())
                .orderByDesc(CrisisInterventionDO::getCreateTime));
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

    /**
     * 查询正在进行的危机事件（status != 5）分页列表
     *
     * @param pageParam 分页参数
     * @return 分页结果
     */
    default PageResult<CrisisInterventionDO> selectOngoingPage(cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CrisisInterventionDO>()
                .ne(CrisisInterventionDO::getStatus, 5)
                .orderByDesc(CrisisInterventionDO::getCreateTime));
    }
}



