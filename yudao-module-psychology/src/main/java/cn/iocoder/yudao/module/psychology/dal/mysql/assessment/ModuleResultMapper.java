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

    /**
     * 根据测评结果ID查询模块结果列表
     */
    default List<ModuleResultDO> selectListByAssessmentResultId(Long assessmentResultId) {
        return selectList(new LambdaQueryWrapperX<ModuleResultDO>()
                .eq(ModuleResultDO::getAssessmentResultId, assessmentResultId)
                .orderByAsc(ModuleResultDO::getScenarioSlotId));
    }

    /**
     * 根据测评结果ID和场景插槽ID查询模块结果
     */
    default ModuleResultDO selectByAssessmentResultIdAndScenarioSlotId(Long assessmentResultId, Long scenarioSlotId) {
        return selectOne(new LambdaQueryWrapperX<ModuleResultDO>()
                .eq(ModuleResultDO::getAssessmentResultId, assessmentResultId)
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

    /**
     * 根据测评结果ID删除所有模块结果
     */
    default void deleteByAssessmentResultId(Long assessmentResultId) {
        delete(new LambdaQueryWrapperX<ModuleResultDO>()
                .eq(ModuleResultDO::getAssessmentResultId, assessmentResultId));
    }
}
