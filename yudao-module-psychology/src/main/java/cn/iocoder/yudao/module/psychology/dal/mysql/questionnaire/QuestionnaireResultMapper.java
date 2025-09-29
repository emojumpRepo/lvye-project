package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
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
        return selectPage((cn.iocoder.yudao.framework.common.pojo.PageParam) reqVO,
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


}