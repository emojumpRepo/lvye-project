package com.lvye.mindtrip.module.psychology.service.questionnaire;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigPageReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigSaveReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigRespVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 问卷结果配置 Service 接口
 *
 * @author MinGoo
 */
public interface QuestionnaireResultConfigService {

    /**
     * 创建问卷结果配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQuestionnaireResultConfig(@Valid QuestionnaireResultConfigSaveReqVO createReqVO);

    /**
     * 更新问卷结果配置
     *
     * @param updateReqVO 更新信息
     */
    void updateQuestionnaireResultConfig(@Valid QuestionnaireResultConfigSaveReqVO updateReqVO);

    /**
     * 删除问卷结果配置
     *
     * @param id 编号
     */
    void deleteQuestionnaireResultConfig(Long id);

    /**
     * 获得问卷结果配置
     *
     * @param id 编号
     * @return 问卷结果配置
     */
    QuestionnaireResultConfigRespVO getQuestionnaireResultConfig(Long id);

    /**
     * 获得问卷结果配置分页
     *
     * @param pageReqVO 分页查询
     * @return 问卷结果配置分页
     */
    PageResult<QuestionnaireResultConfigRespVO> getQuestionnaireResultConfigPage(QuestionnaireResultConfigPageReqVO pageReqVO);

    /**
     * 根据维度ID获取结果配置列表
     *
     * @param dimensionId 维度ID
     * @return 结果配置列表
     */
    List<QuestionnaireResultConfigDO> getQuestionnaireResultConfigListByDimensionId(Long dimensionId);

    /**
     * 根据维度ID删除结果配置
     *
     * @param dimensionId 维度ID
     */
    void deleteQuestionnaireResultConfigByDimensionId(Long dimensionId);

    /**
     * 根据问卷ID获取所有维度的结果配置列表（兼容旧接口）
     *
     * @param questionnaireId 问卷ID
     * @return 结果配置列表
     */
    List<QuestionnaireResultConfigDO> getQuestionnaireResultConfigListByQuestionnaireId(Long questionnaireId);

}
