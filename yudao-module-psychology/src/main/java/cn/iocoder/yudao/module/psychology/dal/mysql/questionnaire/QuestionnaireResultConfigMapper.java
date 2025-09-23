package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigPageReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import org.apache.ibatis.annotations.Mapper;

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
                .eqIfPresent(QuestionnaireResultConfigDO::getQuestionnaireId, reqVO.getQuestionnaireId())
                .likeIfPresent(QuestionnaireResultConfigDO::getDimensionName, reqVO.getDimensionName())
                .eqIfPresent(QuestionnaireResultConfigDO::getCalculateType, reqVO.getCalculateType())
                .eqIfPresent(QuestionnaireResultConfigDO::getIsAbnormal, reqVO.getIsAbnormal())
                .likeIfPresent(QuestionnaireResultConfigDO::getLevel, reqVO.getLevel())
                .betweenIfPresent(QuestionnaireResultConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(QuestionnaireResultConfigDO::getId));
    }

    /**
     * 根据问卷ID查询结果配置列表
     */
    default List<QuestionnaireResultConfigDO> selectListByQuestionnaireId(Long questionnaireId) {
        return selectList(QuestionnaireResultConfigDO::getQuestionnaireId, questionnaireId);
    }

    /**
     * 根据问卷ID和维度名称查询结果配置
     */
    default QuestionnaireResultConfigDO selectByQuestionnaireIdAndDimensionName(Long questionnaireId, String dimensionName) {
        return selectOne(new LambdaQueryWrapperX<QuestionnaireResultConfigDO>()
                .eq(QuestionnaireResultConfigDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireResultConfigDO::getDimensionName, dimensionName));
    }

    /**
     * 根据问卷ID删除结果配置
     */
    default void deleteByQuestionnaireId(Long questionnaireId) {
        delete(new LambdaQueryWrapperX<QuestionnaireResultConfigDO>()
                .eq(QuestionnaireResultConfigDO::getQuestionnaireId, questionnaireId));
    }

}
