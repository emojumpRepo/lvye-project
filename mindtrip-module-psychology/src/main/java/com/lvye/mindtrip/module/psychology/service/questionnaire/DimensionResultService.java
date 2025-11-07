package com.lvye.mindtrip.module.psychology.service.questionnaire;

import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;

import java.util.List;

/**
 * 维度结果服务接口
 *
 * @author MinGoo
 */
public interface DimensionResultService {

    /**
     * 保存维度结果
     *
     * @param dimensionResult 维度结果
     * @return 维度结果ID
     */
    Long saveDimensionResult(DimensionResultDO dimensionResult);

    /**
     * 批量保存维度结果
     *
     * @param dimensionResults 维度结果列表
     */
    void batchSaveDimensionResults(List<DimensionResultDO> dimensionResults);

    /**
     * 根据问卷结果ID查询维度结果列表
     *
     * @param questionnaireResultId 问卷结果ID
     * @return 维度结果列表
     */
    List<DimensionResultDO> getDimensionResultsByQuestionnaireResultId(Long questionnaireResultId);

    /**
     * 根据问卷结果ID和维度ID查询维度结果
     *
     * @param questionnaireResultId 问卷结果ID
     * @param dimensionId 维度ID
     * @return 维度结果
     */
    DimensionResultDO getDimensionResult(Long questionnaireResultId, Long dimensionId);

    /**
     * 根据维度ID查询所有结果
     *
     * @param dimensionId 维度ID
     * @return 维度结果列表
     */
    List<DimensionResultDO> getDimensionResultsByDimensionId(Long dimensionId);


    /**
     * 更新维度结果
     *
     * @param dimensionResult 维度结果
     */
    void updateDimensionResult(DimensionResultDO dimensionResult);

    /**
     * 删除维度结果
     *
     * @param id 维度结果ID
     */
    void deleteDimensionResult(Long id);

    /**
     * 删除指定问卷结果的所有维度结果
     *
     * @param questionnaireResultId 问卷结果ID
     */
    void deleteDimensionResultsByQuestionnaireResultId(Long questionnaireResultId);
}
