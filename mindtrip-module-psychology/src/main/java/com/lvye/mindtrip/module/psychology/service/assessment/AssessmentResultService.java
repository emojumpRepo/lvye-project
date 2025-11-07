package com.lvye.mindtrip.module.psychology.service.assessment;

import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.result.AssessmentResultDetailRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.result.MtuiUniversityResultRespVO;

import java.util.List;

/**
 * 组合测评结果保存服务
 */
public interface AssessmentResultService {

    /**
     * 基于任务与学生档案，汇总该学生在该任务下的所有问卷结果，生成并落库组合测评结果。
     *
     * @param taskNo 测评任务编号
     * @param studentProfileId 学生档案ID
     * @return 新增的测评结果ID
     */
    Long generateAndSaveCombinedResult(String taskNo, Long studentProfileId);

    /**
     * 根据测评结果ID获取测评结果详情
     *
     * @param id 测评结果ID
     * @return 测评结果详情
     */
    AssessmentResultDetailRespVO getAssessmentResult(Long id);

    /**
     * 重新计算指定测评任务的所有问卷结果
     *
     * @param taskNo 测评任务编号
     */
    void recalculateAssessmentResults(String taskNo);

    /**
     * 重新计算指定测评任务的问卷结果(可指定用户)
     *
     * @param taskNo 测评任务编号
     * @param userIds 用户ID列表,如果为null或空则计算所有用户
     */
    void recalculateAssessmentResults(String taskNo, java.util.List<Long> userIds);

    /**
     * 获取MTUI大学测评结果
     * 根据测评任务编号和用户ID查询该用户在该任务下所有问卷的维度结果
     *
     * @param assessmentTaskNo 测评任务编号
     * @param userId 用户ID
     * @return MTUI大学结果(包含整体测评结果和问卷结果列表)
     */
    MtuiUniversityResultRespVO getMtuiUniversityResults(String assessmentTaskNo, Long userId);
}

