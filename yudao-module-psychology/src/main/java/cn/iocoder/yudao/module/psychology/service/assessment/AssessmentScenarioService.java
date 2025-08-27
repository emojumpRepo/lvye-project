package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentScenarioPageReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentScenarioDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentScenarioSlotDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 测评场景 Service 接口
 *
 * @author 芋道源码
 */
public interface AssessmentScenarioService {

    /**
     * 创建测评场景
     *
     * @param scenarioVO 创建信息
     * @return 编号
     */
    Long createScenario(@Valid AssessmentScenarioVO scenarioVO);

    /**
     * 更新测评场景
     *
     * @param scenarioVO 更新信息
     */
    void updateScenario(@Valid AssessmentScenarioVO scenarioVO);

    /**
     * 删除测评场景
     *
     * @param id 编号
     */
    void deleteScenario(Long id);

    /**
     * 获得测评场景
     *
     * @param id 编号
     * @return 测评场景
     */
    AssessmentScenarioDO getScenario(Long id);

    /**
     * 获得启用的测评场景列表（含插槽与插槽问卷信息）
     *
     * @return 场景VO列表
     */
    List<AssessmentScenarioVO> getActiveScenarioList();

    /**
     * 获得详细测评场景（包含场景插槽、问卷）
     */
    AssessmentScenarioVO getScenarioQuestionnaire(Long id, Long userId);

    /**
     * 获得测评场景分页
     *
     * @param pageReqVO 分页查询
     * @return 测评场景分页
     */
    PageResult<AssessmentScenarioDO> getScenarioPage(AssessmentScenarioPageReqVO pageReqVO);

    /**
     * 获得场景的槽位列表
     *
     * @param scenarioId 场景编号
     * @return 槽位列表
     */
    List<AssessmentScenarioSlotDO> getScenarioSlots(Long scenarioId);

    /**
     * 校验场景是否存在且启用
     *
     * @param scenarioId 场景编号
     * @return 场景信息
     */
    AssessmentScenarioDO validateScenarioActive(Long scenarioId);

    /**
     * 校验场景问卷数量限制
     *
     * @param scenarioId 场景编号
     * @param questionnaireCount 问卷数量
     */
    void validateQuestionnaireCount(Long scenarioId, int questionnaireCount);

    /**
     * 更新场景插槽
     *
     * @param scenarioId 场景编号
     * @param slots 插槽列表
     */
    void updateScenarioSlots(Long scenarioId, List<AssessmentScenarioVO.ScenarioSlotVO> slots);
}
