package cn.iocoder.yudao.module.psychology.service.questionnaire.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigSaveReqVO;
import cn.iocoder.yudao.module.psychology.convert.questionnaire.QuestionnaireResultConfigConvert;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultConfigMapper;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

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
    public Long createQuestionnaireResultConfig(QuestionnaireResultConfigSaveReqVO createReqVO) {
        // 插入
        QuestionnaireResultConfigDO questionnaireResultConfig = QuestionnaireResultConfigConvert.INSTANCE.convert(createReqVO);
        questionnaireResultConfigMapper.insert(questionnaireResultConfig);
        // 返回
        return questionnaireResultConfig.getId();
    }

    @Override
    public void updateQuestionnaireResultConfig(QuestionnaireResultConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateQuestionnaireResultConfigExists(updateReqVO.getId());
        // 更新
        QuestionnaireResultConfigDO updateObj = QuestionnaireResultConfigConvert.INSTANCE.convert(updateReqVO);
        questionnaireResultConfigMapper.updateById(updateObj);
    }

    @Override
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
    public QuestionnaireResultConfigDO getQuestionnaireResultConfig(Long id) {
        return questionnaireResultConfigMapper.selectById(id);
    }

    @Override
    public PageResult<QuestionnaireResultConfigDO> getQuestionnaireResultConfigPage(QuestionnaireResultConfigPageReqVO pageReqVO) {
        return questionnaireResultConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public List<QuestionnaireResultConfigDO> getQuestionnaireResultConfigListByQuestionnaireId(Long questionnaireId) {
        return questionnaireResultConfigMapper.selectListByQuestionnaireId(questionnaireId);
    }

    @Override
    public QuestionnaireResultConfigDO getQuestionnaireResultConfigByQuestionnaireIdAndDimensionName(Long questionnaireId, String dimensionName) {
        return questionnaireResultConfigMapper.selectByQuestionnaireIdAndDimensionName(questionnaireId, dimensionName);
    }

    @Override
    public void deleteQuestionnaireResultConfigByQuestionnaireId(Long questionnaireId) {
        questionnaireResultConfigMapper.deleteByQuestionnaireId(questionnaireId);
    }

}
