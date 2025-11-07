package com.lvye.mindtrip.module.psychology.dal.mysql.assessment;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ModuleResultConfigMapper extends BaseMapperX<ModuleResultConfigDO> {

    default List<ModuleResultConfigDO> selectByScenarioSlotId(Long scenarioSlotId) {
        return selectList(new LambdaQueryWrapperX<ModuleResultConfigDO>()
                .eq(ModuleResultConfigDO::getScenarioSlotId, scenarioSlotId)
                .eq(ModuleResultConfigDO::getStatus, 1)
                .orderByAsc(ModuleResultConfigDO::getId));
    }
}
