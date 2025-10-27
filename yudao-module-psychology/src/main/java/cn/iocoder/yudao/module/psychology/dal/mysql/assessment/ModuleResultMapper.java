package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.ModuleResultDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 模块结果 Mapper
 *
 * @author MinGoo
 */
@Mapper
public interface ModuleResultMapper extends BaseMapperX<ModuleResultDO> {

    // 根据任务编号+用户ID查询模块结果列表
    default List<ModuleResultDO> selectListByTaskNoAndUserId(String taskNo, Long userId) {
        return selectList(new LambdaQueryWrapperX<ModuleResultDO>()
                .eq(ModuleResultDO::getAssessmentTaskNo, taskNo)
                .eq(ModuleResultDO::getUserId, userId)
                .orderByAsc(ModuleResultDO::getScenarioSlotId));
    }

    // 根据任务编号+用户ID+插槽ID查询
    default ModuleResultDO selectByTaskNoAndUserIdAndSlotId(String taskNo, Long userId, Long scenarioSlotId) {
        return selectOne(new LambdaQueryWrapperX<ModuleResultDO>()
                .eq(ModuleResultDO::getAssessmentTaskNo, taskNo)
                .eq(ModuleResultDO::getUserId, userId)
                .eq(ModuleResultDO::getScenarioSlotId, scenarioSlotId));
    }

    /**
     * 根据场景插槽ID查询所有模块结果
     */
    default List<ModuleResultDO> selectListByScenarioSlotId(Long scenarioSlotId) {
        return selectList(ModuleResultDO::getScenarioSlotId, scenarioSlotId);
    }

    /**
     * 根据风险等级查询模块结果
     */
    default List<ModuleResultDO> selectListByRiskLevel(Integer riskLevel) {
        return selectList(ModuleResultDO::getRiskLevel, riskLevel);
    }

    // 根据任务编号+用户ID删除
    default void deleteByTaskNoAndUserId(String taskNo, Long userId) {
        delete(new LambdaQueryWrapperX<ModuleResultDO>()
                .eq(ModuleResultDO::getAssessmentTaskNo, taskNo)
                .eq(ModuleResultDO::getUserId, userId));
    }
}
