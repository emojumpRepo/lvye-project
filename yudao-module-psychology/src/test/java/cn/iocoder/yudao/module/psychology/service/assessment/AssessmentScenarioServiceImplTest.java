package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentScenarioDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentScenarioSlotDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentScenarioMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentScenarioSlotMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AssessmentScenarioServiceImpl} 的单元测试类
 */
@Import(AssessmentScenarioServiceImpl.class)
public class AssessmentScenarioServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AssessmentScenarioService assessmentScenarioService;

    @Resource
    private AssessmentScenarioMapper scenarioMapper;

    @Resource
    private AssessmentScenarioSlotMapper scenarioSlotMapper;

    @Test
    public void testUpdateScenarioSlots_NoDuplicateKeyException() {
        // 准备测试数据：创建一个场景
        AssessmentScenarioVO scenarioVO = new AssessmentScenarioVO();
        scenarioVO.setCode("TEST_SCENARIO");
        scenarioVO.setName("测试场景");
        scenarioVO.setFrontendRoute("testRoute");
        scenarioVO.setIsActive(true);

        // 创建初始插槽
        AssessmentScenarioVO.ScenarioSlotVO slot1 = new AssessmentScenarioVO.ScenarioSlotVO();
        slot1.setSlotKey("library");
        slot1.setSlotName("图书馆");
        slot1.setSlotOrder(1);
        slot1.setAllowedQuestionnaireTypes("ANXIETY,DEPRESSION");

        AssessmentScenarioVO.ScenarioSlotVO slot2 = new AssessmentScenarioVO.ScenarioSlotVO();
        slot2.setSlotKey("classroom");
        slot2.setSlotName("教室");
        slot2.setSlotOrder(2);
        slot2.setAllowedQuestionnaireTypes("LEARNING");

        scenarioVO.setSlots(Arrays.asList(slot1, slot2));

        // 创建场景
        Long scenarioId = assessmentScenarioService.createScenario(scenarioVO);
        assertNotNull(scenarioId);

        // 验证插槽已创建
        List<AssessmentScenarioSlotDO> initialSlots = assessmentScenarioService.getScenarioSlots(scenarioId);
        assertEquals(2, initialSlots.size());

        // 更新插槽：保持相同的 slotKey，但修改其他属性
        AssessmentScenarioVO.ScenarioSlotVO updatedSlot1 = new AssessmentScenarioVO.ScenarioSlotVO();
        updatedSlot1.setSlotKey("library"); // 相同的 slotKey
        updatedSlot1.setSlotName("新图书馆"); // 修改名称
        updatedSlot1.setSlotOrder(1);
        updatedSlot1.setAllowedQuestionnaireTypes("ANXIETY"); // 修改允许的问卷类型

        AssessmentScenarioVO.ScenarioSlotVO updatedSlot2 = new AssessmentScenarioVO.ScenarioSlotVO();
        updatedSlot2.setSlotKey("classroom"); // 相同的 slotKey
        updatedSlot2.setSlotName("新教室"); // 修改名称
        updatedSlot2.setSlotOrder(2);
        updatedSlot2.setAllowedQuestionnaireTypes("LEARNING,SOCIAL");

        // 添加一个新插槽
        AssessmentScenarioVO.ScenarioSlotVO newSlot = new AssessmentScenarioVO.ScenarioSlotVO();
        newSlot.setSlotKey("cafeteria");
        newSlot.setSlotName("食堂");
        newSlot.setSlotOrder(3);
        newSlot.setAllowedQuestionnaireTypes("SOCIAL");

        List<AssessmentScenarioVO.ScenarioSlotVO> updatedSlots = Arrays.asList(updatedSlot1, updatedSlot2, newSlot);

        // 执行更新操作 - 这里不应该抛出 DuplicateKeyException
        assertDoesNotThrow(() -> {
            assessmentScenarioService.updateScenarioSlots(scenarioId, updatedSlots);
        });

        // 验证更新结果
        List<AssessmentScenarioSlotDO> finalSlots = assessmentScenarioService.getScenarioSlots(scenarioId);
        assertEquals(3, finalSlots.size());

        // 验证插槽内容已更新
        AssessmentScenarioSlotDO librarySlot = finalSlots.stream()
                .filter(slot -> "library".equals(slot.getSlotKey()))
                .findFirst()
                .orElse(null);
        assertNotNull(librarySlot);
        assertEquals("新图书馆", librarySlot.getSlotName());
        assertEquals("ANXIETY", librarySlot.getAllowedQuestionnaireTypes());

        // 验证新插槽已添加
        AssessmentScenarioSlotDO cafeteriaSlot = finalSlots.stream()
                .filter(slot -> "cafeteria".equals(slot.getSlotKey()))
                .findFirst()
                .orElse(null);
        assertNotNull(cafeteriaSlot);
        assertEquals("食堂", cafeteriaSlot.getSlotName());
    }

    @Test
    public void testMultipleUpdatesWithSameSlotKey() {
        // 测试多次更新相同的 slotKey 不会产生冲突
        AssessmentScenarioVO scenarioVO = new AssessmentScenarioVO();
        scenarioVO.setCode("MULTI_UPDATE_TEST");
        scenarioVO.setName("多次更新测试");
        scenarioVO.setFrontendRoute("multiUpdateRoute");
        scenarioVO.setIsActive(true);

        AssessmentScenarioVO.ScenarioSlotVO slot = new AssessmentScenarioVO.ScenarioSlotVO();
        slot.setSlotKey("library");
        slot.setSlotName("图书馆");
        slot.setSlotOrder(1);
        slot.setAllowedQuestionnaireTypes("ANXIETY");

        scenarioVO.setSlots(Arrays.asList(slot));

        // 创建场景
        Long scenarioId = assessmentScenarioService.createScenario(scenarioVO);

        // 第一次更新
        slot.setSlotName("图书馆V2");
        assertDoesNotThrow(() -> {
            assessmentScenarioService.updateScenarioSlots(scenarioId, Arrays.asList(slot));
        });

        // 第二次更新
        slot.setSlotName("图书馆V3");
        assertDoesNotThrow(() -> {
            assessmentScenarioService.updateScenarioSlots(scenarioId, Arrays.asList(slot));
        });

        // 第三次更新
        slot.setSlotName("图书馆V4");
        assertDoesNotThrow(() -> {
            assessmentScenarioService.updateScenarioSlots(scenarioId, Arrays.asList(slot));
        });

        // 验证最终结果
        List<AssessmentScenarioSlotDO> finalSlots = assessmentScenarioService.getScenarioSlots(scenarioId);
        assertEquals(1, finalSlots.size());
        assertEquals("图书馆V4", finalSlots.get(0).getSlotName());
    }
}
