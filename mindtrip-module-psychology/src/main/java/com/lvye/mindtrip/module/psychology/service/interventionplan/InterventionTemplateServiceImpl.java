package com.lvye.mindtrip.module.psychology.service.interventionplan;

import cn.hutool.core.collection.CollUtil;
import com.lvye.mindtrip.framework.common.util.collection.CollectionUtils;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateCreateReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateSimpleRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateStepRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateStepVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateUpdateReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.interventionplan.InterventionTemplateDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.interventionplan.InterventionTemplateStepDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.interventionplan.InterventionTemplateMapper;
import com.lvye.mindtrip.module.psychology.dal.mysql.interventionplan.InterventionTemplateStepMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lvye.mindtrip.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lvye.mindtrip.framework.common.util.collection.CollectionUtils.convertList;
import static com.lvye.mindtrip.module.psychology.enums.ErrorCodeConstants.INTERVENTION_TEMPLATE_NOT_EXISTS;

/**
 * 干预计划模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class InterventionTemplateServiceImpl implements InterventionTemplateService {

    @Resource
    private InterventionTemplateMapper templateMapper;

    @Resource
    private InterventionTemplateStepMapper stepMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(InterventionTemplateCreateReqVO createReqVO) {
        // 1. 创建模板
        InterventionTemplateDO template = new InterventionTemplateDO();
        template.setTitle(createReqVO.getTitle());
        template.setIsOfficial(createReqVO.getIsOfficial() != null ? createReqVO.getIsOfficial() : false);
        templateMapper.insert(template);

        // 2. 获取模板ID
        Long templateId = template.getId();

        // 3. 批量创建步骤
        List<InterventionTemplateStepDO> steps = new ArrayList<>();
        for (InterventionTemplateStepVO stepVO : createReqVO.getSteps()) {
            InterventionTemplateStepDO step = new InterventionTemplateStepDO();
            step.setTemplateId(templateId);
            step.setSort(stepVO.getSort());
            step.setTitle(stepVO.getTitle());
            steps.add(step);
        }

        // 4. 批量插入步骤
        for (InterventionTemplateStepDO step : steps) {
            stepMapper.insert(step);
        }

        log.info("[createTemplate] 创建干预计划模板成功，templateId: {}, title: {}, stepCount: {}",
                templateId, createReqVO.getTitle(), steps.size());

        return templateId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(InterventionTemplateUpdateReqVO updateReqVO) {
        // 1. 验证模板存在
        InterventionTemplateDO template = templateMapper.selectById(updateReqVO.getId());
        if (template == null) {
            throw exception(INTERVENTION_TEMPLATE_NOT_EXISTS);
        }

        // 2. 更新模板主表
        template.setTitle(updateReqVO.getTitle());
        if (updateReqVO.getIsOfficial() != null) {
            template.setIsOfficial(updateReqVO.getIsOfficial());
        }
        templateMapper.updateById(template);

        // 3. 对比更新步骤
        updateTemplateSteps(updateReqVO.getId(), updateReqVO.getSteps());

        log.info("[updateTemplate] 更新干预计划模板成功，templateId: {}, title: {}, stepCount: {}",
                updateReqVO.getId(), updateReqVO.getTitle(), updateReqVO.getSteps().size());
    }

    /**
     * 对比更新模板步骤
     *
     * @param templateId 模板ID
     * @param stepVOList 新的步骤列表
     */
    private void updateTemplateSteps(Long templateId, List<InterventionTemplateStepVO> stepVOList) {
        // 1. 查询现有步骤
        List<InterventionTemplateStepDO> oldSteps = stepMapper.selectListByTemplateId(templateId);

        // 2. 将 VO 转换为 DO
        List<InterventionTemplateStepDO> newSteps = stepVOList.stream()
                .map(stepVO -> {
                    InterventionTemplateStepDO step = new InterventionTemplateStepDO();
                    step.setId(stepVO.getId());
                    step.setTemplateId(templateId);
                    step.setSort(stepVO.getSort());
                    step.setTitle(stepVO.getTitle());
                    return step;
                })
                .collect(Collectors.toList());

        // 3. 使用 diffList 对比新旧列表
        List<List<InterventionTemplateStepDO>> diffList = CollectionUtils.diffList(oldSteps, newSteps,
                (oldStep, newStep) -> {
                    // 通过 ID 判断是否为同一步骤
                    if (newStep.getId() == null) {
                        return false; // 新步骤（ID为null）不可能匹配旧步骤
                    }
                    boolean isSame = oldStep.getId().equals(newStep.getId());
                    if (isSame) {
                        // 如果是同一步骤，保留旧的 ID（虽然已经设置了，但这里确保一致性）
                        newStep.setId(oldStep.getId());
                    }
                    return isSame;
                });

        // 4. 批量新增（diffList.get(0)）
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            // 新增的步骤需要设置 templateId
            diffList.get(0).forEach(step -> step.setTemplateId(templateId));
            stepMapper.insertBatch(diffList.get(0));
            log.info("[updateTemplateSteps] 新增步骤，templateId: {}, count: {}", templateId, diffList.get(0).size());
        }

        // 5. 批量更新（diffList.get(1)）
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            stepMapper.updateBatch(diffList.get(1));
            log.info("[updateTemplateSteps] 更新步骤，templateId: {}, count: {}", templateId, diffList.get(1).size());
        }

        // 6. 批量删除（diffList.get(2)）
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            List<Long> deleteIds = convertList(diffList.get(2), InterventionTemplateStepDO::getId);
            stepMapper.deleteBatchIds(deleteIds);
            log.info("[updateTemplateSteps] 删除步骤，templateId: {}, count: {}", templateId, diffList.get(2).size());
        }
    }

    @Override
    public List<InterventionTemplateRespVO> getTemplateList() {
        // 1. 查询当前租户下的所有模板
        List<InterventionTemplateDO> templates = templateMapper.selectList();
        if (CollUtil.isEmpty(templates)) {
            return new ArrayList<>();
        }

        // 2. 批量查询所有模板的步骤（避免 N+1 查询问题）
        List<Long> templateIds = convertList(templates, InterventionTemplateDO::getId);
        List<InterventionTemplateStepDO> allSteps = stepMapper.selectList(
                "template_id", templateIds
        );

        // 3. 按 templateId 分组
        Map<Long, List<InterventionTemplateStepDO>> stepMap = allSteps.stream()
                .collect(Collectors.groupingBy(InterventionTemplateStepDO::getTemplateId));

        // 4. 组装返回结果
        return templates.stream()
                .map(template -> {
                    InterventionTemplateRespVO vo = new InterventionTemplateRespVO();
                    vo.setId(template.getId());
                    vo.setTitle(template.getTitle());
                    vo.setIsOfficial(template.getIsOfficial());

                    // 获取该模板的步骤并按 sort 排序
                    List<InterventionTemplateStepDO> steps = stepMap.getOrDefault(
                            template.getId(), new ArrayList<>()
                    );
                    List<InterventionTemplateStepRespVO> stepVOs = steps.stream()
                            .sorted(Comparator.comparing(InterventionTemplateStepDO::getSort))
                            .map(step -> {
                                InterventionTemplateStepRespVO stepVO = new InterventionTemplateStepRespVO();
                                stepVO.setId(step.getId());
                                stepVO.setSort(step.getSort());
                                stepVO.setTitle(step.getTitle());
                                return stepVO;
                            })
                            .collect(Collectors.toList());
                    vo.setSteps(stepVOs);

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public InterventionTemplateRespVO getTemplateById(Long id) {
        // 1. 查询模板
        InterventionTemplateDO template = templateMapper.selectById(id);
        if (template == null) {
            throw exception(INTERVENTION_TEMPLATE_NOT_EXISTS);
        }

        // 2. 查询步骤
        List<InterventionTemplateStepDO> steps = stepMapper.selectListByTemplateId(id);

        // 3. 转换为 VO
        InterventionTemplateRespVO respVO = new InterventionTemplateRespVO();
        respVO.setId(template.getId());
        respVO.setTitle(template.getTitle());
        respVO.setIsOfficial(template.getIsOfficial());

        // 4. 转换步骤并按 sort 排序
        List<InterventionTemplateStepRespVO> stepVOs = steps.stream()
                .sorted(Comparator.comparing(InterventionTemplateStepDO::getSort))
                .map(step -> {
                    InterventionTemplateStepRespVO stepVO = new InterventionTemplateStepRespVO();
                    stepVO.setId(step.getId());
                    stepVO.setSort(step.getSort());
                    stepVO.setTitle(step.getTitle());
                    return stepVO;
                })
                .collect(Collectors.toList());
        respVO.setSteps(stepVOs);

        log.info("[getTemplateById] 获取干预计划模板详情，templateId: {}, title: {}, stepCount: {}",
                id, template.getTitle(), stepVOs.size());

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        // 1. 验证模板存在
        InterventionTemplateDO template = templateMapper.selectById(id);
        if (template == null) {
            throw exception(INTERVENTION_TEMPLATE_NOT_EXISTS);
        }

        // 2. 删除模板关联的所有步骤（级联删除）
        stepMapper.deleteByTemplateId(id);
        log.info("[deleteTemplate] 删除模板步骤，templateId: {}", id);

        // 3. 删除模板主表
        templateMapper.deleteById(id);
        log.info("[deleteTemplate] 删除干预计划模板成功，templateId: {}, title: {}", id, template.getTitle());
    }

}
