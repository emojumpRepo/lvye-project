package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 测评任务 Service 接口
 */
public interface AssessmentTaskService {

    /**
     * 创建测评任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAssessmentTask(@Valid AssessmentTaskSaveReqVO createReqVO);

    /**
     * 更新测评任务
     *
     * @param updateReqVO 更新信息
     */
    void updateAssessmentTask(@Valid AssessmentTaskSaveReqVO updateReqVO);

    /**
     * 删除测评任务
     *
     * @param id 编号
     */
    void deleteAssessmentTask(Long id);

    /**
     * 获得测评任务
     *
     * @param id 编号
     * @return 测评任务
     */
    AssessmentTaskDO getAssessmentTask(Long id);

    /**
     * 获得测评任务分页
     *
     * @param pageReqVO 分页查询
     * @return 测评任务分页
     */
    PageResult<AssessmentTaskDO> getAssessmentTaskPage(AssessmentTaskPageReqVO pageReqVO);

    /**
     * 发布测评任务
     *
     * @param id 任务编号
     */
    void publishAssessmentTask(Long id);

    /**
     * 关闭测评任务
     *
     * @param id 任务编号
     */
    void closeAssessmentTask(Long id);

    /**
     * 添加参与者
     *
     * @param taskId 任务编号
     * @param studentProfileIds 学生档案编号列表
     */
    void addParticipants(Long taskId, List<Long> studentProfileIds);

    /**
     * 移除参与者
     *
     * @param taskId 任务编号
     * @param studentProfileIds 学生档案编号列表
     */
    void removeParticipants(Long taskId, List<Long> studentProfileIds);

    /**
     * 获取任务统计信息
     *
     * @param taskId 任务编号
     * @return 统计信息
     */
    AssessmentTaskStatisticsRespVO getTaskStatistics(Long taskId);

    /**
     * 根据任务编号获取任务
     *
     * @param taskNo 任务编号
     * @return 测评任务
     */
    AssessmentTaskDO getAssessmentTaskByNo(String taskNo);

}