package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 问卷结果 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface QuestionnaireResultMapper extends BaseMapperX<QuestionnaireResultDO> {

    /**
     * 分页查询问卷结果
     */
    default PageResult<QuestionnaireResultDO> selectPage(Object reqVO) {
        // TODO: 待创建QuestionnaireResultPageReqVO后替换Object类型
        return selectPage((cn.iocoder.yudao.framework.common.pojo.PageParam) reqVO, 
                new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .orderByDesc(QuestionnaireResultDO::getId));
    }

    /**
     * 根据用户ID查询问卷结果列表
     */
    default List<QuestionnaireResultDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getUserId, userId)
                .orderByDesc(QuestionnaireResultDO::getCompletedTime));
    }

    /**
     * 根据测评任务ID查询问卷结果列表
     */
    default List<QuestionnaireResultDO> selectListByAssessmentTaskId(Long assessmentTaskId) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getAssessmentTaskId, assessmentTaskId)
                .orderByDesc(QuestionnaireResultDO::getCompletedTime));
    }

    /**
     * 根据测评结果ID查询问卷结果列表
     */
    default List<QuestionnaireResultDO> selectListByAssessmentResultId(Long assessmentResultId) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getAssessmentResultId, assessmentResultId)
                .orderByDesc(QuestionnaireResultDO::getCompletedTime));
    }

    /**
     * 根据问卷ID和用户ID查询结果
     */
    default QuestionnaireResultDO selectByQuestionnaireAndUser(Long questionnaireId, Long userId, Long assessmentTaskId) {
        return selectOne(new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireResultDO::getUserId, userId)
                .eqIfPresent(QuestionnaireResultDO::getAssessmentTaskId, assessmentTaskId));
    }

    /**
     * 根据生成状态查询结果列表
     */
    default List<QuestionnaireResultDO> selectListByGenerationStatus(Integer generationStatus) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getGenerationStatus, generationStatus)
                .orderByAsc(QuestionnaireResultDO::getCreateTime));
    }

    /**
     * 统计问卷完成数量
     */
    default Long countByQuestionnaireId(Long questionnaireId) {
        return selectCount(new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireResultDO::getGenerationStatus, 2)); // 已生成状态
    }

    /**
     * 统计风险等级分布
     */
    default Long countByRiskLevel(Long questionnaireId, Integer riskLevel) {
        return selectCount(new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireResultDO::getRiskLevel, riskLevel)
                .eq(QuestionnaireResultDO::getGenerationStatus, 2)); // 已生成状态
    }

}