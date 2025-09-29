package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDimensionDO;

import java.util.List;

/**
 * 问卷维度服务接口
 */
public interface QuestionnaireDimensionService {

    /**
     * 创建问卷维度
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDimension(QuestionnaireDimensionCreateReqVO createReqVO);

    /**
     * 更新问卷维度
     *
     * @param updateReqVO 更新信息
     */
    void updateDimension(QuestionnaireDimensionUpdateReqVO updateReqVO);

    /**
     * 删除问卷维度
     *
     * @param id 编号
     */
    void deleteDimension(Long id);

    /**
     * 获得问卷维度
     *
     * @param id 编号
     * @return 问卷维度
     */
    QuestionnaireDimensionRespVO getDimension(Long id);

    /**
     * 获得问卷维度分页
     *
     * @param pageReqVO 分页查询
     * @return 问卷维度分页
     */
    PageResult<QuestionnaireDimensionRespVO> getDimensionPage(QuestionnaireDimensionPageReqVO pageReqVO);

    /**
     * 获得问卷维度列表
     *
     * @return 问卷维度列表
     */
    List<QuestionnaireDimensionRespVO> getDimensionList();

    /**
     * 根据问卷ID获得维度列表
     *
     * @param questionnaireId 问卷ID
     * @return 维度列表
     */
    List<QuestionnaireDimensionRespVO> getDimensionListByQuestionnaire(Long questionnaireId);

    /**
     * 根据问卷ID获得维度DO列表（用于内部计算）
     *
     * @param questionnaireId 问卷ID
     * @return 维度DO列表
     */
    List<QuestionnaireDimensionDO> getListByQuestionnaireId(Long questionnaireId);

    /**
     * 批量创建问卷维度
     *
     * @param createReqVOList 创建信息列表
     * @return 编号列表
     */
    List<Long> batchCreateDimensions(List<QuestionnaireDimensionCreateReqVO> createReqVOList);

    /**
     * 更新问卷维度状态
     *
     * @param id     编号
     * @param status 状态
     */
    void updateDimensionStatus(Long id, Integer status);

    /**
     * 批量更新维度参与设置
     *
     * @param updateReqVO 更新信息
     */
    void updateParticipateSettings(QuestionnaireDimensionParticipateUpdateReqVO updateReqVO);

    /**
     * 校验问卷维度是否存在
     *
     * @param id 编号
     */
    void validateDimensionExists(Long id);

    /**
     * 校验维度编码是否唯一
     *
     * @param questionnaireId 问卷ID
     * @param dimensionCode   维度编码
     * @param id              排除的维度ID
     */
    void validateDimensionCodeUnique(Long questionnaireId, String dimensionCode, Long id);
}
