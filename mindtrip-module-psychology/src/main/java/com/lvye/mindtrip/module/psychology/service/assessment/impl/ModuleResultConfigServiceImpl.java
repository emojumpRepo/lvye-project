package com.lvye.mindtrip.module.psychology.service.assessment.impl;

import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.resultconfig.ModuleResultConfigRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.resultconfig.ModuleResultConfigSaveReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.ModuleResultConfigDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.assessment.ModuleResultConfigMapper;
import com.lvye.mindtrip.module.psychology.service.assessment.ModuleResultConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuleResultConfigServiceImpl implements ModuleResultConfigService {

    @Resource
    private ModuleResultConfigMapper moduleResultConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ModuleResultConfigSaveReqVO req) {
        ModuleResultConfigDO d = new ModuleResultConfigDO();
        d.setId(null);
        d.setScenarioSlotId(req.getScenarioSlotId());
        d.setConfigName(req.getConfigName());
        d.setRuleType(req.getRuleType());
        d.setCalculateFormula(req.getCalculateFormula());
        d.setDescription(req.getDescription());
        d.setLevel(req.getLevel());
        d.setSuggestions(req.getSuggestions());
        d.setComments(req.getComments());
        d.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        moduleResultConfigMapper.insert(d);
        return d.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ModuleResultConfigSaveReqVO req) {
        ModuleResultConfigDO d = new ModuleResultConfigDO();
        d.setId(req.getId());
        d.setScenarioSlotId(req.getScenarioSlotId());
        d.setConfigName(req.getConfigName());
        d.setRuleType(req.getRuleType());
        d.setCalculateFormula(req.getCalculateFormula());
        d.setDescription(req.getDescription());
        d.setLevel(req.getLevel());
        d.setSuggestions(req.getSuggestions());
        d.setComments(req.getComments());
        d.setStatus(req.getStatus());
        moduleResultConfigMapper.updateById(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        moduleResultConfigMapper.deleteById(id);
    }

    @Override
    public ModuleResultConfigRespVO get(Long id) {
        ModuleResultConfigDO d = moduleResultConfigMapper.selectById(id);
        if (d == null) {
            return null;
        }
        ModuleResultConfigRespVO vo = new ModuleResultConfigRespVO();
        vo.setId(d.getId());
        vo.setScenarioSlotId(d.getScenarioSlotId());
        vo.setConfigName(d.getConfigName());
        vo.setRuleType(d.getRuleType());
        vo.setCalculateFormula(d.getCalculateFormula());
        vo.setDescription(d.getDescription());
        vo.setLevel(d.getLevel());
        vo.setSuggestions(d.getSuggestions());
        vo.setComments(d.getComments());
        vo.setStatus(d.getStatus());
        vo.setCreateTime(d.getCreateTime());
        vo.setUpdateTime(d.getUpdateTime());
        return vo;
    }

    @Override
    public List<ModuleResultConfigDO> listBySlot(Long scenarioSlotId) {
        return moduleResultConfigMapper.selectByScenarioSlotId(scenarioSlotId);
    }
}


