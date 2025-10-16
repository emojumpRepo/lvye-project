package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 心理问卷 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface QuestionnaireMapper extends BaseMapperX<QuestionnaireDO> {

    /**
     * 分页查询问卷
     */
    default PageResult<QuestionnaireDO> selectPage(cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnairePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QuestionnaireDO>()
                .likeIfPresent(QuestionnaireDO::getTitle, reqVO.getTitle())
                .eqIfPresent(QuestionnaireDO::getQuestionnaireType, reqVO.getQuestionnaireType())
                .eqIfPresent(QuestionnaireDO::getTargetAudience, reqVO.getTargetAudience())
                .likeIfPresent(QuestionnaireDO::getSurveyCode, reqVO.getSurveyCode())
                .eqIfPresent(QuestionnaireDO::getStatus, reqVO.getStatus())
                .eqIfPresent(QuestionnaireDO::getSupportIndependentUse, reqVO.getSupportIndependentUse())
                .betweenIfPresent(QuestionnaireDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(QuestionnaireDO::getId));
    }

    /**
     * 根据外部ID查询问卷
     */
    default QuestionnaireDO selectByExternalId(String externalId) {
        return selectOne(QuestionnaireDO::getExternalId, externalId);
    }

    /**
     * 根据状态查询问卷列表
     */
    default List<QuestionnaireDO> selectListByStatus(Integer status) {
        return selectList(QuestionnaireDO::getStatus, status);
    }

    /**
     * 根据问卷类型和目标对象查询可用问卷
     */
    default List<QuestionnaireDO> selectAvailableQuestionnaires(Integer questionnaireType, Integer targetAudience) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireDO>()
                .eq(QuestionnaireDO::getStatus, 1) // 已发布状态
                .eqIfPresent(QuestionnaireDO::getQuestionnaireType, questionnaireType)
                .eqIfPresent(QuestionnaireDO::getTargetAudience, targetAudience)
                .orderByDesc(QuestionnaireDO::getId));
    }

    /**
     * 查询支持独立使用的问卷列表
     */
    default List<QuestionnaireDO> selectIndependentUseQuestionnaires(Integer supportIndependentUse) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireDO>()
                .eq(QuestionnaireDO::getStatus, 1) // 已发布状态
                .eqIfPresent(QuestionnaireDO::getSupportIndependentUse, supportIndependentUse)
                .orderByDesc(QuestionnaireDO::getId));
    }

    /**
     * 更新访问次数
     */
    default void updateAccessCount(Long id) {
        // 简化实现：使用基础的更新方法
        QuestionnaireDO questionnaire = selectById(id);
        if (questionnaire != null) {
            questionnaire.setAccessCount(questionnaire.getAccessCount() + 1);
            updateById(questionnaire);
        }
    }

    /**
     * 更新完成次数
     */
    default void updateCompletionCount(Long id) {
        // 简化实现：使用基础的更新方法
        QuestionnaireDO questionnaire = selectById(id);
        if (questionnaire != null) {
            questionnaire.setCompletionCount(questionnaire.getCompletionCount() + 1);
            updateById(questionnaire);
        }
    }

    /**
     * 批量查询问卷
     * @param ids 问卷ID集合
     * @return 问卷列表
     */
    default List<QuestionnaireDO> selectListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return selectList(new LambdaQueryWrapperX<QuestionnaireDO>()
                .in(QuestionnaireDO::getId, ids));
    }

}