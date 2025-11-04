package cn.iocoder.yudao.module.psychology.service.interventionplan;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanCreateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisInterventionDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.interventionplan.InterventionEventDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.interventionplan.InterventionEventStepDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.interventionplan.InterventionTemplateStepDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisInterventionMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.interventionplan.InterventionEventMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.interventionplan.InterventionEventStepMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.interventionplan.InterventionTemplateStepMapper;
import cn.iocoder.yudao.module.psychology.enums.TimelineEventTypeEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.psychology.service.profile.StudentTimelineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.INTERVENTION_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS;

/**
 * 干预计划 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class InterventionPlanServiceImpl implements InterventionPlanService {

    @Resource
    private InterventionEventMapper interventionEventMapper;

    @Resource
    private InterventionEventStepMapper interventionEventStepMapper;

    @Resource
    private InterventionTemplateService interventionTemplateService;

    @Resource
    private InterventionTemplateStepMapper interventionTemplateStepMapper;

    @Resource
    private CrisisInterventionMapper crisisInterventionMapper;

    @Resource
    private StudentTimelineService studentTimelineService;

    @Resource
    private StudentProfileService studentProfileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInterventionPlan(InterventionPlanCreateReqVO createReqVO) {
        // 1. 验证学生档案是否存在
        validateStudentProfile(createReqVO.getStudentProfileId());

        // 2. 验证模板是否存在，并获取模板详情
        InterventionTemplateRespVO template = interventionTemplateService.getTemplateById(createReqVO.getTemplateId());
        if (template == null) {
            throw exception(INTERVENTION_TEMPLATE_NOT_EXISTS);
        }

        // 3. 查询该学生 status=5（持续关注）的最新危机干预记录
        List<Long> relativeEventIds = getLatestCrisisInterventionId(createReqVO.getStudentProfileId());

        // 4. 创建干预事件记录
        InterventionEventDO event = new InterventionEventDO();
        event.setInterventionId(generateInterventionId());
        event.setStudentProfileId(createReqVO.getStudentProfileId());
        event.setTitle(createReqVO.getTitle());
        event.setTemplateId(createReqVO.getTemplateId());
        event.setRelativeEventIds(relativeEventIds);
        event.setStatus(1); // 设置状态为1（进行中）
        interventionEventMapper.insert(event);

        log.info("[createInterventionPlan] 创建干预事件成功，eventId: {}, interventionId: {}, studentProfileId: {}",
                event.getId(), event.getInterventionId(), createReqVO.getStudentProfileId());

        // 5. 根据模板步骤创建干预步骤
        List<InterventionTemplateStepDO> templateSteps = interventionTemplateStepMapper.selectListByTemplateId(createReqVO.getTemplateId());
        if (templateSteps != null && !templateSteps.isEmpty()) {
            List<InterventionEventStepDO> steps = new ArrayList<>();
            for (InterventionTemplateStepDO templateStep : templateSteps) {
                InterventionEventStepDO step = new InterventionEventStepDO();
                step.setInterventionId(event.getId()); // 注意：这里是 event.getId()，不是 interventionId
                step.setTemplateId(createReqVO.getTemplateId());
                step.setTitle(templateStep.getTitle());
                step.setSort(templateStep.getSort());
                step.setStatus(1); // 状态设为1（未开始）
                step.setNotes(null);
                step.setAttachmentIds(null);
                steps.add(step);
            }

            // 批量插入步骤
            for (InterventionEventStepDO step : steps) {
                interventionEventStepMapper.insert(step);
            }

            log.info("[createInterventionPlan] 创建干预步骤成功，eventId: {}, stepCount: {}",
                    event.getId(), steps.size());
        }

        // 6. 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("templateId", createReqVO.getTemplateId());
        meta.put("interventionId", event.getInterventionId());
        meta.put("stepCount", templateSteps != null ? templateSteps.size() : 0);

        studentTimelineService.saveTimelineWithMeta(
                createReqVO.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "创建危机干预计划(" + event.getInterventionId() + ")",
                event.getId().toString(),
                "创建危机干预计划(" + event.getInterventionId() + ")",
                meta
        );

        log.info("[createInterventionPlan] 创建干预计划成功，eventId: {}, studentProfileId: {}, templateId: {}",
                event.getId(), createReqVO.getStudentProfileId(), createReqVO.getTemplateId());

        return event.getId();
    }

    /**
     * 验证学生档案是否存在
     *
     * @param studentProfileId 学生档案ID
     */
    private void validateStudentProfile(Long studentProfileId) {
        if (studentProfileService.getStudentProfile(studentProfileId) == null) {
            throw exception(STUDENT_PROFILE_NOT_EXISTS);
        }
    }

    /**
     * 查询该学生 status=5（持续关注）的最新危机干预记录ID
     *
     * @param studentProfileId 学生档案ID
     * @return 关联事件ID列表（如果存在返回包含最新记录ID的列表，否则返回空列表）
     */
    private List<Long> getLatestCrisisInterventionId(Long studentProfileId) {
        LambdaQueryWrapper<CrisisInterventionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CrisisInterventionDO::getStudentProfileId, studentProfileId)
                .eq(CrisisInterventionDO::getStatus, 5) // status=5 表示持续关注
                .orderByDesc(CrisisInterventionDO::getCreateTime)
                .last("LIMIT 1");

        CrisisInterventionDO latestCrisis = crisisInterventionMapper.selectOne(wrapper);

        if (latestCrisis != null) {
            log.info("[getLatestCrisisInterventionId] 找到最新的持续关注危机干预记录，studentProfileId: {}, crisisId: {}",
                    studentProfileId, latestCrisis.getId());
            return Collections.singletonList(latestCrisis.getId());
        } else {
            log.info("[getLatestCrisisInterventionId] 未找到持续关注的危机干预记录，studentProfileId: {}", studentProfileId);
            return new ArrayList<>();
        }
    }

    /**
     * 生成干预编号
     * 格式: IV + yyyyMMdd + 6位随机数
     * 示例: IV20250318123456
     *
     * @return 干预编号
     */
    private String generateInterventionId() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = RandomUtil.randomNumbers(6);
        return "IV" + dateStr + randomStr;
    }

}
