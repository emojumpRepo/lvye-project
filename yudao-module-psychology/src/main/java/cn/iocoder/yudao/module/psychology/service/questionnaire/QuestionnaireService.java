package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 问卷管理服务接口
 *
 * @author 芋道源码
 */
public interface QuestionnaireService {

    /**
     * 创建问卷
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQuestionnaire(@Valid QuestionnaireCreateReqVO createReqVO);

    /**
     * 更新问卷
     *
     * @param updateReqVO 更新信息
     */
    void updateQuestionnaire(@Valid QuestionnaireUpdateReqVO updateReqVO);

    /**
     * 删除问卷
     *
     * @param id 编号
     */
    void deleteQuestionnaire(Long id);

    /**
     * 获得问卷详情
     *
     * @param id 编号
     * @return 问卷信息
     */
    QuestionnaireRespVO getQuestionnaire(Long id);

    /**
     * 获得问卷分页列表
     *
     * @param pageReqVO 分页查询
     * @return 问卷分页
     */
    PageResult<QuestionnaireRespVO> getQuestionnairePage(QuestionnairePageReqVO pageReqVO);

    /**
     * 获得问卷分页列表（包含 surveyCode 字段）
     */
    PageResult<cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireWithSurveyRespVO>
        getQuestionnairePageWithSurvey(QuestionnairePageReqVO pageReqVO);

    /**
     * 获得所有问卷列表
     *
     * @return 问卷列表
     */
    List<QuestionnaireRespVO> getAllQuestionnaireList();

    /**
     * 获得问卷精简信息列表
     *
     * @return 问卷精简列表
     */
    List<QuestionnaireSimpleRespVO> getSimpleQuestionnaireList();

    /**
     * 获得支持独立使用的问卷精简信息列表
     *
     * @param supportIndependentUse 是否支持独立使用
     * @return 问卷精简列表
     */
    List<QuestionnaireSimpleRespVO> getSimpleQuestionnaireList(Integer supportIndependentUse);

    /**
     * 发布问卷到外部系统
     *
     * @param id 问卷编号
     */
    void publishQuestionnaireToExternal(Long id);

    /**
     * 暂停外部问卷
     *
     * @param id 问卷编号
     */
    void pauseQuestionnaireInExternal(Long id);

    /**
     * 测试问卷链接
     *
     * @param id 问卷编号
     * @return 是否可访问
     */
    boolean testQuestionnaireLink(Long id);

    /**
     * 获取可用问卷列表（用于测评任务选择）
     *
     * @param targetAudience 目标对象
     * @return 问卷列表
     */
    List<QuestionnaireSimpleRespVO> getAvailableQuestionnaires(Integer targetAudience);

    /**
     * 更新问卷状态
     *
     * @param id 问卷编号
     * @param status 状态
     */
    void updateQuestionnaireStatus(Long id, Integer status);

    /**
     * 增加访问次数
     *
     * @param id 问卷编号
     */
    void increaseAccessCount(Long id);

    /**
     * 增加完成次数
     *
     * @param id 问卷编号
     */
    void increaseCompletionCount(Long id);

}