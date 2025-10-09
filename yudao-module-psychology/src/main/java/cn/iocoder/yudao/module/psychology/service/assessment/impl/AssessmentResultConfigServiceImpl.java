package cn.iocoder.yudao.module.psychology.service.assessment.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentResultConfigDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentResultConfigMapper;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测评结果配置服务实现
 *
 * @author MinGoo
 */
@Service
public class AssessmentResultConfigServiceImpl implements AssessmentResultConfigService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentResultConfigServiceImpl.class);

    @Resource
    private AssessmentResultConfigMapper assessmentResultConfigMapper;

    @Override
    public Long createAssessmentResultConfig(AssessmentResultConfigDO config) {
        // 验证JSON规则
        if (!validateJsonRule(config.getCalculateFormula())) {
            throw new IllegalArgumentException("计算公式JSON格式无效");
        }

        assessmentResultConfigMapper.insert(config);
        logger.info("创建测评结果配置成功: id={}, scenarioId={}, configName={}", 
            config.getId(), config.getScenarioId(), config.getConfigName());
        return config.getId();
    }

    @Override
    public void updateAssessmentResultConfig(AssessmentResultConfigDO config) {
        // 验证JSON规则
        if (!validateJsonRule(config.getCalculateFormula())) {
            throw new IllegalArgumentException("计算公式JSON格式无效");
        }

        assessmentResultConfigMapper.updateById(config);
        logger.info("更新测评结果配置成功: id={}", config.getId());
    }

    @Override
    public void deleteAssessmentResultConfig(Long id) {
        assessmentResultConfigMapper.deleteById(id);
        logger.info("删除测评结果配置成功: id={}", id);
    }

    @Override
    public AssessmentResultConfigDO getAssessmentResultConfig(Long id) {
        return assessmentResultConfigMapper.selectById(id);
    }

    @Override
    public List<AssessmentResultConfigDO> getAssessmentResultConfigsByScenarioId(Long scenarioId) {
        return assessmentResultConfigMapper.selectListByScenarioId(scenarioId);
    }

    @Override
    public List<AssessmentResultConfigDO> getAssessmentResultConfigsByScenarioIdAndRuleType(Long scenarioId, Integer ruleType) {
        return assessmentResultConfigMapper.selectListByScenarioIdAndRuleType(scenarioId, ruleType);
    }

    @Override
    public boolean validateJsonRule(String calculateFormula) {
        if (calculateFormula == null || calculateFormula.trim().isEmpty()) {
            return false;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(calculateFormula);
            
            // 基本的JSON格式验证
            if (!jsonNode.isObject()) {
                logger.warn("计算公式不是有效的JSON对象: {}", calculateFormula);
                return false;
            }

            logger.debug("JSON规则验证通过: {}", calculateFormula);
            return true;
        } catch (Exception e) {
            logger.error("JSON规则验证失败: {}", calculateFormula, e);
            return false;
        }
    }

}
