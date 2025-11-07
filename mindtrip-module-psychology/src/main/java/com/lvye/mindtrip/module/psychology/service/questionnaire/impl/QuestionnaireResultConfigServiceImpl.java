package com.lvye.mindtrip.module.psychology.service.questionnaire.impl;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.tenant.core.aop.TenantIgnore;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigPageReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigSaveReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigRespVO;
import com.lvye.mindtrip.module.psychology.convert.questionnaire.QuestionnaireResultConfigConvert;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire.QuestionnaireResultConfigMapper;
import com.lvye.mindtrip.module.psychology.enums.ErrorCodeConstants;
import com.lvye.mindtrip.module.psychology.service.questionnaire.QuestionnaireResultConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static com.lvye.mindtrip.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 问卷结果配置 Service 实现类
 *
 * @author MinGoo
 */
@Service
@Validated
@Slf4j
public class QuestionnaireResultConfigServiceImpl implements QuestionnaireResultConfigService {

    @Resource
    private QuestionnaireResultConfigMapper questionnaireResultConfigMapper;

    @Override
    @TenantIgnore
    public Long createQuestionnaireResultConfig(QuestionnaireResultConfigSaveReqVO createReqVO) {
        // 插入
        QuestionnaireResultConfigDO questionnaireResultConfig = QuestionnaireResultConfigConvert.INSTANCE.convert(createReqVO);
        questionnaireResultConfigMapper.insert(questionnaireResultConfig);
        // 返回
        return questionnaireResultConfig.getId();
    }

    @Override
    @TenantIgnore
    public void updateQuestionnaireResultConfig(QuestionnaireResultConfigSaveReqVO updateReqVO) {
        log.info("更新问卷结果配置: id={}, riskLevel={}", updateReqVO.getId(), updateReqVO.getRiskLevel());
        
        // 校验存在
        validateQuestionnaireResultConfigExists(updateReqVO.getId());
        
        // 更新
        QuestionnaireResultConfigDO updateObj = QuestionnaireResultConfigConvert.INSTANCE.convert(updateReqVO);
        log.info("转换后的DO对象: id={}, riskLevel={}", updateObj.getId(), updateObj.getRiskLevel());
        
        questionnaireResultConfigMapper.updateById(updateObj);
        
        // 验证更新结果
        QuestionnaireResultConfigDO updated = questionnaireResultConfigMapper.selectById(updateReqVO.getId());
        log.info("更新后的数据库记录: id={}, riskLevel={}", updated.getId(), updated.getRiskLevel());
    }

    @Override
    @TenantIgnore
    public void deleteQuestionnaireResultConfig(Long id) {
        // 校验存在
        validateQuestionnaireResultConfigExists(id);
        // 删除
        questionnaireResultConfigMapper.deleteById(id);
    }

    private void validateQuestionnaireResultConfigExists(Long id) {
        if (questionnaireResultConfigMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_RESULT_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    @TenantIgnore
    public QuestionnaireResultConfigRespVO getQuestionnaireResultConfig(Long id) {
        return QuestionnaireResultConfigConvert.INSTANCE.convert(questionnaireResultConfigMapper.selectById(id));
    }

    @Override
    @TenantIgnore
    public PageResult<QuestionnaireResultConfigRespVO> getQuestionnaireResultConfigPage(QuestionnaireResultConfigPageReqVO pageReqVO) {
        return QuestionnaireResultConfigConvert.INSTANCE.convertPage(questionnaireResultConfigMapper.selectPage(pageReqVO));
    }

    @Override
    @TenantIgnore
    public List<QuestionnaireResultConfigDO> getQuestionnaireResultConfigListByDimensionId(Long dimensionId) {
        return questionnaireResultConfigMapper.selectListByDimensionId(dimensionId);
    }

    @Override
    @TenantIgnore
    public void deleteQuestionnaireResultConfigByDimensionId(Long dimensionId) {
        questionnaireResultConfigMapper.deleteByDimensionId(dimensionId);
    }

    @Override
    @TenantIgnore
    public List<QuestionnaireResultConfigDO> getQuestionnaireResultConfigListByQuestionnaireId(Long questionnaireId) {
        return questionnaireResultConfigMapper.selectListByQuestionnaireId(questionnaireId);
    }

}
