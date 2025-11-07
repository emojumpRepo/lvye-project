package com.lvye.mindtrip.module.psychology.dal.mysql.assessment;

import com.lvye.mindtrip.framework.test.core.ut.BaseDbUnitTest;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentScenarioSlotDO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AssessmentScenarioSlotMapper} 的单元测试类
 */
@Import(AssessmentScenarioSlotMapper.class)
public class AssessmentScenarioSlotMapperTest extends BaseDbUnitTest {

    @Resource
    private AssessmentScenarioSlotMapper assessmentScenarioSlotMapper;

    @Test
    public void testDeletePhysicallyByScenarioId() {
        // 先清理可能存在的数据，确保测试独立性
        // 使用物理删除清理所有数据
        List<AssessmentScenarioSlotDO> existingSlots = assessmentScenarioSlotMapper.selectList();
        for (AssessmentScenarioSlotDO slot : existingSlots) {
            assessmentScenarioSlotMapper.deletePhysicallyByScenarioId(slot.getScenarioId());
        }
        
        // 准备测试数据
        AssessmentScenarioSlotDO slot1 = new AssessmentScenarioSlotDO();
        slot1.setScenarioId(1L);
        slot1.setSlotKey("library");
        slot1.setSlotName("图书馆");
        slot1.setSlotOrder(1);
        slot1.setAllowedQuestionnaireTypes("ANXIETY");
        assessmentScenarioSlotMapper.insert(slot1);

        AssessmentScenarioSlotDO slot2 = new AssessmentScenarioSlotDO();
        slot2.setScenarioId(1L);
        slot2.setSlotKey("classroom");
        slot2.setSlotName("教室");
        slot2.setSlotOrder(2);
        slot2.setAllowedQuestionnaireTypes("LEARNING");
        assessmentScenarioSlotMapper.insert(slot2);

        AssessmentScenarioSlotDO slot3 = new AssessmentScenarioSlotDO();
        slot3.setScenarioId(2L);
        slot3.setSlotKey("library");
        slot3.setSlotName("图书馆2");
        slot3.setSlotOrder(1);
        slot3.setAllowedQuestionnaireTypes("ANXIETY");
        assessmentScenarioSlotMapper.insert(slot3);

        // 验证数据已插入
        List<AssessmentScenarioSlotDO> allSlots = assessmentScenarioSlotMapper.selectList();
        assertEquals(3, allSlots.size());

        // 执行物理删除
        int deletedCount = assessmentScenarioSlotMapper.deletePhysicallyByScenarioId(1L);
        assertEquals(2, deletedCount);

        // 验证删除结果
        List<AssessmentScenarioSlotDO> remainingSlots = assessmentScenarioSlotMapper.selectList();
        assertEquals(1, remainingSlots.size());
        assertEquals(2L, remainingSlots.get(0).getScenarioId());
        assertEquals("library", remainingSlots.get(0).getSlotKey());
    }

    @Test
    public void testMultipleDeletesWithSameSlotKey() {
        // 先清理可能存在的数据，确保测试独立性
        // 使用物理删除清理所有数据
        List<AssessmentScenarioSlotDO> existingSlots = assessmentScenarioSlotMapper.selectList();
        for (AssessmentScenarioSlotDO slot : existingSlots) {
            assessmentScenarioSlotMapper.deletePhysicallyByScenarioId(slot.getScenarioId());
        }
        
        // 测试多次删除和插入相同的 slotKey 不会产生冲突
        Long scenarioId = 1L;
        String slotKey = "library";

        // 第一次插入
        AssessmentScenarioSlotDO slot1 = new AssessmentScenarioSlotDO();
        slot1.setScenarioId(scenarioId);
        slot1.setSlotKey(slotKey);
        slot1.setSlotName("图书馆V1");
        slot1.setSlotOrder(1);
        slot1.setAllowedQuestionnaireTypes("ANXIETY");
        assessmentScenarioSlotMapper.insert(slot1);

        // 删除
        assessmentScenarioSlotMapper.deletePhysicallyByScenarioId(scenarioId);

        // 第二次插入相同的 slotKey - 应该不会抛出 DuplicateKeyException
        AssessmentScenarioSlotDO slot2 = new AssessmentScenarioSlotDO();
        slot2.setScenarioId(scenarioId);
        slot2.setSlotKey(slotKey);
        slot2.setSlotName("图书馆V2");
        slot2.setSlotOrder(1);
        slot2.setAllowedQuestionnaireTypes("DEPRESSION");
        assertDoesNotThrow(() -> {
            assessmentScenarioSlotMapper.insert(slot2);
        });

        // 再次删除
        assessmentScenarioSlotMapper.deletePhysicallyByScenarioId(scenarioId);

        // 第三次插入相同的 slotKey - 应该不会抛出 DuplicateKeyException
        AssessmentScenarioSlotDO slot3 = new AssessmentScenarioSlotDO();
        slot3.setScenarioId(scenarioId);
        slot3.setSlotKey(slotKey);
        slot3.setSlotName("图书馆V3");
        slot3.setSlotOrder(1);
        slot3.setAllowedQuestionnaireTypes("SOCIAL");
        assertDoesNotThrow(() -> {
            assessmentScenarioSlotMapper.insert(slot3);
        });

        // 验证最终结果
        List<AssessmentScenarioSlotDO> finalSlots = assessmentScenarioSlotMapper.selectListByScenarioId(scenarioId);
        assertEquals(1, finalSlots.size());
        assertEquals("图书馆V3", finalSlots.get(0).getSlotName());
        assertEquals("SOCIAL", finalSlots.get(0).getAllowedQuestionnaireTypes());
    }
}
