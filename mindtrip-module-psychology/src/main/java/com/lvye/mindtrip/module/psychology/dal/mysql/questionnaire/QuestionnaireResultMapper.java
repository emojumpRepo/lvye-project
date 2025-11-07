package com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
        return selectPage((com.lvye.mindtrip.framework.common.pojo.PageParam) reqVO,
                new LambdaQueryWrapperX<QuestionnaireResultDO>()
                        .orderByDesc(QuestionnaireResultDO::getId));
    }

    /**
     * 查询同一个任务id完成问卷的数量
     *
     * @param AssessmentTaskNo
     * @param userId
     * @return
     */
    default Long selectCountByTaskNoAndUserId(String AssessmentTaskNo, Long userId) {
        return selectCount(new LambdaQueryWrapper<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getAssessmentTaskNo, AssessmentTaskNo)
                .eq(QuestionnaireResultDO::getUserId, userId)
                .isNotNull(QuestionnaireResultDO::getScore));
    }

    /**
     * 通过业务唯一主键查询
     * @param AssessmentTaskNo
     * @param userId
     * @param questionnaireId
     * @return
     */
    default QuestionnaireResultDO selectByUnique(String AssessmentTaskNo, Long userId, Long questionnaireId) {
        return selectOne(new LambdaQueryWrapper<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getAssessmentTaskNo, AssessmentTaskNo)
                .eq(QuestionnaireResultDO::getUserId, userId)
                .eq(QuestionnaireResultDO::getQuestionnaireId, questionnaireId));
    }

    /**
     * 通过任务编号和用户ID查询
     * @param taskNo
     * @param userId
     * @return
     */
    default List<QuestionnaireResultDO> selectListByTaskNoAndUserId(String taskNo, Long userId){
        return selectList(new LambdaQueryWrapper<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getAssessmentTaskNo, taskNo)
                .eq(QuestionnaireResultDO::getUserId, userId));
    }

    /**
     * 根据用户ID和问卷ID查询问卷结果列表
     */
    default List<QuestionnaireResultDO> selectListByUserIdAndQuestionnaireId(Long userId, Long questionnaireId) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getUserId, userId)
                .eq(QuestionnaireResultDO::getQuestionnaireId, questionnaireId)
                .orderByDesc(QuestionnaireResultDO::getCreateTime));
    }

    /**
     * 根据任务编号查询所有问卷结果
     */
    default List<QuestionnaireResultDO> selectListByTaskNo(String taskNo) {
        return selectList(new LambdaQueryWrapper<QuestionnaireResultDO>()
                .eq(QuestionnaireResultDO::getAssessmentTaskNo, taskNo));
    }


}