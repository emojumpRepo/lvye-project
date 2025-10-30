package cn.iocoder.yudao.module.psychology.service.intervention;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 危机干预 Service 接口
 *
 * @author 芋道源码
 */
public interface CrisisInterventionService {

    /**
     * 获取五级干预看板统计数据（带分页和查询）
     *
     * @param reqVO 查询参数
     * @return 统计数据和学生列表
     */
    InterventionDashboardSummaryVO getDashboardSummaryWithPage(InterventionDashboardReqVO reqVO);

    /**
     * 获取五级干预看板统计数据
     *
     * @param classId 班级ID（可选）
     * @param counselorUserId 咨询师ID（可选）
     * @return 统计数据
     */
    InterventionDashboardSummaryVO getDashboardSummary(Long classId, Long counselorUserId);

    /**
     * 获取五级干预看板统计数据
     *
     * @param classId 班级ID（可选）
     * @param counselorUserId 咨询师ID（可选）
     * @param pageSize 每个等级返回的学生数量限制（可选，默认10）
     * @return 各等级统计数据列表
     */
    List<InterventionDashboardLevelVO> getDashboardLevels(Long classId, Long counselorUserId, Integer pageSize);

    /**
     * 获取指定干预等级的学生列表
     *
     * @param level 干预等级
     * @param pageReqVO 分页参数
     * @return 学生列表
     */
    PageResult<InterventionStudentRespVO> getStudentsByLevel(String level, InterventionStudentPageReqVO pageReqVO);

    /**
     * 根据风险等级获取学生列表
     *
     * @param riskLevel 风险等级（字典值）
     * @param classId 班级ID（可选）
     * @param counselorUserId 咨询师ID（可选）
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 学生分页列表
     */
    PageResult<InterventionStudentRespVO> getStudentsByRiskLevel(
        Integer riskLevel, Long classId, Long counselorUserId, Integer pageNo, Integer pageSize);

    /**
     * 调整学生的心理健康风险等级
     *
     * @param studentProfileId 学生档案ID
     * @param adjustReqVO 调整信息
     */
    void adjustStudentLevel(Long studentProfileId, StudentLevelAdjustReqVO adjustReqVO);

    /**
     * 创建危机事件
     *
     * @param createReqVO 创建信息
     * @return 创建结果（包含id、事件编号、标题）
     */
    CrisisEventCreateRespVO createCrisisEvent(@Valid CrisisEventCreateReqVO createReqVO);

    /**
     * 获取危机事件分页
     *
     * @param pageReqVO 分页查询
     * @return 事件分页
     */
    PageResult<CrisisEventRespVO> getCrisisEventPage(CrisisEventPageReqVO pageReqVO);

    /**
     * 获取危机事件统计（按 process_status 分组）
     *
     * @return 统计数据（type: 0-5）
     */
    List<CrisisEventProcessStatisticsVO.StatisticsItem> getCrisisEventStatistics();

    /**
     * 获取危机事件处理统计
     * 包含：干预处理方式统计、后续建议统计、危机事件状态统计
     *
     * @return 处理统计数据
     */
    CrisisEventProcessStatisticsVO getCrisisEventStatusStatistics();

    /**
     * 获取危机事件详情
     *
     * @param id 事件ID
     * @return 事件详情
     */
    CrisisEventRespVO getCrisisEvent(Long id);

    /**
     * 分配事件负责人
     *
     * @param id 事件ID
     * @param assignReqVO 分配信息
     */
    void assignHandler(Long id, CrisisEventAssignReqVO assignReqVO);

    /**
     * 更改事件负责人
     *
     * @param id 事件ID
     * @param reassignReqVO 重新分配信息
     */
    void reassignHandler(Long id, CrisisEventReassignReqVO reassignReqVO);

    /**
     * 选择处理方式
     *
     * @param id 事件ID
     * @param processReqVO 处理信息
     */
    void processCrisisEvent(Long id, CrisisEventProcessReqVO processReqVO);

    /**
     * 结案危机事件
     *
     * @param id 事件ID
     * @param closeReqVO 结案信息
     */
    void closeCrisisEvent(Long id, CrisisEventCloseReqVO closeReqVO);

    /**
     * 提交阶段性评估
     *
     * @param id 事件ID
     * @param assessmentReqVO 评估信息
     */
    void submitStageAssessment(Long id, CrisisEventAssessmentReqVO assessmentReqVO);

    /**
     * 获取事件处理历史
     *
     * @param id 事件ID
     * @return 处理历史
     */
    PageResult<CrisisEventProcessHistoryVO> getProcessHistory(Long id, Integer pageNo, Integer pageSize);

    /**
     * 检测重复上报
     *
     * @param studentProfileId 学生档案ID
     * @return 是否有重复事件
     */
    Boolean checkDuplicateEvent(Long studentProfileId);

    /**
     * 更新危机事件描述
     *
     * @param id 事件ID
     * @param description 事件描述
     */
    void updateCrisisEventDescription(Long id, String description);

    /**
     * 更新危机事件处理记录
     * 根据action字段值决定更新reason或content字段
     * REASSIGN_HANDLER和CHOOSE_PROCESS类型更新reason字段，其他类型更新content字段
     *
     * @param id 处理记录ID
     * @param content 更新内容
     */
    void updateProcessRecord(Long id, String content);

    /**
     * 切换危机事件关闭状态
     *
     * @param id 事件ID
     * @param closed 是否关闭
     */
    void toggleEventClosed(Long id, Boolean closed);

    /**
     * 获取危机事件分配设置
     */
    InterventionAssignmentSettingVO getAssignmentSettings();
    
    /**
     * 设置危机事件分配设置
     */
    void setAssignmentSettings(InterventionAssignmentSettingVO settingVO);

    /**
     * 获取学生的所有危机事件
     *
     * @param studentProfileId 学生档案ID
     * @return 危机事件列表
     */
    List<CrisisEventRespVO> getStudentCrisisEvents(Long studentProfileId);

    /**
     * 获取学生的所有评估记录
     *
     * @param studentProfileId 学生档案ID
     * @return 评估记录列表（按创建时间倒序）
     */
    List<CrisisEventRespVO.AssessmentRecordVO> getStudentAssessments(Long studentProfileId);

    /**
     * 获取正在进行的危机事件分页（status != 5）
     *
     * @param pageParam 分页参数
     * @return 事件分页
     */
    PageResult<CrisisEventRespVO> getOngoingCrisisEventPage(cn.iocoder.yudao.framework.common.pojo.PageParam pageParam);
}