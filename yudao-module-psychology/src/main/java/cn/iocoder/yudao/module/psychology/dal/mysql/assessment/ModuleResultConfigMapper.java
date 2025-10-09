package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ModuleResultConfigMapper extends BaseMapperX<ModuleResultConfigDO> {

    List<ModuleResultConfigDO> selectByScenarioSlotId(Long scenarioSlotId);
}
