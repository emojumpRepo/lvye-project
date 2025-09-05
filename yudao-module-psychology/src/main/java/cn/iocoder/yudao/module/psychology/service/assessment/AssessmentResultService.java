package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.result.AssessmentResultDetailRespVO;

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
}

