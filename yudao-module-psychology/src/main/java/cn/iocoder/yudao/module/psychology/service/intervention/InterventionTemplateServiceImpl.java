package cn.iocoder.yudao.module.psychology.service.intervention;

import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateCreateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateStepVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.intervention.InterventionTemplateDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.intervention.InterventionTemplateStepDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.intervention.InterventionTemplateMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.intervention.InterventionTemplateStepMapper;
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
    public List<InterventionTemplateCreateReqVO> getTemplateList() {
        // 1. 查询当前租户下的所有模板
        List<InterventionTemplateDO> templates = templateMapper.selectList();

        // 2. 查询所有模板ID
        List<Long> templateIds = templates.stream()
                .map(InterventionTemplateDO::getId)
                .collect(Collectors.toList());

        // 3. 查询所有步骤并按模板ID分组
        List<InterventionTemplateStepDO> allSteps = new ArrayList<>();
        for (Long templateId : templateIds) {
            List<InterventionTemplateStepDO> steps = stepMapper.selectListByTemplateId(templateId);
            allSteps.addAll(steps);
        }

        // 4. 按模板ID分组步骤
        Map<Long, List<InterventionTemplateStepDO>> stepsByTemplateId = allSteps.stream()
                .collect(Collectors.groupingBy(InterventionTemplateStepDO::getTemplateId));

        // 5. 构建返回结果
        List<InterventionTemplateCreateReqVO> result = new ArrayList<>();
        for (InterventionTemplateDO template : templates) {
            InterventionTemplateCreateReqVO templateVO = new InterventionTemplateCreateReqVO();
            templateVO.setTitle(template.getTitle());
            templateVO.setIsOfficial(template.getIsOfficial());

            // 获取当前模板的步骤并按sort升序排序
            List<InterventionTemplateStepDO> steps = stepsByTemplateId.get(template.getId());
            if (steps != null) {
                List<InterventionTemplateStepVO> stepVOs = steps.stream()
                        .sorted(Comparator.comparing(InterventionTemplateStepDO::getSort))
                        .map(step -> {
                            InterventionTemplateStepVO stepVO = new InterventionTemplateStepVO();
                            stepVO.setSort(step.getSort());
                            stepVO.setTitle(step.getTitle());
                            return stepVO;
                        })
                        .collect(Collectors.toList());
                templateVO.setSteps(stepVOs);
            } else {
                templateVO.setSteps(new ArrayList<>());
            }

            result.add(templateVO);
        }

        return result;
    }

}
