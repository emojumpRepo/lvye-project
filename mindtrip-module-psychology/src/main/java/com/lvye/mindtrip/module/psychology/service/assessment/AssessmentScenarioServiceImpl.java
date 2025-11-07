package com.lvye.mindtrip.module.psychology.service.assessment;

import com.lvye.mindtrip.framework.common.util.object.BeanUtils;
import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.framework.tenant.core.aop.TenantIgnore;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioPageReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentScenarioDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentScenarioSlotDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.assessment.AssessmentScenarioMapper;
import com.lvye.mindtrip.module.psychology.dal.mysql.assessment.AssessmentScenarioSlotMapper;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import static com.lvye.mindtrip.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lvye.mindtrip.module.psychology.enums.ErrorCodeConstants.*;
import com.lvye.mindtrip.module.psychology.service.questionnaire.QuestionnaireService;
import com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.QuestionnaireRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.ScenarioQuestionnaireAccessVO;

/**
 * 测评场景 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AssessmentScenarioServiceImpl implements AssessmentScenarioService {

    @Resource
    private AssessmentScenarioMapper scenarioMapper;

    @Resource
    private AssessmentScenarioSlotMapper scenarioSlotMapper;

    @Resource
    private QuestionnaireService questionnaireService;

    @Override
    @Transactional
    public Long createScenario(AssessmentScenarioVO scenarioVO) {
        // 校验场景编码唯一性
        validateScenarioCodeUnique(scenarioVO.getCode(), null);

        // 创建场景
        AssessmentScenarioDO scenario = BeanUtils.toBean(scenarioVO, AssessmentScenarioDO.class);
        scenarioMapper.insert(scenario);

        // 创建插槽
        if (scenarioVO.getSlots() != null && !scenarioVO.getSlots().isEmpty()) {
            createScenarioSlots(scenario.getId(), scenarioVO.getSlots());
        }

        return scenario.getId();
    }

    @Override
    @Transactional
    public void updateScenario(AssessmentScenarioVO scenarioVO) {
        // 校验场景存在
        validateScenarioExists(scenarioVO.getId());

        // 校验场景编码唯一性
        validateScenarioCodeUnique(scenarioVO.getCode(), scenarioVO.getId());

        // 更新场景
        AssessmentScenarioDO updateObj = BeanUtils.toBean(scenarioVO, AssessmentScenarioDO.class);
        scenarioMapper.updateById(updateObj);

        // 更新插槽
        if (scenarioVO.getSlots() != null) {
            updateScenarioSlotsInternal(scenarioVO.getId(), scenarioVO.getSlots());
        }
    }

    @Override
    @Transactional
    public void deleteScenario(Long id) {
        // 校验场景存在
        validateScenarioExists(id);
        
        // 删除场景
        scenarioMapper.deleteById(id);
    }

    @Override
    @TenantIgnore
    public AssessmentScenarioDO getScenario(Long id) {
        return scenarioMapper.selectById(id);
    }

    @Override
    @TenantIgnore
    public List<AssessmentScenarioVO> getActiveScenarioList() {
        List<AssessmentScenarioDO> activeList = scenarioMapper.selectList(AssessmentScenarioDO::getIsActive, true);
        if (activeList == null || activeList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<AssessmentScenarioVO> result = new java.util.ArrayList<>();
        for (AssessmentScenarioDO scenario : activeList) {
            List<AssessmentScenarioSlotDO> slots = scenarioSlotMapper.selectListByScenarioId(scenario.getId());
            if (slots == null || slots.isEmpty()) {
                continue; // 必须有插槽
            }
            boolean allSlotsLinkedToQuestionnaire = true;
            java.util.List<AssessmentScenarioVO.ScenarioSlotVO> slotVOs = new java.util.ArrayList<>();
            for (AssessmentScenarioSlotDO slot : slots) {
                if (slot.getQuestionnaireIds() == null || slot.getQuestionnaireIds().trim().isEmpty()) {
                    allSlotsLinkedToQuestionnaire = false;
                    break;
                }
                AssessmentScenarioVO.ScenarioSlotVO slotVO = BeanUtils.toBean(slot, AssessmentScenarioVO.ScenarioSlotVO.class);
                
                // 直接返回逗号分隔字符串给VO
                slotVO.setQuestionnaireIds(slot.getQuestionnaireIds());
                
                // 查询问卷详情并填充
                List<ScenarioQuestionnaireAccessVO> questionnaires = new java.util.ArrayList<>();
                for (Long questionnaireId : parseQuestionnaireIds(slot.getQuestionnaireIds())) {
                    QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(questionnaireId);
                    if (questionnaire != null) {
                        ScenarioQuestionnaireAccessVO info = new ScenarioQuestionnaireAccessVO();
                        info.setId(questionnaire.getId());
                        info.setTitle(questionnaire.getTitle());
                        info.setDescription(questionnaire.getDescription());
                        info.setQuestionnaireType(questionnaire.getQuestionnaireType());
                        info.setTargetAudience(questionnaire.getTargetAudience());
                        info.setQuestionCount(questionnaire.getQuestionCount());
                        info.setExternalLink(questionnaire.getExternalLink());
                        info.setEstimatedDuration(questionnaire.getEstimatedDuration());
                        info.setStatus(questionnaire.getStatus());
                        questionnaires.add(info);
                    }
                }
                slotVO.setQuestionnaires(questionnaires);
                slotVOs.add(slotVO);
            }
            if (!allSlotsLinkedToQuestionnaire) {
                continue;
            }
            AssessmentScenarioVO vo = BeanUtils.toBean(scenario, AssessmentScenarioVO.class);
            vo.setSlots(slotVOs);
            result.add(vo);
        }
        return result;
    }

    @Override
    @TenantIgnore
    public PageResult<AssessmentScenarioDO> getScenarioPage(AssessmentScenarioPageReqVO pageReqVO) {
        return scenarioMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<AssessmentScenarioDO>()
                .likeIfPresent(AssessmentScenarioDO::getCode, pageReqVO.getCode())
                .likeIfPresent(AssessmentScenarioDO::getName, pageReqVO.getName())
                .likeIfPresent(AssessmentScenarioDO::getDescription, pageReqVO.getDescription())
                .eqIfPresent(AssessmentScenarioDO::getIsActive, pageReqVO.getIsActive()));
    }

    @Override
    public List<AssessmentScenarioSlotDO> getScenarioSlots(Long scenarioId) {
        List<AssessmentScenarioSlotDO> slots = scenarioSlotMapper.selectListByScenarioId(scenarioId);
        if (slots != null) {
            for (AssessmentScenarioSlotDO slot : slots) {
                // 解析逗号分隔字符串为数字数组，便于前端直接使用
                List<Long> ids = parseQuestionnaireIds(slot.getQuestionnaireIds());
                slot.setQuestionnaireIdList(ids);
            }
        }
        return slots;
    }

    @Override
    public AssessmentScenarioDO validateScenarioActive(Long scenarioId) {
        if (scenarioId == null) {
            return null;
        }
        
        AssessmentScenarioDO scenario = scenarioMapper.selectById(scenarioId);
        if (scenario == null) {
            throw exception(ASSESSMENT_SCENARIO_NOT_EXISTS);
        }
        
        if (scenario.getIsActive() == null || !scenario.getIsActive()) {
            throw exception(ASSESSMENT_SCENARIO_NOT_ACTIVE);
        }
        
        return scenario;
    }

    @Override
    public void validateQuestionnaireCount(Long scenarioId, int questionnaireCount) {
        AssessmentScenarioDO scenario = getScenario(scenarioId);
        if (scenario != null && scenario.getMaxQuestionnaireCount() != null) {
            if (questionnaireCount > scenario.getMaxQuestionnaireCount()) {
                throw exception(ASSESSMENT_SCENARIO_QUESTIONNAIRE_COUNT_EXCEEDED);
            }
        }
    }

    @Override
    @TenantIgnore
    public com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO getScenarioQuestionnaire(Long id, Long userId) {
        AssessmentScenarioDO scenario = scenarioMapper.selectById(id);
        if (scenario == null) {
            return null;
        }
        // 基本信息
        com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO vo = com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO.class.cast(
                BeanUtils.toBean(scenario, com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO.class));
        // 插槽与问卷详情
        java.util.List<AssessmentScenarioSlotDO> slots = scenarioSlotMapper.selectListByScenarioId(id);
        java.util.List<com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO.ScenarioSlotVO> slotVOs = new java.util.ArrayList<>();
        if (slots != null) {
            for (AssessmentScenarioSlotDO slot : slots) {
                com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO.ScenarioSlotVO slotVO = BeanUtils.toBean(
                        slot,
                        com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO.ScenarioSlotVO.class);
                
                // 直接返回逗号分隔字符串给VO
                slotVO.setQuestionnaireIds(slot.getQuestionnaireIds());
                
                // 查询问卷详情并填充
                List<com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.ScenarioQuestionnaireAccessVO> questionnaires = new ArrayList<>();
                for (Long questionnaireId : parseQuestionnaireIds(slot.getQuestionnaireIds())) {
                    com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(questionnaireId);
                    if (questionnaire != null) {
                        com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.ScenarioQuestionnaireAccessVO info = new com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.ScenarioQuestionnaireAccessVO();
                        info.setId(questionnaire.getId());
                        info.setTitle(questionnaire.getTitle());
                        info.setDescription(questionnaire.getDescription());
                        info.setQuestionnaireType(questionnaire.getQuestionnaireType());
                        info.setTargetAudience(questionnaire.getTargetAudience());
                        info.setQuestionCount(questionnaire.getQuestionCount());
                        info.setExternalLink(questionnaire.getExternalLink());
                        info.setEstimatedDuration(questionnaire.getEstimatedDuration());
                        info.setStatus(questionnaire.getStatus());
                        questionnaires.add(info);
                    }
                }
                slotVO.setQuestionnaires(questionnaires);
                slotVOs.add(slotVO);
            }
        }
        vo.setSlots(slotVOs);
        return vo;
    }

    /**
     * 校验场景是否存在
     */
    private void validateScenarioExists(Long id) {
        if (scenarioMapper.selectById(id) == null) {
            throw exception(ASSESSMENT_SCENARIO_NOT_EXISTS);
        }
    }

    /**
     * 校验场景编码唯一性（同一租户内）
     */
    private void validateScenarioCodeUnique(String code, Long id) {
        // 在当前租户内查询相同code的场景
        AssessmentScenarioDO scenario = scenarioMapper.selectOne(
            new LambdaQueryWrapperX<AssessmentScenarioDO>()
                .eq(AssessmentScenarioDO::getCode, code)
        );
        if (scenario != null && !scenario.getId().equals(id)) {
            throw exception(ASSESSMENT_SCENARIO_CODE_DUPLICATE);
        }
    }

    /**
     * 创建场景插槽
     */
    private void createScenarioSlots(Long scenarioId, List<AssessmentScenarioVO.ScenarioSlotVO> slotVOs) {
        // 校验插槽编码唯一性
        validateSlotKeysUnique(slotVOs);

        for (AssessmentScenarioVO.ScenarioSlotVO slotVO : slotVOs) {
            AssessmentScenarioSlotDO slot = BeanUtils.toBean(slotVO, AssessmentScenarioSlotDO.class);
            slot.setScenarioId(scenarioId);
            // 序列化问卷ID列表为JSON字符串
            slot.setQuestionnaireIds(serializeQuestionnaireIds(slotVO.getQuestionnaireIds()));
            // 清除ID，让数据库自动生成新的主键，避免主键冲突
            slot.setId(null);
            scenarioSlotMapper.insert(slot);
        }
    }

    @Override
    @Transactional
    public void updateScenarioSlots(Long scenarioId, List<AssessmentScenarioVO.ScenarioSlotVO> slotVOs) {
        // 校验场景存在
        validateScenarioExists(scenarioId);

        // 删除原有插槽
        scenarioSlotMapper.deleteByScenarioId(scenarioId);

        // 重新创建插槽
        if (slotVOs != null && !slotVOs.isEmpty()) {
            createScenarioSlots(scenarioId, slotVOs);
        }
    }

    /**
     * 更新场景插槽（私有方法）
     */
    private void updateScenarioSlotsInternal(Long scenarioId, List<AssessmentScenarioVO.ScenarioSlotVO> slotVOs) {
        // 删除原有插槽
        scenarioSlotMapper.deleteByScenarioId(scenarioId);

        // 重新创建插槽
        if (!slotVOs.isEmpty()) {
            createScenarioSlots(scenarioId, slotVOs);
        }
    }

    /**
     * 校验插槽编码唯一性（在同一场景内）
     */
    private void validateSlotKeysUnique(List<AssessmentScenarioVO.ScenarioSlotVO> slotVOs) {
        Set<String> slotKeys = new HashSet<>();
        for (AssessmentScenarioVO.ScenarioSlotVO slotVO : slotVOs) {
            if (!slotKeys.add(slotVO.getSlotKey())) {
                throw exception(ASSESSMENT_SCENARIO_SLOT_KEY_DUPLICATE);
            }
        }
    }

    /**
     * 解析问卷ID列表JSON字符串
     */
    private List<Long> parseQuestionnaireIds(String questionnaireIdsStr) {
        if (questionnaireIdsStr == null || questionnaireIdsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> list = new ArrayList<>();
        for (String part : questionnaireIdsStr.split(",")) {
            String s = part.trim();
            if (!s.isEmpty()) {
                try { list.add(Long.parseLong(s)); } catch (Exception ignore) {}
            }
        }
        return list;
    }

    /**
     * 序列化问卷ID列表为JSON字符串
     */
    private String serializeQuestionnaireIds(String questionnaireIds) { return questionnaireIds; }
}
