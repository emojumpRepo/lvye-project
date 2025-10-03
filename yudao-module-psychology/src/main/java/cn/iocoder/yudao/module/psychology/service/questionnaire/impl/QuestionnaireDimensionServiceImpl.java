package cn.iocoder.yudao.module.psychology.service.questionnaire.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.convert.questionnaire.QuestionnaireDimensionConvert;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDimensionDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireDimensionMapper;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireDimensionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.*;

/**
 * 问卷维度服务实现类
 */
@Service
@Slf4j
public class QuestionnaireDimensionServiceImpl implements QuestionnaireDimensionService {

    @Resource
    private QuestionnaireDimensionMapper questionnaireDimensionMapper;

    @Override
    @Transactional
    public Long createDimension(QuestionnaireDimensionCreateReqVO createReqVO) {
        // 校验维度编码唯一性
        validateDimensionCodeUnique(createReqVO.getQuestionnaireId(), createReqVO.getDimensionCode(), null);
        
        // 插入
        QuestionnaireDimensionDO dimension = BeanUtils.toBean(createReqVO, QuestionnaireDimensionDO.class);
        // 处理Boolean到Integer的转换
        dimension.setParticipateModuleCalc(booleanToInteger(createReqVO.getParticipateModuleCalc()));
        dimension.setParticipateAssessmentCalc(booleanToInteger(createReqVO.getParticipateAssessmentCalc()));
        dimension.setParticipateRanking(booleanToInteger(createReqVO.getParticipateRanking()));
        
        dimension.setCreateTime(LocalDateTime.now());
        dimension.setUpdateTime(LocalDateTime.now());
        dimension.setDeleted(false);
        
        questionnaireDimensionMapper.insert(dimension);
        
        log.info("创建问卷维度成功: questionnaireId={}, dimensionName={}, dimensionCode={}", 
            createReqVO.getQuestionnaireId(), createReqVO.getDimensionName(), createReqVO.getDimensionCode());
        
        return dimension.getId();
    }

    @Override
    @Transactional
    public void updateDimension(QuestionnaireDimensionUpdateReqVO updateReqVO) {
        // 校验存在
        validateDimensionExists(updateReqVO.getId());
        
        // 校验维度编码唯一性
        validateDimensionCodeUnique(updateReqVO.getQuestionnaireId(), updateReqVO.getDimensionCode(), updateReqVO.getId());
        
        // 更新
        QuestionnaireDimensionDO updateObj = BeanUtils.toBean(updateReqVO, QuestionnaireDimensionDO.class);
        // 处理Boolean到Integer的转换
        updateObj.setParticipateModuleCalc(booleanToInteger(updateReqVO.getParticipateModuleCalc()));
        updateObj.setParticipateAssessmentCalc(booleanToInteger(updateReqVO.getParticipateAssessmentCalc()));
        updateObj.setParticipateRanking(booleanToInteger(updateReqVO.getParticipateRanking()));
        
        updateObj.setUpdateTime(LocalDateTime.now());
        
        questionnaireDimensionMapper.updateById(updateObj);
        
        log.info("更新问卷维度成功: id={}, dimensionName={}", updateReqVO.getId(), updateReqVO.getDimensionName());
    }

    @Override
    @Transactional
    public void deleteDimension(Long id) {
        // 校验存在
        validateDimensionExists(id);
        
        // TODO: 校验是否被维度结果配置引用，如果被引用则不能删除
        
        // 删除
        questionnaireDimensionMapper.deleteById(id);
        
        log.info("删除问卷维度成功: id={}", id);
    }

    @Override
    public QuestionnaireDimensionRespVO getDimension(Long id) {
        QuestionnaireDimensionDO dimension = questionnaireDimensionMapper.selectById(id);
        return QuestionnaireDimensionConvert.INSTANCE.convert(dimension);
    }

    @Override
    public PageResult<QuestionnaireDimensionRespVO> getDimensionPage(QuestionnaireDimensionPageReqVO pageReqVO) {
        LambdaQueryWrapper<QuestionnaireDimensionDO> wrapper = new LambdaQueryWrapper<QuestionnaireDimensionDO>()
            .eq(pageReqVO.getQuestionnaireId() != null, QuestionnaireDimensionDO::getQuestionnaireId, pageReqVO.getQuestionnaireId())
            .like(pageReqVO.getDimensionName() != null, QuestionnaireDimensionDO::getDimensionName, pageReqVO.getDimensionName())
            .like(pageReqVO.getDimensionCode() != null, QuestionnaireDimensionDO::getDimensionCode, pageReqVO.getDimensionCode())
            .eq(pageReqVO.getParticipateModuleCalc() != null, QuestionnaireDimensionDO::getParticipateModuleCalc, pageReqVO.getParticipateModuleCalc())
            .eq(pageReqVO.getParticipateAssessmentCalc() != null, QuestionnaireDimensionDO::getParticipateAssessmentCalc, pageReqVO.getParticipateAssessmentCalc())
            .eq(pageReqVO.getParticipateRanking() != null, QuestionnaireDimensionDO::getParticipateRanking, pageReqVO.getParticipateRanking())
            .eq(pageReqVO.getStatus() != null, QuestionnaireDimensionDO::getStatus, pageReqVO.getStatus())
            .orderByAsc(QuestionnaireDimensionDO::getSortOrder)
            .orderByDesc(QuestionnaireDimensionDO::getCreateTime);
        
        PageResult<QuestionnaireDimensionDO> pageResult = questionnaireDimensionMapper.selectPage(pageReqVO, wrapper);
        return QuestionnaireDimensionConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<QuestionnaireDimensionRespVO> getDimensionList() {
        List<QuestionnaireDimensionDO> list = questionnaireDimensionMapper.selectList(
            new LambdaQueryWrapper<QuestionnaireDimensionDO>()
                .eq(QuestionnaireDimensionDO::getStatus, 1)
                .orderByAsc(QuestionnaireDimensionDO::getSortOrder)
                .orderByDesc(QuestionnaireDimensionDO::getCreateTime)
        );
        return QuestionnaireDimensionConvert.INSTANCE.convertList(list);
    }

    @Override
    public List<QuestionnaireDimensionRespVO> getDimensionListByQuestionnaire(Long questionnaireId) {
        List<QuestionnaireDimensionDO> list = questionnaireDimensionMapper.selectList(
            new LambdaQueryWrapper<QuestionnaireDimensionDO>()
                .eq(QuestionnaireDimensionDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireDimensionDO::getStatus, 1)
                .orderByAsc(QuestionnaireDimensionDO::getSortOrder)
        );
        return QuestionnaireDimensionConvert.INSTANCE.convertList(list);
    }

    @Override
    public List<QuestionnaireDimensionDO> getListByQuestionnaireId(Long questionnaireId) {
        return questionnaireDimensionMapper.selectList(
            new LambdaQueryWrapper<QuestionnaireDimensionDO>()
                .eq(QuestionnaireDimensionDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireDimensionDO::getStatus, 1)
                .orderByAsc(QuestionnaireDimensionDO::getSortOrder)
        );
    }

    @Override
    @Transactional
    public List<Long> batchCreateDimensions(List<QuestionnaireDimensionCreateReqVO> createReqVOList) {
        List<Long> dimensionIds = new ArrayList<>();
        
        for (QuestionnaireDimensionCreateReqVO createReqVO : createReqVOList) {
            // 校验维度编码唯一性
            validateDimensionCodeUnique(createReqVO.getQuestionnaireId(), createReqVO.getDimensionCode(), null);
            
            // 插入
            QuestionnaireDimensionDO dimension = BeanUtils.toBean(createReqVO, QuestionnaireDimensionDO.class);
            // 处理Boolean到Integer的转换
            dimension.setParticipateModuleCalc(booleanToInteger(createReqVO.getParticipateModuleCalc()));
            dimension.setParticipateAssessmentCalc(booleanToInteger(createReqVO.getParticipateAssessmentCalc()));
            dimension.setParticipateRanking(booleanToInteger(createReqVO.getParticipateRanking()));
            
            dimension.setCreateTime(LocalDateTime.now());
            dimension.setUpdateTime(LocalDateTime.now());
            dimension.setDeleted(false);
            
            questionnaireDimensionMapper.insert(dimension);
            dimensionIds.add(dimension.getId());
        }
        
        log.info("批量创建问卷维度成功: count={}", createReqVOList.size());
        return dimensionIds;
    }

    @Override
    @Transactional
    public void updateDimensionStatus(Long id, Integer status) {
        // 校验存在
        validateDimensionExists(id);
        
        // 更新状态
        QuestionnaireDimensionDO updateObj = new QuestionnaireDimensionDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        updateObj.setUpdateTime(LocalDateTime.now());
        
        questionnaireDimensionMapper.updateById(updateObj);
        
        log.info("更新问卷维度状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional
    public void updateParticipateSettings(QuestionnaireDimensionParticipateUpdateReqVO updateReqVO) {
        LambdaUpdateWrapper<QuestionnaireDimensionDO> wrapper = new LambdaUpdateWrapper<QuestionnaireDimensionDO>()
            .in(QuestionnaireDimensionDO::getId, updateReqVO.getDimensionIds())
            .set(QuestionnaireDimensionDO::getUpdateTime, LocalDateTime.now());
        
        // 根据操作类型更新对应字段
        switch (updateReqVO.getOperationType()) {
            case "MODULE":
                wrapper.set(QuestionnaireDimensionDO::getParticipateModuleCalc, booleanToInteger(updateReqVO.getParticipateModuleCalc()));
                break;
            case "ASSESSMENT":
                wrapper.set(QuestionnaireDimensionDO::getParticipateAssessmentCalc, booleanToInteger(updateReqVO.getParticipateAssessmentCalc()));
                break;
            case "RANKING":
                wrapper.set(QuestionnaireDimensionDO::getParticipateRanking, booleanToInteger(updateReqVO.getParticipateRanking()));
                break;
            case "ALL":
                if (updateReqVO.getParticipateModuleCalc() != null) {
                    wrapper.set(QuestionnaireDimensionDO::getParticipateModuleCalc, booleanToInteger(updateReqVO.getParticipateModuleCalc()));
                }
                if (updateReqVO.getParticipateAssessmentCalc() != null) {
                    wrapper.set(QuestionnaireDimensionDO::getParticipateAssessmentCalc, booleanToInteger(updateReqVO.getParticipateAssessmentCalc()));
                }
                if (updateReqVO.getParticipateRanking() != null) {
                    wrapper.set(QuestionnaireDimensionDO::getParticipateRanking, booleanToInteger(updateReqVO.getParticipateRanking()));
                }
                break;
            default:
                throw exception(QUESTIONNAIRE_DIMENSION_OPERATION_TYPE_INVALID);
        }
        
        questionnaireDimensionMapper.update(null, wrapper);
        
        log.info("批量更新维度参与设置成功: dimensionIds={}, operationType={}", 
            updateReqVO.getDimensionIds(), updateReqVO.getOperationType());
    }

    @Override
    public void validateDimensionExists(Long id) {
        if (questionnaireDimensionMapper.selectById(id) == null) {
            throw exception(QUESTIONNAIRE_DIMENSION_NOT_EXISTS);
        }
    }

    @Override
    public void validateDimensionCodeUnique(Long questionnaireId, String dimensionCode, Long id) {
        QuestionnaireDimensionDO dimension = questionnaireDimensionMapper.selectOne(
            new LambdaQueryWrapper<QuestionnaireDimensionDO>()
                .eq(QuestionnaireDimensionDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireDimensionDO::getDimensionCode, dimensionCode)
                .ne(id != null, QuestionnaireDimensionDO::getId, id)
        );
        
        if (dimension != null) {
            throw exception(QUESTIONNAIRE_DIMENSION_CODE_DUPLICATE);
        }
    }

    /**
     * Boolean转Integer的辅助方法
     */
    private Integer booleanToInteger(Boolean value) {
        if (value == null || !value) {
            return 0;
        }
        return 1;
    }
}
