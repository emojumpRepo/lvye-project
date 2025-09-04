package cn.iocoder.yudao.module.psychology.service.assessment;

/**
 * 组合测评结果保存服务
 */
public interface AssessmentResultService {

    /**
     * 基于任务与用户，汇总该用户在该任务下的所有问卷结果，生成并落库组合测评结果。
     *
     * @param taskNo 测评任务编号
     * @param userId 用户ID
     * @return 新增的测评结果ID
     */
    Long generateAndSaveCombinedResult(String taskNo, Long userId);
}

