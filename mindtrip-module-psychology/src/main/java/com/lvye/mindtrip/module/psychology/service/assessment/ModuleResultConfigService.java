package com.lvye.mindtrip.module.psychology.service.assessment;

import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.resultconfig.ModuleResultConfigRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.resultconfig.ModuleResultConfigSaveReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;

import java.util.List;

public interface ModuleResultConfigService {

    Long create(ModuleResultConfigSaveReqVO req);

    void update(ModuleResultConfigSaveReqVO req);

    void delete(Long id);

    ModuleResultConfigRespVO get(Long id);

    List<ModuleResultConfigDO> listBySlot(Long scenarioSlotId);
}


