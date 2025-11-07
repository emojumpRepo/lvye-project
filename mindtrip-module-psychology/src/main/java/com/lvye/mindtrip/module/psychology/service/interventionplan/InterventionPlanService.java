package com.lvye.mindtrip.module.psychology.service.interventionplan;

import com.lvye.mindtrip.framework.common.pojo.PageParam;
import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepBatchUpdateSortReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepCreateReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionEventStepUpdateReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionPlanCreateReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionPlanOngoingRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.interventionplan.vo.InterventionPlanRespVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 干预计划 Service 接口
 *
 * @author 芋道源码
 */
public interface InterventionPlanService {

    /**
     * 创建干预计划
     *
     * @param createReqVO 创建请求 VO
     * @return 干预事件ID
     */
    Long createInterventionPlan(@Valid InterventionPlanCreateReqVO createReqVO);

    /**
     * 获取干预计划详情
     *
     * @param id 干预计划ID
     * @return 干预计划详情
     */
    InterventionPlanRespVO getInterventionPlan(Long id);

    /**
     * 更新干预事件标题
     *
     * @param id 干预事件ID
     * @param title 新标题
     */
    void updateEventTitle(Long id, String title);

    /**
     * 移除干预计划的关联事件ID
     *
     * @param id 干预事件ID
     * @param relativeEventId 要移除的关联事件ID
     */
    void removeRelativeEvent(Long id, Long relativeEventId);

    /**
     * 更新干预计划的关联事件ID列表
     *
     * @param id 干预事件ID
     * @param relativeEventIds 关联事件ID列表
     */
    void updateRelativeEvents(Long id, List<Long> relativeEventIds);

    /**
     * 新增干预事件步骤
     *
     * @param createReqVO 新增请求VO
     * @return 步骤ID
     */
    Long createEventStep(@Valid InterventionEventStepCreateReqVO createReqVO);

    /**
     * 更新干预事件步骤
     *
     * @param updateReqVO 更新请求VO
     */
    void updateEventStep(@Valid InterventionEventStepUpdateReqVO updateReqVO);

    /**
     * 批量更新干预事件步骤排序
     *
     * @param interventionId 干预事件ID
     * @param stepSortItems 步骤排序列表
     */
    void batchUpdateStepSort(Long interventionId, List<InterventionEventStepBatchUpdateSortReqVO.StepSortItem> stepSortItems);

    /**
     * 完成干预事件
     *
     * @param id 干预事件ID
     */
    void completeInterventionEvent(Long id);

    /**
     * 根据学生档案ID查询干预事件列表
     *
     * @param studentProfileId 学生档案ID
     * @return 干预事件列表
     */
    List<InterventionPlanRespVO> getInterventionEventsByStudentProfileId(Long studentProfileId);

    /**
     * 分页查询正在进行的干预计划列表
     *
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<InterventionPlanOngoingRespVO> getOngoingInterventionPlanPage(PageParam pageParam);

}
