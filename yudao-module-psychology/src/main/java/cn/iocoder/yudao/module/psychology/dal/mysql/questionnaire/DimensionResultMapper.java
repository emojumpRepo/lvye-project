package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import org.apache.ibatis.annotations.Mapper;

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
}
