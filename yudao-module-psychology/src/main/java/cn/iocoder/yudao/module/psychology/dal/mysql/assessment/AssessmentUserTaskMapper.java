package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentTaskDetailVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentTaskHisVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentUserTaskDO;
import cn.iocoder.yudao.module.psychology.enums.ParticipantCompletionStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-11
 * @Description:用户测评关联任务数据库组件
 * @Version: 1.0
 */
@Mapper
public interface AssessmentUserTaskMapper extends BaseMapperX<AssessmentUserTaskDO> {

    default AssessmentUserTaskDO selectByTaskNoAndUserId(String taskNo, Long userId) {
        return selectOne(new LambdaQueryWrapperX<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .eq(AssessmentUserTaskDO::getUserId, userId));
    }

    default void deleteByTaskNoAndUserId(String taskNo, Long userId) {
        delete(new LambdaQueryWrapperX<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .eq(AssessmentUserTaskDO::getUserId, userId));
    }

    default void deleteByTaskNo(String taskNo) {
        delete(new LambdaQueryWrapperX<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo));
    }

    /**
     * 查询正在进行的测评任务（未完成状态，包含未开始和进行中）
     * 
     * @param eventId 危机事件ID
     * @param userId 用户ID
     * @return 用户任务对象（按创建时间倒序取第一条）
     */
    AssessmentUserTaskDO selectOngoingTaskByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    /**
     * 查询危机事件关联的所有测评任务（所有状态）
     * 
     * @param eventId 危机事件ID
     * @param userId 用户ID
     * @return 测评任务列表
     */
    List<cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.CrisisEventRespVO.AssessmentTaskVO> selectAllTasksByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    default int updateStatusById(Long id, Integer status) {
        return update(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getId, id)
                .set(AssessmentUserTaskDO::getStatus, status)
                .set(AssessmentUserTaskDO::getStartTime, new Date()));
    }

    default int updateFinishStatusById(String taskNo) {
        return update(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .set(AssessmentUserTaskDO::getStatus, ParticipantCompletionStatusEnum.COMPLETED.getStatus())
                .set(AssessmentUserTaskDO::getSubmitTime, new Date()));
    }

    default int updateFinishTask(String taskNo, Long userId) {
        return update(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .eq(AssessmentUserTaskDO::getUserId, userId)
                .set(AssessmentUserTaskDO::getStatus, ParticipantCompletionStatusEnum.COMPLETED.getStatus())
                .set(AssessmentUserTaskDO::getSubmitTime, new Date()));
    }

    default int updateTaskRiskLevel(String taskNo, Long userId, Integer riskLevel, String evaluate, String suggestions) {
        return update(new LambdaUpdateWrapper<AssessmentUserTaskDO>()
                .eq(AssessmentUserTaskDO::getTaskNo, taskNo)
                .eq(AssessmentUserTaskDO::getUserId, userId)
                .set(AssessmentUserTaskDO::getRiskLevel, riskLevel)
                .set(AssessmentUserTaskDO::getEvaluate, evaluate)
                .set(AssessmentUserTaskDO::getSuggestions, suggestions)
                .set(AssessmentUserTaskDO::getSubmitTime, new Date()));
    }

    List<AssessmentTaskUserVO> selectListByTaskNo(@Param("taskNo") String taskNo);

    Long selectCountByTaskNo(@Param("taskNo") String taskNo);

    Long selectCountByTaskNoAndStatus(@Param("taskNo") String taskNo, @Param("status") Integer status);

    List<StudentAssessmentTaskHisVO> selectStudentAssessmentTaskList(@Param("studentProfileId") Long studentProfileId);

    StudentAssessmentTaskDetailVO selectStudentAssessmentTaskDetail(@Param("studentProfileId") Long studentProfileId, @Param("taskNo") String taskNo);

    IPage<QuestionnaireUserVO> selectQuestionnaireUserListByTaskNoAndQuestionnaire(IPage<QuestionnaireUserVO> page
            , @Param("pageReqVO") QuestionnaireUserPageVO pageReqVO);

    IPage<QuestionnaireUserVO> selectQuestionnaireUserListByTaskNo(IPage<QuestionnaireUserVO> page
            , @Param("pageReqVO") QuestionnaireUserPageVO pageReqVO);

    /**
     * 统计：按年级/班级聚合
     */
    List<cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentDeptAggregationRow> selectAggregationByDept(@Param("taskNo") String taskNo);

    List<RiskLevelDeptStatisticsVO> selectRiskLevelListByTaskNo(@Param("taskNo") String taskNo, @Param("schoolYear") String schoolYear);

}