package cn.iocoder.yudao.module.psychology.service.questionnaire.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.DimensionResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.DimensionResultMapper;
import cn.iocoder.yudao.module.psychology.service.questionnaire.DimensionResultService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 维度结果服务实现类
 *
 * @author MinGoo
 */
@Slf4j
@Service
public class DimensionResultServiceImpl implements DimensionResultService {

    @Resource
    private DimensionResultMapper dimensionResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDimensionResult(DimensionResultDO dimensionResult) {
        log.info("保存维度结果: dimensionId={}, questionnaireResultId={}", 
            dimensionResult.getDimensionId(), dimensionResult.getQuestionnaireResultId());
        
        // 检查是否已存在相同的维度结果
        DimensionResultDO existing = dimensionResultMapper.selectByQuestionnaireResultIdAndDimensionId(
            dimensionResult.getQuestionnaireResultId(), dimensionResult.getDimensionId());
        
        if (existing != null) {
            // 更新现有记录
            dimensionResult.setId(existing.getId());
            dimensionResult.setUpdateTime(LocalDateTime.now());
            dimensionResultMapper.updateById(dimensionResult);
            log.info("更新维度结果成功: id={}", existing.getId());
            return existing.getId();
        } else {
            // 插入新记录
            dimensionResult.setCreateTime(LocalDateTime.now());
            dimensionResult.setUpdateTime(LocalDateTime.now());
            dimensionResultMapper.insert(dimensionResult);
            log.info("新增维度结果成功: id={}", dimensionResult.getId());
            return dimensionResult.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveDimensionResults(List<DimensionResultDO> dimensionResults) {
        if (dimensionResults == null || dimensionResults.isEmpty()) {
            log.warn("批量保存维度结果: 结果列表为空");
            return;
        }
        
        log.info("批量保存维度结果: 数量={}", dimensionResults.size());
        
        for (DimensionResultDO dimensionResult : dimensionResults) {
            saveDimensionResult(dimensionResult);
        }
        
        log.info("批量保存维度结果完成");
    }

    @Override
    public List<DimensionResultDO> getDimensionResultsByQuestionnaireResultId(Long questionnaireResultId) {
        return dimensionResultMapper.selectListByQuestionnaireResultId(questionnaireResultId);
    }

    @Override
    public DimensionResultDO getDimensionResult(Long questionnaireResultId, Long dimensionId) {
        return dimensionResultMapper.selectByQuestionnaireResultIdAndDimensionId(questionnaireResultId, dimensionId);
    }

    @Override
    public List<DimensionResultDO> getDimensionResultsByDimensionId(Long dimensionId) {
        return dimensionResultMapper.selectListByDimensionId(dimensionId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDimensionResult(DimensionResultDO dimensionResult) {
        log.info("更新维度结果: id={}", dimensionResult.getId());
        dimensionResult.setUpdateTime(LocalDateTime.now());
        dimensionResultMapper.updateById(dimensionResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDimensionResult(Long id) {
        log.info("删除维度结果: id={}", id);
        dimensionResultMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDimensionResultsByQuestionnaireResultId(Long questionnaireResultId) {
        log.info("删除指定问卷结果的所有维度结果: questionnaireResultId={}", questionnaireResultId);
        dimensionResultMapper.deleteByQuestionnaireResultId(questionnaireResultId);
    }
}
