package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigSaveReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;

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
    QuestionnaireResultConfigDO getQuestionnaireResultConfig(Long id);

    /**
     * 获得问卷结果配置分页
     *
     * @param pageReqVO 分页查询
     * @return 问卷结果配置分页
     */
    PageResult<QuestionnaireResultConfigDO> getQuestionnaireResultConfigPage(QuestionnaireResultConfigPageReqVO pageReqVO);

    /**
     * 根据问卷ID获取结果配置列表
     *
     * @param questionnaireId 问卷ID
     * @return 结果配置列表
     */
    List<QuestionnaireResultConfigDO> getQuestionnaireResultConfigListByQuestionnaireId(Long questionnaireId);

    /**
     * 根据问卷ID和维度名称获取结果配置
     *
     * @param questionnaireId 问卷ID
     * @param dimensionName 维度名称
     * @return 结果配置
     */
    QuestionnaireResultConfigDO getQuestionnaireResultConfigByQuestionnaireIdAndDimensionName(Long questionnaireId, String dimensionName);

    /**
     * 根据问卷ID删除结果配置
     *
     * @param questionnaireId 问卷ID
     */
    void deleteQuestionnaireResultConfigByQuestionnaireId(Long questionnaireId);

}
