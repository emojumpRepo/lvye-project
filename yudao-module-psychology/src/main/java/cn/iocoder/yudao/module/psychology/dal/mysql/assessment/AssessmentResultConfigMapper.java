package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 测评结果配置 Mapper
 *
 * @author MinGoo
 */
@Mapper
public interface AssessmentResultConfigMapper extends BaseMapperX<AssessmentResultConfigDO> {

    /**
     * 根据场景ID查询配置列表
     */
    default List<AssessmentResultConfigDO> selectListByScenarioId(Long scenarioId) {
        return selectList(new LambdaQueryWrapperX<AssessmentResultConfigDO>()
                .eq(AssessmentResultConfigDO::getScenarioId, scenarioId)
                .eq(AssessmentResultConfigDO::getStatus, 1)
                .orderByAsc(AssessmentResultConfigDO::getId));
    }

    /**
     * 根据场景ID和规则类型查询配置列表
     */
    default List<AssessmentResultConfigDO> selectListByScenarioIdAndRuleType(Long scenarioId, Integer ruleType) {
        return selectList(new LambdaQueryWrapperX<AssessmentResultConfigDO>()
                .eq(AssessmentResultConfigDO::getScenarioId, scenarioId)
                .eq(AssessmentResultConfigDO::getRuleType, ruleType)
                .eq(AssessmentResultConfigDO::getStatus, 1)
                .orderByAsc(AssessmentResultConfigDO::getId));
    }

}