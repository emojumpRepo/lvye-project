package cn.iocoder.yudao.module.psychology.dal.mysql.profile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.InterventionDashboardReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.InterventionStudentRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生档案干预相关 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface StudentInterventionMapper extends BaseMapperX<StudentProfileDO> {

    /**
     * 分页查询干预学生列表
     */
    @Select("<script>" +
            "SELECT " +
            "  sp.id as student_profile_id, " +
            "  sp.name as student_name, " +
            "  sp.student_no as student_number, " +
            "  sp.sex as gender, " +
            "  sp.risk_level as current_risk_level, " +
            "  sp.graduation_status as study_status, " +
            "  <choose>" +
            "    <when test='reqVO.interventionEventStatus != null'>" +
            "      ie.update_time as last_update_time, " +
            "    </when>" +
            "    <otherwise>" +
            "      sp.update_time as last_update_time, " +
            "    </otherwise>" +
            "  </choose>" +
            "  d1.name as class_name, " +
            "  su.nickname as counselor_name, " +
            "  COUNT(DISTINCT ci.id) as crisis_event_count, " +
            "  COUNT(DISTINCT ca.id) as consultation_count, " +
            "  ie.id as intervention_plan_id " +
            "FROM lvye_student_profile sp " +
            "LEFT JOIN system_dept d1 ON sp.class_dept_id = d1.id " +
            "LEFT JOIN ( " +
            "  SELECT student_profile_id, handler_user_id " +
            "  FROM lvye_crisis_intervention " +
            "  WHERE deleted = 0 AND status IN (2, 3, 4, 5) " +
            "  AND id IN ( " +
            "    SELECT MAX(id) FROM lvye_crisis_intervention " +
            "    WHERE deleted = 0 GROUP BY student_profile_id " +
            "  ) " +
            ") latest_ci ON sp.id = latest_ci.student_profile_id " +
            "LEFT JOIN system_users su ON latest_ci.handler_user_id = su.id " +
            "LEFT JOIN lvye_crisis_intervention ci ON sp.id = ci.student_profile_id AND ci.deleted = 0 " +
            "LEFT JOIN lvye_consultation_appointment ca ON sp.id = ca.student_profile_id AND ca.deleted = 0 " +
            "LEFT JOIN ( " +
            "  SELECT student_profile_id, id, status, update_time " +
            "  FROM lvye_intervention_event " +
            "  WHERE deleted = 0 " +
            "  <if test='reqVO.interventionEventStatus == null'> AND status = 1 </if>" +
            "  AND id IN ( " +
            "    SELECT MAX(id) FROM lvye_intervention_event " +
            "    WHERE deleted = 0 " +
            "    <if test='reqVO.interventionEventStatus != null'> AND status = #{reqVO.interventionEventStatus} </if>" +
            "    GROUP BY student_profile_id " +
            "  ) " +
            ") ie ON sp.id = ie.student_profile_id " +
            "WHERE sp.deleted = 0 " +
            "<if test='reqVO.riskLevel != null'> AND sp.risk_level = #{reqVO.riskLevel} </if>" +
            "<if test='reqVO.classId != null'> AND sp.class_dept_id = #{reqVO.classId} </if>" +
            "<if test='reqVO.gradeId != null'> AND sp.grade_dept_id = #{reqVO.gradeId} </if>" +
            "<if test='reqVO.studentName != null and reqVO.studentName != \"\"'> AND sp.name LIKE CONCAT('%', #{reqVO.studentName}, '%') </if>" +
            "<if test='reqVO.studentNumber != null and reqVO.studentNumber != \"\"'> AND sp.student_no LIKE CONCAT('%', #{reqVO.studentNumber}, '%') </if>" +
            "<if test='reqVO.studyStatus != null'> AND sp.graduation_status = #{reqVO.studyStatus} </if>" +
            "<if test='reqVO.counselorUserId != null'> " +
            "  AND EXISTS (SELECT 1 FROM lvye_crisis_intervention ci_counselor WHERE ci_counselor.student_profile_id = sp.id AND ci_counselor.handler_user_id = #{reqVO.counselorUserId} AND ci_counselor.deleted = 0 AND ci_counselor.status IN (2, 3, 4, 5)) " +
            "</if>" +
            "<if test='reqVO.interventionEventStatus != null'> " +
            "  AND ie.status = #{reqVO.interventionEventStatus} " +
            "</if>" +
            "<if test='reqVO.excludeInterventionEventStatus != null'> " +
            "  AND NOT EXISTS (SELECT 1 FROM lvye_intervention_event ie_exclude WHERE ie_exclude.student_profile_id = sp.id AND ie_exclude.deleted = 0 AND ie_exclude.status = #{reqVO.excludeInterventionEventStatus}) " +
            "</if>" +
            "<choose>" +
            "  <when test='reqVO.excludeCrisisStatus != null'>" +
            "    AND EXISTS (SELECT 1 FROM lvye_crisis_intervention ci_status WHERE ci_status.student_profile_id = sp.id AND ci_status.status != #{reqVO.excludeCrisisStatus} AND ci_status.deleted = 0) " +
            "  </when>" +
            "  <when test='reqVO.interventionEventStatus == null'>" +
            "    AND EXISTS (SELECT 1 FROM lvye_crisis_intervention ci_status5 WHERE ci_status5.student_profile_id = sp.id AND ci_status5.status = 5 AND ci_status5.deleted = 0) " +
            "  </when>" +
            "</choose>" +
            "GROUP BY sp.id, sp.name, sp.student_no, sp.sex, sp.risk_level, sp.graduation_status, sp.update_time, d1.name, latest_ci.handler_user_id, su.nickname, ie.id, ie.update_time " +
            "<if test='reqVO.sortField != null and reqVO.sortField == \"riskLevel\"'>" +
            "  ORDER BY sp.risk_level " +
            "  <if test='reqVO.sortOrder != null and reqVO.sortOrder == \"desc\"'>DESC</if>" +
            "  <if test='reqVO.sortOrder == null or reqVO.sortOrder != \"desc\"'>ASC</if>" +
            "</if>" +
            "<if test='reqVO.sortField == null or reqVO.sortField != \"riskLevel\"'>" +
            "  ORDER BY sp.risk_level ASC, sp.id DESC" +
            "</if>" +
            " LIMIT #{offset}, #{limit}" +
            "</script>")
    List<InterventionStudentRespVO> selectInterventionStudentPage(
            @Param("reqVO") InterventionDashboardReqVO reqVO,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 统计干预学生总数
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT sp.id) " +
            "FROM lvye_student_profile sp " +
            "LEFT JOIN lvye_intervention_event ie ON sp.id = ie.student_profile_id AND ie.deleted = 0 " +
            "WHERE sp.deleted = 0 " +
            "<if test='reqVO.riskLevel != null'> AND sp.risk_level = #{reqVO.riskLevel} </if>" +
            "<if test='reqVO.classId != null'> AND sp.class_dept_id = #{reqVO.classId} </if>" +
            "<if test='reqVO.gradeId != null'> AND sp.grade_dept_id = #{reqVO.gradeId} </if>" +
            "<if test='reqVO.studentName != null and reqVO.studentName != \"\"'> AND sp.name LIKE CONCAT('%', #{reqVO.studentName}, '%') </if>" +
            "<if test='reqVO.studentNumber != null and reqVO.studentNumber != \"\"'> AND sp.student_no LIKE CONCAT('%', #{reqVO.studentNumber}, '%') </if>" +
            "<if test='reqVO.studyStatus != null'> AND sp.graduation_status = #{reqVO.studyStatus} </if>" +
            "<if test='reqVO.counselorUserId != null'> " +
            "  AND EXISTS (SELECT 1 FROM lvye_crisis_intervention ci_counselor WHERE ci_counselor.student_profile_id = sp.id AND ci_counselor.handler_user_id = #{reqVO.counselorUserId} AND ci_counselor.deleted = 0 AND ci_counselor.status IN (2, 3, 4, 5)) " +
            "</if>" +
            "<if test='reqVO.interventionEventStatus != null'> " +
            "  AND EXISTS (SELECT 1 FROM lvye_intervention_event ie_status WHERE ie_status.student_profile_id = sp.id AND ie_status.deleted = 0 AND ie_status.status = #{reqVO.interventionEventStatus}) " +
            "</if>" +
            "<if test='reqVO.excludeInterventionEventStatus != null'> " +
            "  AND NOT EXISTS (SELECT 1 FROM lvye_intervention_event ie_exclude WHERE ie_exclude.student_profile_id = sp.id AND ie_exclude.deleted = 0 AND ie_exclude.status = #{reqVO.excludeInterventionEventStatus}) " +
            "</if>" +
            "<choose>" +
            "  <when test='reqVO.excludeCrisisStatus != null'>" +
            "    AND EXISTS (SELECT 1 FROM lvye_crisis_intervention ci_status WHERE ci_status.student_profile_id = sp.id AND ci_status.status != #{reqVO.excludeCrisisStatus} AND ci_status.deleted = 0) " +
            "  </when>" +
            "  <when test='reqVO.interventionEventStatus == null'>" +
            "    AND EXISTS (SELECT 1 FROM lvye_crisis_intervention ci_status5 WHERE ci_status5.student_profile_id = sp.id AND ci_status5.status = 5 AND ci_status5.deleted = 0) " +
            "  </when>" +
            "</choose>" +
            "</script>")
    Long countInterventionStudent(@Param("reqVO") InterventionDashboardReqVO reqVO);

    /**
     * 按风险等级统计学生数量（带过滤条件）
     */
    @Select("<script>" +
            "SELECT COUNT(sp.id) " +
            "FROM lvye_student_profile sp " +
            "WHERE sp.deleted = 0 AND sp.risk_level = #{riskLevel} " +
            "<if test='classId != null'> AND sp.class_dept_id = #{classId} </if>" +
            "<if test='gradeId != null'> AND sp.grade_dept_id = #{gradeId} </if>" +
            "<if test='counselorUserId != null'> " +
            "  AND EXISTS (SELECT 1 FROM lvye_crisis_intervention ci WHERE ci.student_profile_id = sp.id AND ci.handler_user_id = #{counselorUserId} AND ci.deleted = 0 AND ci.status IN (2, 3, 4, 5)) " +
            "</if>" +
            "<if test='excludeInterventionEventStatus != null'> " +
            "  AND NOT EXISTS (SELECT 1 FROM lvye_intervention_event ie_exclude WHERE ie_exclude.student_profile_id = sp.id AND ie_exclude.deleted = 0 AND ie_exclude.status = #{excludeInterventionEventStatus}) " +
            "</if>" +
            "</script>")
    Long countByRiskLevelWithFilter(
            @Param("riskLevel") Integer riskLevel,
            @Param("classId") Long classId,
            @Param("gradeId") Long gradeId,
            @Param("counselorUserId") Long counselorUserId,
            @Param("excludeInterventionEventStatus") Integer excludeInterventionEventStatus);

    /**
     * 统计待评估学生数量（风险等级为空或为0）
     */
    @Select("<script>" +
            "SELECT COUNT(sp.id) " +
            "FROM lvye_student_profile sp " +
            "WHERE sp.deleted = 0 AND (sp.risk_level IS NULL OR sp.risk_level = 0) " +
            "<if test='classId != null'> AND sp.class_dept_id = #{classId} </if>" +
            "<if test='gradeId != null'> AND sp.grade_dept_id = #{gradeId} </if>" +
            "<if test='counselorUserId != null'> " +
            "  AND EXISTS (SELECT 1 FROM lvye_crisis_intervention ci WHERE ci.student_profile_id = sp.id AND ci.handler_user_id = #{counselorUserId} AND ci.deleted = 0 AND ci.status IN (2, 3, 4, 5)) " +
            "</if>" +
            "</script>")
    Long countPendingAssessmentWithFilter(
            @Param("classId") Long classId,
            @Param("gradeId") Long gradeId,
            @Param("counselorUserId") Long counselorUserId);
}