package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.convert.questionnaire.QuestionnaireConvert;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.QuestionnaireStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.DictTypeConstants;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 问卷管理服务实现
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class QuestionnaireServiceImpl implements QuestionnaireService {

    @Resource
    private QuestionnaireMapper questionnaireMapper;

    @Resource
    private DictDataService dictDataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestionnaire(@Valid QuestionnaireCreateReqVO createReqVO) {
        // 转换为DO对象
        QuestionnaireDO questionnaire = QuestionnaireConvert.INSTANCE.convert(createReqVO);
        
        // 设置默认值
        questionnaire.setStatus(QuestionnaireStatusEnum.DRAFT.getStatus());
        questionnaire.setAccessCount(0);
        questionnaire.setCompletionCount(0);
        questionnaire.setSyncStatus(0);
        // 设置默认支持独立使用
        if (questionnaire.getSupportIndependentUse() == null) {
            questionnaire.setSupportIndependentUse(1);
        }
        
        // 插入数据库
        questionnaireMapper.insert(questionnaire);
        
        log.info("创建问卷成功，问卷ID: {}, 标题: {}", questionnaire.getId(), questionnaire.getTitle());
        return questionnaire.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionnaire(@Valid QuestionnaireUpdateReqVO updateReqVO) {
        // 验证问卷存在
        validateQuestionnaireExists(updateReqVO.getId());
        
        // 转换为DO对象
        QuestionnaireDO questionnaire = QuestionnaireConvert.INSTANCE.convert(updateReqVO);
        
        // 更新数据库
        questionnaireMapper.updateById(questionnaire);
        
        log.info("更新问卷成功，问卷ID: {}", questionnaire.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestionnaire(Long id) {
        // 验证问卷存在
        QuestionnaireDO questionnaire = validateQuestionnaireExists(id);
        
        // 检查是否可以删除（已发布的问卷不能删除）
        if (QuestionnaireStatusEnum.PUBLISHED.getStatus().equals(questionnaire.getStatus())) {
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_ALREADY_PUBLISHED, "已发布的问卷不能删除");
        }
        
        // 删除问卷
        questionnaireMapper.deleteById(id);
        
        log.info("删除问卷成功，问卷ID: {}", id);
    }

    @Override
    public QuestionnaireRespVO getQuestionnaire(Long id) {
        QuestionnaireDO questionnaire = questionnaireMapper.selectById(id);
        return QuestionnaireConvert.INSTANCE.convert(questionnaire);
    }

    @Override
    public PageResult<QuestionnaireRespVO> getQuestionnairePage(QuestionnairePageReqVO pageReqVO) {
        PageResult<QuestionnaireDO> pageResult = questionnaireMapper.selectPage(pageReqVO);
        return QuestionnaireConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public PageResult<cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireWithSurveyRespVO>
        getQuestionnairePageWithSurvey(QuestionnairePageReqVO pageReqVO) {
        PageResult<QuestionnaireDO> pageResult = questionnaireMapper.selectPage(pageReqVO);
        return QuestionnaireConvert.INSTANCE.convertWithSurveyPage(pageResult);
    }

    @Override
    public List<QuestionnaireRespVO> getAllQuestionnaireList() {
        List<QuestionnaireDO> list = questionnaireMapper.selectList();
        return QuestionnaireConvert.INSTANCE.convertList(list);
    }

    @Override
    public List<QuestionnaireSimpleRespVO> getSimpleQuestionnaireList() {
        List<QuestionnaireDO> list = questionnaireMapper.selectList();
        return list.stream()
                .map(this::convertToSimpleRespVO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<QuestionnaireSimpleRespVO> getSimpleQuestionnaireList(Integer supportIndependentUse) {
        List<QuestionnaireDO> list = questionnaireMapper.selectIndependentUseQuestionnaires(supportIndependentUse);
        return list.stream()
                .map(this::convertToSimpleRespVO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 手动转换 QuestionnaireDO 到 QuestionnaireSimpleRespVO
     */
    private QuestionnaireSimpleRespVO convertToSimpleRespVO(QuestionnaireDO questionnaire) {
        if (questionnaire == null) {
            return null;
        }
        
        QuestionnaireSimpleRespVO simple = new QuestionnaireSimpleRespVO();
        simple.setId(questionnaire.getId());
        simple.setTitle(questionnaire.getTitle());
        simple.setDescription(questionnaire.getDescription());
        simple.setQuestionnaireType(questionnaire.getQuestionnaireType());
        simple.setTargetAudience(questionnaire.getTargetAudience());
        simple.setSurveyCode(questionnaire.getSurveyCode());
        simple.setQuestionCount(questionnaire.getQuestionCount());
        simple.setEstimatedDuration(questionnaire.getEstimatedDuration());
        simple.setStatus(questionnaire.getStatus());
        simple.setSupportIndependentUse(questionnaire.getSupportIndependentUse());

        // 转换测评维度为标签
        simple.setAssessmentDimensionLabels(convertAssessmentDimensionToLabels(questionnaire.getAssessmentDimension()));
        
        return simple;
    }

    /**
     * 将测评维度的字典键值转换为标签列表
     */
    private List<String> convertAssessmentDimensionToLabels(String assessmentDimension) {
        if (assessmentDimension == null || assessmentDimension.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            String[] dimensionValues = assessmentDimension.split(",");
            List<String> labels = new ArrayList<>();
            
            for (String value : dimensionValues) {
                if (value.trim().isEmpty()) {
                    continue;
                }
                
                DictDataDO dictData = dictDataService.getDictData(DictTypeConstants.QUESTIONNAIRE_ASSESSMENT_DIMENSION, value.trim());
                if (dictData != null) {
                    labels.add(dictData.getLabel());
                } else {
                    // 如果字典中找不到对应的标签，使用原值
                    labels.add(value.trim());
                }
            }
            
            return labels;
        } catch (Exception e) {
            log.warn("转换测评维度标签失败: {}", assessmentDimension, e);
            // 转换失败时返回包含原值的列表
            return Arrays.asList(assessmentDimension);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishQuestionnaireToExternal(Long id) {
        // 验证问卷存在
        QuestionnaireDO questionnaire = validateQuestionnaireExists(id);
        
        // 检查问卷状态
        if (QuestionnaireStatusEnum.PUBLISHED.getStatus().equals(questionnaire.getStatus())) {
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_ALREADY_PUBLISHED);
        }
        
        // 更新问卷状态
        questionnaire.setStatus(QuestionnaireStatusEnum.PUBLISHED.getStatus());
        questionnaire.setSyncStatus(1);
        questionnaire.setLastSyncTime(LocalDateTime.now());
        questionnaireMapper.updateById(questionnaire);
        
        log.info("发布问卷到外部系统成功，问卷ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseQuestionnaireInExternal(Long id) {
        // 验证问卷存在
        QuestionnaireDO questionnaire = validateQuestionnaireExists(id);
        
        // 检查问卷状态
        if (!QuestionnaireStatusEnum.PUBLISHED.getStatus().equals(questionnaire.getStatus())) {
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_NOT_PUBLISHED);
        }
        
        // 更新问卷状态
        questionnaire.setStatus(QuestionnaireStatusEnum.PAUSED.getStatus());
        questionnaire.setLastSyncTime(LocalDateTime.now());
        questionnaireMapper.updateById(questionnaire);
        
        log.info("暂停外部问卷成功，问卷ID: {}", id);
    }

    @Override
    public boolean testQuestionnaireLink(Long id) {
        // 验证问卷存在
        QuestionnaireDO questionnaire = validateQuestionnaireExists(id);
        
        // 检查外部链接是否存在
        if (questionnaire.getExternalLink() == null || questionnaire.getExternalLink().trim().isEmpty()) {
            return false;
        }
        
        // TODO: 实际测试链接可访问性
        // 这里简单返回true，实际应该发送HTTP请求测试
        
        log.info("测试问卷链接，问卷ID: {}, 链接: {}", id, questionnaire.getExternalLink());
        return true;
    }

    @Override
    public List<QuestionnaireSimpleRespVO> getAvailableQuestionnaires(Integer targetAudience) {
        List<QuestionnaireDO> questionnaires = questionnaireMapper.selectAvailableQuestionnaires(null, targetAudience);
        return QuestionnaireConvert.INSTANCE.convertSimpleList(questionnaires);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionnaireStatus(Long id, Integer status) {
        // 验证问卷存在
        QuestionnaireDO questionnaire = validateQuestionnaireExists(id);
        
        // 更新状态
        questionnaire.setStatus(status);
        questionnaireMapper.updateById(questionnaire);
        
        log.info("更新问卷状态成功，问卷ID: {}, 新状态: {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseAccessCount(Long id) {
        questionnaireMapper.updateAccessCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseCompletionCount(Long id) {
        questionnaireMapper.updateCompletionCount(id);
    }

    /**
     * 验证问卷是否存在
     */
    private QuestionnaireDO validateQuestionnaireExists(Long id) {
        QuestionnaireDO questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw exception(ErrorCodeConstants.QUESTIONNAIRE_NOT_EXISTS);
        }
        return questionnaire;
    }

}