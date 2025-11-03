package cn.iocoder.yudao.module.psychology.service.interventionplan;

import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanCreateReqVO;
import jakarta.validation.Valid;

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

}
