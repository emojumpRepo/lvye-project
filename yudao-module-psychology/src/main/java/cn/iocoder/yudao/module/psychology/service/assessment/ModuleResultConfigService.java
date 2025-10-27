package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.resultconfig.ModuleResultConfigRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.resultconfig.ModuleResultConfigSaveReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;

import java.util.List;

public interface ModuleResultConfigService {

    Long create(ModuleResultConfigSaveReqVO req);

    void update(ModuleResultConfigSaveReqVO req);

    void delete(Long id);

    ModuleResultConfigRespVO get(Long id);

    List<ModuleResultConfigDO> listBySlot(Long scenarioSlotId);
}


