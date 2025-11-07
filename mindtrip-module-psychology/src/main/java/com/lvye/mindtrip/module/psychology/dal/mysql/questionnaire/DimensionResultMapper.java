package com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.MtuiDimensionResultQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 维度结果 Mapper
 *
 * @author MinGoo
 */
@Mapper
public interface DimensionResultMapper extends BaseMapperX<DimensionResultDO> {

    /**
     * 根据问卷结果ID查询维度结果列表
     */
    default List<DimensionResultDO> selectListByQuestionnaireResultId(Long questionnaireResultId) {
        return selectList(new LambdaQueryWrapperX<DimensionResultDO>()
                .eq(DimensionResultDO::getQuestionnaireResultId, questionnaireResultId)
                .orderByAsc(DimensionResultDO::getDimensionId));
    }

    /**
     * 根据问卷结果ID和维度ID查询维度结果
     */
    default DimensionResultDO selectByQuestionnaireResultIdAndDimensionId(Long questionnaireResultId, Long dimensionId) {
        return selectOne(new LambdaQueryWrapperX<DimensionResultDO>()
                .eq(DimensionResultDO::getQuestionnaireResultId, questionnaireResultId)
                .eq(DimensionResultDO::getDimensionId, dimensionId));
    }

    /**
     * 根据维度ID查询所有结果
     */
    default List<DimensionResultDO> selectListByDimensionId(Long dimensionId) {
        return selectList(DimensionResultDO::getDimensionId, dimensionId);
    }

    /**
     * 根据问卷结果ID删除所有维度结果
     */
    default void deleteByQuestionnaireResultId(Long questionnaireResultId) {
        delete(new LambdaQueryWrapperX<DimensionResultDO>()
                .eq(DimensionResultDO::getQuestionnaireResultId, questionnaireResultId));
    }

    /**
     * 查询MTUI大学维度结果(优化版)
     * 通过测评任务编号和用户ID,一次性JOIN查询出所有维度结果及相关信息
     *
     * @param assessmentTaskNo 测评任务编号
     * @param userId 用户ID
     * @return 维度结果列表(包含问卷信息和维度信息)
     */
    @Select("<script>" +
            "SELECT " +
            "  qr.id AS questionnaireResultId, " +
            "  qr.questionnaire_id AS questionnaireId, " +
            "  qr.user_id AS userId, " +
            "  qr.assessment_task_no AS assessmentTaskNo, " +
            "  qr.answers AS answers, " +
            "  qr.completed_time AS completedTime, " +
            "  q.title AS questionnaireName, " +
            "  q.description AS questionnaireDescription, " +
            "  q.questionnaire_type AS questionnaireType, " +
            "  dr.id AS dimensionResultId, " +
            "  dr.score AS score, " +
            "  dr.is_abnormal AS isAbnormal, " +
            "  dr.risk_level AS riskLevel, " +
            "  dr.level AS level, " +
            "  dr.teacher_comment AS teacherComment, " +
            "  dr.student_comment AS studentComment, " +
            "  qd.id AS dimensionId, " +
            "  qd.dimension_name AS dimensionName, " +
            "  qd.dimension_code AS dimensionCode, " +
            "  qd.description AS dimensionDescription, " +
            "  qd.sort_order AS sortOrder, " +
            "  qd.participate_module_calc AS participateModuleCalc, " +
            "  qd.participate_assessment_calc AS participateAssessmentCalc, " +
            "  qd.participate_ranking AS participateRanking, " +
            "  qd.show_score AS showScore " +
            "FROM lvye_questionnaire_result qr " +
            "INNER JOIN lvye_questionnaire q ON qr.questionnaire_id = q.id AND q.deleted = 0 " +
            "LEFT JOIN lvye_questionnaire_dimension qd ON q.id = qd.questionnaire_id AND qd.deleted = 0 " +
            "LEFT JOIN lvye_dimension_result dr ON qr.id = dr.questionnaire_result_id AND dr.dimension_id = qd.id AND dr.deleted = 0 " +
            "WHERE qr.assessment_task_no = #{assessmentTaskNo} " +
            "  AND qr.user_id = #{userId} " +
            "  AND qr.deleted = 0 " +
            "ORDER BY qr.id, qd.sort_order ASC" +
            "</script>")
    List<MtuiDimensionResultQueryDTO> selectMtuiDimensionResultsByTaskAndUser(
            @Param("assessmentTaskNo") String assessmentTaskNo,
            @Param("userId") Long userId);
}
