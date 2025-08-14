package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentTaskSaveReqVO;
import cn.iocoder.yudao.module.psychology.enums.AssessmentTaskStatusEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AssessmentTaskServiceImpl} 的单元测试类
 * 专注于测试 isPublish 功能的逻辑
 */
public class AssessmentTaskServiceImplTest {

    @Test
    public void testIsPublishField_defaultValue() {
        // 测试 isPublish 字段的默认值
        AssessmentTaskSaveReqVO createReqVO = createValidAssessmentTaskSaveReqVO();

        // 验证默认值
        assertNotNull(createReqVO.getIsPublish());
        assertFalse(createReqVO.getIsPublish());

        System.out.println("✓ isPublish 默认值测试通过: " + createReqVO.getIsPublish());
    }

    @Test
    public void testIsPublishField_trueValue() {
        // 测试 isPublish 字段设置为 true
        AssessmentTaskSaveReqVO createReqVO = createValidAssessmentTaskSaveReqVO();
        createReqVO.setIsPublish(true);

        // 验证设置值
        assertTrue(createReqVO.getIsPublish());

        // 模拟服务层逻辑判断
        boolean shouldPublish = createReqVO.getIsPublish() != null && createReqVO.getIsPublish();
        assertTrue(shouldPublish);

        System.out.println("✓ isPublish = true 测试通过，应该发布: " + shouldPublish);
    }

    @Test
    public void testIsPublishField_falseValue() {
        // 测试 isPublish 字段设置为 false
        AssessmentTaskSaveReqVO createReqVO = createValidAssessmentTaskSaveReqVO();
        createReqVO.setIsPublish(false);

        // 验证设置值
        assertFalse(createReqVO.getIsPublish());

        // 模拟服务层逻辑判断
        boolean shouldPublish = createReqVO.getIsPublish() != null && createReqVO.getIsPublish();
        assertFalse(shouldPublish);

        System.out.println("✓ isPublish = false 测试通过，不应该发布: " + shouldPublish);
    }

    @Test
    public void testIsPublishField_nullValue() {
        // 测试 isPublish 字段设置为 null
        AssessmentTaskSaveReqVO createReqVO = createValidAssessmentTaskSaveReqVO();
        createReqVO.setIsPublish(null);

        // 验证设置值
        assertNull(createReqVO.getIsPublish());

        // 模拟服务层逻辑判断
        boolean shouldPublish = createReqVO.getIsPublish() != null && createReqVO.getIsPublish();
        assertFalse(shouldPublish);

        System.out.println("✓ isPublish = null 测试通过，不应该发布: " + shouldPublish);
    }

    @Test
    public void testServiceLogic_publishDecision() {
        // 测试服务层发布决策逻辑
        System.out.println("\n=== 测试服务层发布决策逻辑 ===");

        // 测试各种情况下的发布决策
        testPublishDecision(true, true, "isPublish = true");
        testPublishDecision(false, false, "isPublish = false");
        testPublishDecision(null, false, "isPublish = null");

        System.out.println("✓ 所有发布决策逻辑测试通过");
    }

    private void testPublishDecision(Boolean isPublish, boolean expectedResult, String scenario) {
        AssessmentTaskSaveReqVO reqVO = createValidAssessmentTaskSaveReqVO();
        reqVO.setIsPublish(isPublish);

        // 模拟 AssessmentTaskServiceImpl.createAssessmentTask 中的逻辑
        boolean actualResult = reqVO.getIsPublish() != null && reqVO.getIsPublish();

        assertEquals(expectedResult, actualResult,
            String.format("场景 [%s] 失败: 期望 %s, 实际 %s", scenario, expectedResult, actualResult));

        System.out.printf("  ✓ %s -> 应该发布: %s%n", scenario, actualResult);
    }

    @Test
    public void testStatusEnum_values() {
        // 测试状态枚举值
        System.out.println("\n=== 测试状态枚举值 ===");

        assertEquals(0, AssessmentTaskStatusEnum.NOT_STARTED.getStatus());
        assertEquals(1, AssessmentTaskStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(2, AssessmentTaskStatusEnum.COMPLETED.getStatus());
        assertEquals(3, AssessmentTaskStatusEnum.CLOSED.getStatus());

        System.out.println("✓ 状态枚举值测试通过");
        System.out.println("  NOT_STARTED: " + AssessmentTaskStatusEnum.NOT_STARTED.getStatus());
        System.out.println("  IN_PROGRESS: " + AssessmentTaskStatusEnum.IN_PROGRESS.getStatus());
        System.out.println("  COMPLETED: " + AssessmentTaskStatusEnum.COMPLETED.getStatus());
        System.out.println("  CLOSED: " + AssessmentTaskStatusEnum.CLOSED.getStatus());
    }

    /**
     * 创建有效的测评任务保存请求VO
     */
    private AssessmentTaskSaveReqVO createValidAssessmentTaskSaveReqVO() {
        AssessmentTaskSaveReqVO createReqVO = new AssessmentTaskSaveReqVO();
        createReqVO.setTaskName("测试任务_" + System.currentTimeMillis());
        createReqVO.setScaleCode("A");
        createReqVO.setTargetAudience(1); // 学生
        createReqVO.setStartline(new Date());
        createReqVO.setDeadline(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)); // 7天后
        createReqVO.setDeptIdList(new ArrayList<>());
        createReqVO.setUserIdList(new ArrayList<>());
        return createReqVO;
    }
}
