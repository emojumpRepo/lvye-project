package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigPageReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 问卷结果配置 Mapper
 *
 * @author MinGoo
 */
@Mapper
public interface QuestionnaireResultConfigMapper extends BaseMapperX<QuestionnaireResultConfigDO> {

    /**
     * 分页查询问卷结果配置
     */
    default PageResult<QuestionnaireResultConfigDO> selectPage(QuestionnaireResultConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QuestionnaireResultConfigDO>()
                .eqIfPresent(QuestionnaireResultConfigDO::getDimensionId, reqVO.getDimensionId())
                .eqIfPresent(QuestionnaireResultConfigDO::getCalculateType, reqVO.getCalculateType())
                .eqIfPresent(QuestionnaireResultConfigDO::getIsAbnormal, reqVO.getIsAbnormal())
                .eqIfPresent(QuestionnaireResultConfigDO::getRiskLevel, reqVO.getRiskLevel())
                .likeIfPresent(QuestionnaireResultConfigDO::getLevel, reqVO.getLevel())
                .betweenIfPresent(QuestionnaireResultConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(QuestionnaireResultConfigDO::getMatchOrder)
                .orderByDesc(QuestionnaireResultConfigDO::getId));
    }

    /**
     * 根据维度ID查询结果配置列表
     */
    default List<QuestionnaireResultConfigDO> selectListByDimensionId(Long dimensionId) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireResultConfigDO>()
                .eq(QuestionnaireResultConfigDO::getDimensionId, dimensionId)
                .orderByAsc(QuestionnaireResultConfigDO::getMatchOrder)
                .orderByAsc(QuestionnaireResultConfigDO::getId));
    }

    /**
     * 根据维度ID删除结果配置
     */
    default void deleteByDimensionId(Long dimensionId) {
        delete(new LambdaQueryWrapperX<QuestionnaireResultConfigDO>()
                .eq(QuestionnaireResultConfigDO::getDimensionId, dimensionId));
    }

    /**
     * 根据问卷ID获取所有维度的结果配置列表（通过维度表关联查询）
     */
    @Select("SELECT qrc.* FROM lvye_questionnaire_result_config qrc " +
            "INNER JOIN lvye_questionnaire_dimension qd ON qrc.dimension_id = qd.id " +
            "WHERE qd.questionnaire_id = #{questionnaireId} " +
            "ORDER BY qd.sort_order, qrc.match_order ASC, qrc.id ASC")
    List<QuestionnaireResultConfigDO> selectListByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

}
