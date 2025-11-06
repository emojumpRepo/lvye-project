package cn.iocoder.yudao.module.psychology.service.interventionplan;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepBatchUpdateSortReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepCreateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepUpdateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanCreateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanOngoingRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.RelativeCrisisEventVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisInterventionDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.interventionplan.InterventionEventDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.interventionplan.InterventionEventStepDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.interventionplan.InterventionTemplateStepDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisInterventionMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.interventionplan.InterventionEventMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.interventionplan.InterventionEventStepMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.interventionplan.InterventionTemplateStepMapper;
import cn.iocoder.yudao.module.psychology.enums.DictTypeConstants;
import cn.iocoder.yudao.module.psychology.enums.TimelineEventTypeEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentProfileService;
import cn.iocoder.yudao.module.psychology.service.profile.StudentTimelineService;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.INTERVENTION_EVENT_ALREADY_COMPLETED;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.INTERVENTION_EVENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.INTERVENTION_EVENT_STEP_NOT_EXISTS;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.INTERVENTION_EVENT_STEPS_NOT_COMPLETED;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.INTERVENTION_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants.STUDENT_HAS_ONGOING_INTERVENTION;
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

    @Resource
    private AdminUserService adminUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInterventionPlan(InterventionPlanCreateReqVO createReqVO) {
        // 1. 验证学生档案是否存在
        validateStudentProfile(createReqVO.getStudentProfileId());

        // 2. 检查该学生是否有正在进行的干预事件（status = 1）
        checkOngoingIntervention(createReqVO.getStudentProfileId());

        // 3. 验证模板是否存在，并获取模板详情
        InterventionTemplateRespVO template = interventionTemplateService.getTemplateById(createReqVO.getTemplateId());
        if (template == null) {
            throw exception(INTERVENTION_TEMPLATE_NOT_EXISTS);
        }

        // 4. 查询该学生 status=5（持续关注）的最新危机干预记录
        List<Long> relativeEventIds = getLatestCrisisInterventionId(createReqVO.getStudentProfileId());

        // 5. 创建干预事件记录
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

        // 6. 根据模板步骤创建干预步骤
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

        // 7. 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("templateId", createReqVO.getTemplateId());
        meta.put("interventionId", event.getInterventionId());
        meta.put("stepCount", templateSteps != null ? templateSteps.size() : 0);
        meta.put("action", "create");
        meta.put("description", "创建危机干预计划(" + event.getInterventionId() + ")");

        studentTimelineService.saveTimelineWithMeta(
                createReqVO.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "创建危机干预计划(" + event.getInterventionId() + ")",
                "intervention_plan_" + event.getId(),
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
     * 检查该学生是否有正在进行的干预事件
     *
     * @param studentProfileId 学生档案ID
     */
    private void checkOngoingIntervention(Long studentProfileId) {
        LambdaQueryWrapper<InterventionEventDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterventionEventDO::getStudentProfileId, studentProfileId)
                .eq(InterventionEventDO::getStatus, 1); // status=1 表示正在进行

        List<InterventionEventDO> ongoingEvents = interventionEventMapper.selectList(wrapper);

        if (ongoingEvents != null && !ongoingEvents.isEmpty()) {
            log.warn("[checkOngoingIntervention] 学生已有正在进行的干预事件，studentProfileId: {}, eventCount: {}",
                    studentProfileId, ongoingEvents.size());
            throw exception(STUDENT_HAS_ONGOING_INTERVENTION);
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
     *
     * @return 干预编号
     */
    private String generateInterventionId() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = RandomUtil.randomNumbers(4);
        return "IV_" + dateStr + "_" + randomStr;
    }

    /**
     * 获取步骤状态名称
     *
     * @param status 状态值
     * @return 状态名称
     */
    private String getStepStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待处理";
            case 2:
                return "进行中";
            case 3:
                return "已完成";
            default:
                return "未知";
        }
    }

    /**
     * 记录步骤标题变化到时间线
     */
    private void recordTitleChange(InterventionEventDO event, InterventionEventStepDO oldStep, String newTitle) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("stepId", oldStep.getId());
        meta.put("oldTitle", oldStep.getTitle());
        meta.put("newTitle", newTitle);
        meta.put("interventionId", event.getInterventionId());
        meta.put("action", "update");
        meta.put("description", "修改步骤「" + oldStep.getTitle() + "」的标题为「" + newTitle + "」");

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "修改干预计划(" + event.getInterventionId() + ")的步骤标题",
                "intervention_plan_" + event.getId(),
                "步骤标题从「" + oldStep.getTitle() + "」修改为「" + newTitle + "」",
                meta
        );
    }

    /**
     * 记录步骤状态变化到时间线
     */
    private void recordStatusChange(InterventionEventDO event, InterventionEventStepDO oldStep, Integer newStatus) {
        String oldStatusName = getStepStatusName(oldStep.getStatus());
        String newStatusName = getStepStatusName(newStatus);

        Map<String, Object> meta = new HashMap<>();
        meta.put("stepId", oldStep.getId());
        meta.put("stepTitle", oldStep.getTitle());
        meta.put("oldStatus", oldStep.getStatus());
        meta.put("newStatus", newStatus);
        meta.put("oldStatusName", oldStatusName);
        meta.put("newStatusName", newStatusName);
        meta.put("interventionId", event.getInterventionId());
        meta.put("action", "update");
        meta.put("description", "修改步骤「" + oldStep.getTitle() + "」的状态从「" + oldStatusName + "」变更为「" + newStatusName + "」");

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "更新干预计划(" + event.getInterventionId() + ")的步骤状态",
                "intervention_plan_" + event.getId(),
                "步骤「" + oldStep.getTitle() + "」状态从「" + oldStatusName + "」变更为「" + newStatusName + "」",
                meta
        );
    }

    /**
     * 记录步骤笔记变化到时间线
     */
    private void recordNotesChange(InterventionEventDO event, InterventionEventStepDO oldStep) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("stepId", oldStep.getId());
        meta.put("stepTitle", oldStep.getTitle());
        meta.put("interventionId", event.getInterventionId());
        meta.put("action", "write");
        meta.put("description", "更新了步骤「" + oldStep.getTitle() + "」的教师笔记/详情方案");

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "更新干预计划(" + event.getInterventionId() + ")的步骤笔记",
                "intervention_plan_" + event.getId(),
                "更新了步骤「" + oldStep.getTitle() + "」的教师笔记/详情方案",
                meta
        );
    }

    /**
     * 记录步骤附件变化到时间线
     */
    private void recordAttachmentsChange(InterventionEventDO event, InterventionEventStepDO oldStep, List<Long> newAttachmentIds) {
        int newCount = newAttachmentIds != null ? newAttachmentIds.size() : 0;

        Map<String, Object> meta = new HashMap<>();
        meta.put("stepId", oldStep.getId());
        meta.put("stepTitle", oldStep.getTitle());
        meta.put("attachmentCount", newCount);
        meta.put("interventionId", event.getInterventionId());
        meta.put("action", "upload");
        meta.put("description", "更新了步骤「" + oldStep.getTitle() + "」的附件列表(共" + newCount + "个)");

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "更新干预计划(" + event.getInterventionId() + ")的步骤附件",
                "intervention_plan_" + event.getId(),
                "更新了步骤「" + oldStep.getTitle() + "」的附件列表(共" + newCount + "个)",
                meta
        );
    }

    @Override
    public InterventionPlanRespVO getInterventionPlan(Long id) {
        // 1. 查询干预事件
        InterventionEventDO event = interventionEventMapper.selectById(id);
        if (event == null) {
            throw exception(INTERVENTION_EVENT_NOT_EXISTS);
        }

        // 2. 查询步骤列表
        List<InterventionEventStepDO> steps = interventionEventStepMapper.selectListByInterventionId(id);

        // 3. 转换为 VO
        InterventionPlanRespVO respVO = BeanUtils.toBean(event, InterventionPlanRespVO.class);

        // 3.1. 获取创建者名字
        if (event.getCreator() != null) {
            try {
                Long creatorId = Long.valueOf(event.getCreator());
                AdminUserDO creator = adminUserService.getUser(creatorId);
                if (creator != null) {
                    respVO.setCreatorName(creator.getNickname());
                }
            } catch (NumberFormatException e) {
                log.warn("[getInterventionPlan] 创建者ID格式错误，eventId: {}, creator: {}",
                        event.getId(), event.getCreator());
            }
        }

        // 3.2. 获取学生信息
        if (event.getStudentProfileId() != null) {
            StudentProfileVO studentProfile = studentProfileService.getStudentProfile(event.getStudentProfileId());
            if (studentProfile != null) {
                respVO.setStudentNo(studentProfile.getStudentNo());
                respVO.setStudentName(studentProfile.getName());
                respVO.setClassName(studentProfile.getClassName());
            }
        }

        // 4. 转换步骤列表并排序
        List<InterventionEventStepRespVO> stepVOs = steps.stream()
                .sorted(Comparator.comparing(InterventionEventStepDO::getSort))
                .map(step -> BeanUtils.toBean(step, InterventionEventStepRespVO.class))
                .collect(Collectors.toList());
        respVO.setSteps(stepVOs);

        // 5. 查询关联的危机干预事件详情（包含 id、eventId 和 sourceType）
        if (event.getRelativeEventIds() != null && !event.getRelativeEventIds().isEmpty()) {
            List<RelativeCrisisEventVO> relativeEvents = event.getRelativeEventIds().stream()
                    .map(crisisId -> {
                        CrisisInterventionDO crisis = crisisInterventionMapper.selectById(crisisId);
                        if (crisis != null) {
                            return new RelativeCrisisEventVO(crisis.getId(), crisis.getEventId(), crisis.getSourceType());
                        }
                        return null;
                    })
                    .filter(vo -> vo != null)
                    .collect(Collectors.toList());
            respVO.setRelativeEvents(relativeEvents);
        } else {
            // 如果没有关联事件，返回空数组
            respVO.setRelativeEvents(new ArrayList<>());
        }

        return respVO;
    }

    @Override
    public void updateEventTitle(Long id, String title) {
        // 1. 校验干预事件是否存在
        InterventionEventDO event = interventionEventMapper.selectById(id);
        if (event == null) {
            throw exception(INTERVENTION_EVENT_NOT_EXISTS);
        }

        // 2. 保存旧标题,用于时间线记录
        String oldTitle = event.getTitle();

        // 3. 更新标题字段
        InterventionEventDO updateObj = new InterventionEventDO();
        updateObj.setId(id);
        updateObj.setTitle(title);
        interventionEventMapper.updateById(updateObj);

        // 4. 记录时间线
        Map<String, Object> meta = new HashMap<>();
        meta.put("eventId", event.getId());
        meta.put("interventionId", event.getInterventionId());
        meta.put("oldTitle", oldTitle);
        meta.put("newTitle", title);
        meta.put("action", "update");
        meta.put("description", "更新干预事件标题从「" + oldTitle + "」变更为「" + title + "」");

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "更新危机干预计划(" + event.getInterventionId() + ")的标题",
                "intervention_plan_" + event.getId(),
                "更新干预事件标题从「" + oldTitle + "」变更为「" + title + "」",
                meta
        );

        log.info("[updateEventTitle] 更新干预事件标题成功，eventId: {}, 旧标题: {}, 新标题: {}", id, oldTitle, title);
    }

    @Override
    public void removeRelativeEvent(Long id, Long relativeEventId) {
        // 1. 校验干预事件是否存在
        InterventionEventDO event = interventionEventMapper.selectById(id);
        if (event == null) {
            throw exception(INTERVENTION_EVENT_NOT_EXISTS);
        }

        // 2. 获取当前的关联事件ID列表
        List<Long> relativeEventIds = event.getRelativeEventIds();
        if (relativeEventIds == null || relativeEventIds.isEmpty()) {
            log.warn("[removeRelativeEvent] 干预事件没有关联事件，eventId: {}", id);
            return;
        }

        // 3. 检查要移除的ID是否存在
        if (!relativeEventIds.contains(relativeEventId)) {
            log.warn("[removeRelativeEvent] 关联事件ID不存在于列表中，eventId: {}, relativeEventId: {}", id, relativeEventId);
            return;
        }

        // 4. 创建新的列表并移除指定的ID（避免不可变列表问题）
        List<Long> newRelativeEventIds = new ArrayList<>(relativeEventIds);
        newRelativeEventIds.remove(relativeEventId);

        // 5. 更新数据库
        InterventionEventDO updateObj = new InterventionEventDO();
        updateObj.setId(id);
        updateObj.setRelativeEventIds(newRelativeEventIds);
        interventionEventMapper.updateById(updateObj);

        // 6. 查询危机干预事件详情，获取事件类型和eventId
        CrisisInterventionDO crisis = crisisInterventionMapper.selectById(relativeEventId);
        String eventTypeLabel = "未知类型";
        String eventId = String.valueOf(relativeEventId);

        if (crisis != null) {
            // 获取事件类型字典标签
            String dictLabel = DictFrameworkUtils.parseDictDataLabel(
                DictTypeConstants.CRISIS_SOURCE_TYPE,
                crisis.getSourceType()
            );
            if (dictLabel != null && !dictLabel.isEmpty()) {
                eventTypeLabel = dictLabel;
            }
            eventId = crisis.getEventId();
        }

        // 构建时间线展示内容
        String displayText = String.format("移除关联事件：%s(%s)", eventTypeLabel, eventId);

        // 7. 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("removedEventId", relativeEventId);
        meta.put("interventionId", event.getInterventionId());
        meta.put("action", "updateRelativeEvents");
        meta.put("eventType", eventTypeLabel);
        meta.put("eventId", eventId);
        meta.put("description", displayText);

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "从危机干预计划(" + event.getInterventionId() + ")中移除关联事件",
                "intervention_plan_" + event.getId(),
                displayText,
                meta
        );

        log.info("[removeRelativeEvent] 移除关联事件成功，eventId: {}, relativeEventId: {}", id, relativeEventId);
    }

    @Override
    public void updateRelativeEvents(Long id, List<Long> relativeEventIds) {
        // 1. 校验干预事件是否存在
        InterventionEventDO event = interventionEventMapper.selectById(id);
        if (event == null) {
            throw exception(INTERVENTION_EVENT_NOT_EXISTS);
        }

        // 2. 更新关联事件ID列表
        InterventionEventDO updateObj = new InterventionEventDO();
        updateObj.setId(id);
        updateObj.setRelativeEventIds(relativeEventIds);
        interventionEventMapper.updateById(updateObj);

        // 3. 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("relativeEventIds", relativeEventIds);
        meta.put("interventionId", event.getInterventionId());
        meta.put("eventCount", relativeEventIds.size());
        meta.put("action", "updateRelativeEvents");
        meta.put("description", "更新关联事件列表(共" + relativeEventIds.size() + "个)");

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "更新危机干预计划(" + event.getInterventionId() + ")的关联事件列表",
                "intervention_plan_" + event.getId(),
                "更新关联事件列表(共" + relativeEventIds.size() + "个)",
                meta
        );

        log.info("[updateRelativeEvents] 更新关联事件列表成功，eventId: {}, relativeEventIds: {}", id, relativeEventIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEventStep(InterventionEventStepCreateReqVO createReqVO) {
        // 1. 校验干预事件是否存在
        InterventionEventDO event = interventionEventMapper.selectById(createReqVO.getInterventionId());
        if (event == null) {
            throw exception(INTERVENTION_EVENT_NOT_EXISTS);
        }

        // 2. 如果未指定排序值，自动计算为当前最大值+1
        Integer sort = createReqVO.getSort();
        if (sort == null) {
            List<InterventionEventStepDO> existingSteps =
                interventionEventStepMapper.selectListByInterventionId(createReqVO.getInterventionId());
            sort = existingSteps.stream()
                .map(InterventionEventStepDO::getSort)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        }

        // 3. 构建步骤对象
        InterventionEventStepDO step = new InterventionEventStepDO();
        step.setInterventionId(createReqVO.getInterventionId());
        step.setTemplateId(event.getTemplateId()); // 继承干预事件的模板ID
        step.setTitle(createReqVO.getTitle());
        step.setSort(sort);
        step.setStatus(createReqVO.getStatus() != null ? createReqVO.getStatus() : 1); // 使用传入的status，如未传入则默认为1（待处理）
        step.setNotes(createReqVO.getNotes());
        step.setAttachmentIds(createReqVO.getAttachmentIds());

        // 4. 插入数据库
        interventionEventStepMapper.insert(step);

        // 5. 记录时间线
        Map<String, Object> meta = new HashMap<>();
        meta.put("interventionId", event.getInterventionId());
        meta.put("stepId", step.getId());
        meta.put("stepTitle", createReqVO.getTitle());
        meta.put("sort", sort);
        meta.put("action", "addStep");
        meta.put("description", "新增干预步骤「" + createReqVO.getTitle() + "」");

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "为干预计划(" + event.getInterventionId() + ")新增步骤",
                "intervention_plan_" + event.getId(),
                "新增干预步骤：" + createReqVO.getTitle(),
                meta
        );

        log.info("[createEventStep] 新增干预事件步骤成功，interventionId={}, stepId={}, title={}, sort={}",
            createReqVO.getInterventionId(), step.getId(), createReqVO.getTitle(), sort);

        return step.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEventStep(InterventionEventStepUpdateReqVO updateReqVO) {
        // 1. 校验步骤是否存在，并保存旧值
        InterventionEventStepDO oldStep = interventionEventStepMapper.selectById(updateReqVO.getId());
        if (oldStep == null) {
            throw exception(INTERVENTION_EVENT_STEP_NOT_EXISTS);
        }

        // 2. 查询干预事件信息（用于时间线记录）
        InterventionEventDO event = interventionEventMapper.selectById(oldStep.getInterventionId());
        if (event == null) {
            throw exception(INTERVENTION_EVENT_NOT_EXISTS);
        }

        // 3. 构建更新对象，只更新非空字段
        InterventionEventStepDO updateObj = new InterventionEventStepDO();
        updateObj.setId(updateReqVO.getId());

        boolean hasChanges = false;

        // 4. 检查并记录 title 变化
        if (updateReqVO.getTitle() != null && !updateReqVO.getTitle().equals(oldStep.getTitle())) {
            updateObj.setTitle(updateReqVO.getTitle());
            recordTitleChange(event, oldStep, updateReqVO.getTitle());
            hasChanges = true;
        }

        // 5. 检查并记录 status 变化
        if (updateReqVO.getStatus() != null && !updateReqVO.getStatus().equals(oldStep.getStatus())) {
            updateObj.setStatus(updateReqVO.getStatus());
            recordStatusChange(event, oldStep, updateReqVO.getStatus());
            hasChanges = true;
        }

        // 6. 检查并记录 notes 变化
        if (updateReqVO.getNotes() != null && !updateReqVO.getNotes().equals(oldStep.getNotes())) {
            updateObj.setNotes(updateReqVO.getNotes());
            recordNotesChange(event, oldStep);
            hasChanges = true;
        }

        // 7. 检查并记录 attachmentIds 变化
        if (updateReqVO.getAttachmentIds() != null && !updateReqVO.getAttachmentIds().equals(oldStep.getAttachmentIds())) {
            updateObj.setAttachmentIds(updateReqVO.getAttachmentIds());
            recordAttachmentsChange(event, oldStep, updateReqVO.getAttachmentIds());
            hasChanges = true;
        }

        // 8. 如果有字段变化，执行更新
        if (hasChanges) {
            interventionEventStepMapper.updateById(updateObj);
            log.info("[updateEventStep] 更新干预事件步骤成功，stepId: {}, changes: title={}, status={}, notes={}, attachments={}",
                    updateReqVO.getId(),
                    updateReqVO.getTitle() != null && !updateReqVO.getTitle().equals(oldStep.getTitle()),
                    updateReqVO.getStatus() != null && !updateReqVO.getStatus().equals(oldStep.getStatus()),
                    updateReqVO.getNotes() != null && !updateReqVO.getNotes().equals(oldStep.getNotes()),
                    updateReqVO.getAttachmentIds() != null && !updateReqVO.getAttachmentIds().equals(oldStep.getAttachmentIds()));
        } else {
            log.info("[updateEventStep] 干预事件步骤无变化，stepId: {}", updateReqVO.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStepSort(Long interventionId, List<InterventionEventStepBatchUpdateSortReqVO.StepSortItem> stepSortItems) {
        // 1. 校验干预事件是否存在
        InterventionEventDO event = interventionEventMapper.selectById(interventionId);
        if (event == null) {
            throw exception(INTERVENTION_EVENT_NOT_EXISTS);
        }

        // 2. 查询该干预事件的所有现有步骤
        List<InterventionEventStepDO> existingSteps = interventionEventStepMapper.selectListByInterventionId(interventionId);

        // 3. 构建现有步骤的 id -> step 映射，用于后续查找步骤详情
        Map<Long, InterventionEventStepDO> existingStepMap = existingSteps.stream()
                .collect(Collectors.toMap(InterventionEventStepDO::getId, step -> step));

        // 4. 找出排序发生变化的步骤，并构建更新列表
        List<InterventionEventStepDO> updateList = new ArrayList<>();
        List<Map<String, Object>> changedStepsInfo = new ArrayList<>();

        for (InterventionEventStepBatchUpdateSortReqVO.StepSortItem item : stepSortItems) {
            InterventionEventStepDO existingStep = existingStepMap.get(item.getId());

            // 如果步骤存在且排序值发生变化
            if (existingStep != null && !existingStep.getSort().equals(item.getSort())) {
                // 添加到更新列表
                InterventionEventStepDO updateObj = new InterventionEventStepDO();
                updateObj.setId(item.getId());
                updateObj.setSort(item.getSort());
                updateList.add(updateObj);

                // 记录变化信息，用于时间线
                Map<String, Object> changeInfo = new HashMap<>();
                changeInfo.put("stepId", item.getId());
                changeInfo.put("stepTitle", existingStep.getTitle());
                changeInfo.put("oldSort", existingStep.getSort());
                changeInfo.put("newSort", item.getSort());
                changedStepsInfo.add(changeInfo);
            }
        }

        // 5. 如果没有任何步骤排序发生变化，直接返回
        if (updateList.isEmpty()) {
            log.info("[batchUpdateStepSort] 没有步骤排序发生变化，interventionId: {}", interventionId);
            return;
        }

        // 6. 批量更新排序
        interventionEventStepMapper.updateBatch(updateList);

        // 7. 为每个发生变化的步骤添加时间线记录
        for (Map<String, Object> changeInfo : changedStepsInfo) {
            String stepTitle = (String) changeInfo.get("stepTitle");
            Integer oldSort = (Integer) changeInfo.get("oldSort");
            Integer newSort = (Integer) changeInfo.get("newSort");

            Map<String, Object> meta = new HashMap<>();
            meta.put("stepId", changeInfo.get("stepId"));
            meta.put("oldSort", oldSort);
            meta.put("newSort", newSort);
            meta.put("interventionId", event.getInterventionId());
            meta.put("action", "updateSort");
            meta.put("description", "调整步骤「" + stepTitle + "」的顺序(从第" + oldSort + "位调整为第" + newSort + "位)");

            studentTimelineService.saveTimelineWithMeta(
                    event.getStudentProfileId(),
                    TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                    "调整危机干预计划(" + event.getInterventionId() + ")的步骤顺序",
                    "intervention_plan_" + event.getId(),
                    "调整步骤「" + stepTitle + "」的顺序(从第" + oldSort + "位调整为第" + newSort + "位)",
                    meta
            );
        }

        log.info("[batchUpdateStepSort] 批量更新步骤排序成功，interventionId: {}, 更新步骤数: {}",
                interventionId, updateList.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeInterventionEvent(Long id) {
        // 1. 校验干预事件是否存在
        InterventionEventDO event = interventionEventMapper.selectById(id);
        if (event == null) {
            throw exception(INTERVENTION_EVENT_NOT_EXISTS);
        }

        // 2. 检查干预事件是否已完成
        if (event.getStatus() != null && event.getStatus().equals(2)) {
            throw exception(INTERVENTION_EVENT_ALREADY_COMPLETED);
        }

        // 3. 查询该干预事件的所有步骤
        List<InterventionEventStepDO> steps = interventionEventStepMapper.selectListByInterventionId(id);

        // 4. 检查所有步骤是否完成（status=3）
        boolean allCompleted = steps.stream()
                .allMatch(step -> step.getStatus() != null && step.getStatus().equals(3));

        if (!allCompleted) {
            throw exception(INTERVENTION_EVENT_STEPS_NOT_COMPLETED);
        }

        // 5. 更新干预事件状态为2（已完成）
        InterventionEventDO updateObj = new InterventionEventDO();
        updateObj.setId(id);
        updateObj.setStatus(2);
        interventionEventMapper.updateById(updateObj);

        // 6. 添加时间线记录
        Map<String, Object> meta = new HashMap<>();
        meta.put("interventionId", event.getInterventionId());
        meta.put("oldStatus", 1);
        meta.put("newStatus", 2);
        meta.put("action", "complete");
        meta.put("description", "干预计划(" + event.getInterventionId() + ")已完成");
        meta.put("totalSteps", steps.size());

        studentTimelineService.saveTimelineWithMeta(
                event.getStudentProfileId(),
                TimelineEventTypeEnum.CRISIS_INTERVENTION_PLAN.getType(),
                "干预计划(" + event.getInterventionId() + ")已完成",
                "intervention_plan_" + event.getId(),
                "所有干预步骤已完成，干预计划状态更新为已完成",
                meta
        );

        log.info("[completeInterventionEvent] 完成干预事件成功，eventId: {}, interventionId: {}, totalSteps: {}",
                id, event.getInterventionId(), steps.size());
    }

    @Override
    public List<InterventionPlanRespVO> getInterventionEventsByStudentProfileId(Long studentProfileId) {
        // 1. 验证学生档案是否存在
        validateStudentProfile(studentProfileId);

        // 2. 根据学生档案ID查询干预事件列表
        List<InterventionEventDO> events = interventionEventMapper.selectListByStudentId(studentProfileId);

        // 3. 转换为响应VO
        List<InterventionPlanRespVO> result = new ArrayList<>();
        if (events != null && !events.isEmpty()) {
            for (InterventionEventDO event : events) {
                InterventionPlanRespVO respVO = BeanUtils.toBean(event, InterventionPlanRespVO.class);
                
                // 获取创建者名字
                if (event.getCreator() != null) {
                    try {
                        Long creatorId = Long.valueOf(event.getCreator());
                        AdminUserDO creator = adminUserService.getUser(creatorId);
                        if (creator != null) {
                            respVO.setCreatorName(creator.getNickname());
                        }
                    } catch (NumberFormatException e) {
                        log.warn("[getInterventionEventsByStudentProfileId] 创建者ID格式错误，eventId: {}, creator: {}",
                                event.getId(), event.getCreator());
                    }
                }
                
                // 查询关联事件详情
                if (event.getRelativeEventIds() != null && !event.getRelativeEventIds().isEmpty()) {
                    List<RelativeCrisisEventVO> relativeEvents = new ArrayList<>();
                    for (Long eventId : event.getRelativeEventIds()) {
                        CrisisInterventionDO crisis = crisisInterventionMapper.selectById(eventId);
                        if (crisis != null) {
                            RelativeCrisisEventVO relativeEvent = new RelativeCrisisEventVO();
                            relativeEvent.setId(crisis.getId());
                            relativeEvent.setEventId(crisis.getEventId());
                            relativeEvent.setSourceType(crisis.getSourceType());
                            relativeEvents.add(relativeEvent);
                        }
                    }
                    respVO.setRelativeEvents(relativeEvents);
                } else {
                    // 如果没有关联事件，返回空数组
                    respVO.setRelativeEvents(new ArrayList<>());
                }
                
                // 查询干预步骤
                List<InterventionEventStepDO> steps = interventionEventStepMapper.selectListByInterventionId(event.getId());
                if (steps != null && !steps.isEmpty()) {
                    // 按照排序字段排序
                    List<InterventionEventStepDO> sortedSteps = steps.stream()
                            .sorted(Comparator.comparing(InterventionEventStepDO::getSort))
                            .collect(Collectors.toList());
                    
                    // 转换为响应VO
                    List<InterventionEventStepRespVO> stepRespVOs = BeanUtils.toBean(sortedSteps, InterventionEventStepRespVO.class);
                    respVO.setSteps(stepRespVOs);
                }
                
                result.add(respVO);
            }
        }
        
        log.info("[getInterventionEventsByStudentProfileId] 查询学生干预事件列表成功，studentProfileId: {}, count: {}",
                studentProfileId, result.size());

        return result;
    }

    @Override
    public PageResult<InterventionPlanOngoingRespVO> getOngoingInterventionPlanPage(PageParam pageParam) {
        // 1. 创建分页对象
        IPage<InterventionPlanOngoingRespVO> page = new Page<>(pageParam.getPageNo(), pageParam.getPageSize());

        // 2. 调用 Mapper 查询正在进行的干预计划
        interventionEventMapper.selectOngoingPage(page);

        // 3. 返回分页结果
        log.info("[getOngoingInterventionPlanPage] 查询正在进行的干预计划分页列表成功，pageNo: {}, pageSize: {}, total: {}",
                pageParam.getPageNo(), pageParam.getPageSize(), page.getTotal());

        return new PageResult<>(page.getRecords(), page.getTotal());
    }

}
